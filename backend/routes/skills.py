from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import db, User, Skill, UserSkill
from utils.auth import admin_required
from utils.response import APIResponse

skills_bp = Blueprint('skills', __name__)

@skills_bp.route('', methods=['GET'])
@jwt_required()
def get_skills():
    """
    Get a list of available skills with optional filtering
    
    Query parameters:
    - category: Filter by category
    - search: Search by name
    - page: Page number (default: 1)
    - per_page: Results per page (default: 20, max: 100)
    """
    category = request.args.get('category')
    search = request.args.get('search')
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 20)), 100)
    
    # Build query
    query = Skill.query
    
    if category:
        query = query.filter(Skill.category == category)
    
    if search:
        query = query.filter(Skill.name.ilike(f'%{search}%'))
    
    # Order by name
    query = query.order_by(Skill.name)
    
    # Execute paginated query
    skills_pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    skills = skills_pagination.items
    
    # Format response
    skills_data = []
    for skill in skills:
        skills_data.append({
            'id': skill.id,
            'name': skill.name,
            'category': skill.category,
            'description': skill.description
        })
    
    return APIResponse.success_response(
        data={
            'skills': skills_data,
            'pagination': {
                'total': skills_pagination.total,
                'pages': skills_pagination.pages,
                'page': page,
                'per_page': per_page
            }
        }
    )


@skills_bp.route('', methods=['POST'])
@jwt_required()
@admin_required()
def create_skill():
    """Create a new skill (admin only)"""
    data = request.get_json()
    
    if not data or 'name' not in data:
        return APIResponse.error_response('Skill name is required', status_code=400)
    
    # Check if skill already exists
    existing_skill = Skill.query.filter(Skill.name.ilike(data['name'])).first()
    if existing_skill:
        return APIResponse.error_response('Skill already exists', status_code=409)
    
    try:
        skill = Skill(
            name=data['name'],
            category=data.get('category'),
            description=data.get('description')
        )
        
        db.session.add(skill)
        db.session.commit()
        
        return APIResponse.success_response(
            message='Skill created successfully',
            data={
                'skill': {
                    'id': skill.id,
                    'name': skill.name,
                    'category': skill.category,
                    'description': skill.description
                }
            },
            status_code=201
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@skills_bp.route('/<int:skill_id>', methods=['PUT'])
@jwt_required()
@admin_required()
def update_skill(skill_id):
    """Update a skill (admin only)"""
    skill = Skill.query.get(skill_id)
    
    if not skill:
        return APIResponse.error_response('Skill not found', status_code=404)
    
    data = request.get_json()
    if not data:
        return APIResponse.error_response('No data provided', status_code=400)
    
    try:
        # Update fields
        if 'name' in data:
            # Check for name conflict
            existing_skill = Skill.query.filter(
                Skill.name.ilike(data['name']),
                Skill.id != skill_id
            ).first()
            
            if existing_skill:
                return APIResponse.error_response('Another skill with this name already exists', status_code=409)
            
            skill.name = data['name']
        
        if 'category' in data:
            skill.category = data['category']
        
        if 'description' in data:
            skill.description = data['description']
        
        db.session.commit()
        
        return APIResponse.success_response(
            message='Skill updated successfully',
            data={
                'skill': {
                    'id': skill.id,
                    'name': skill.name,
                    'category': skill.category,
                    'description': skill.description
                }
            }
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@skills_bp.route('/<int:skill_id>', methods=['DELETE'])
@jwt_required()
@admin_required()
def delete_skill(skill_id):
    """Delete a skill (admin only)"""
    skill = Skill.query.get(skill_id)
    
    if not skill:
        return APIResponse.error_response('Skill not found', status_code=404)
    
    try:
        db.session.delete(skill)
        db.session.commit()
        return APIResponse.success_response(message='Skill deleted successfully')
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@skills_bp.route('/user', methods=['GET'])
@jwt_required()
def get_user_skills():
    """Get skills for the current user"""
    current_user_id = get_jwt_identity()
    
    # Get all user skills
    user_skills = UserSkill.query.filter_by(user_id=current_user_id).all()
    
    # Format response
    skills_data = []
    for user_skill in user_skills:
        skill = Skill.query.get(user_skill.skill_id)
        if not skill:
            continue
        
        skills_data.append({
            'id': user_skill.id,
            'skill_id': user_skill.skill_id,
            'skill_name': skill.name,
            'skill_category': skill.category,
            'proficiency_level': user_skill.proficiency_level,
            'years_of_experience': user_skill.years_of_experience
        })
    
    return APIResponse.success_response(data={'skills': skills_data})


@skills_bp.route('/user/<int:user_id>', methods=['GET'])
@jwt_required()
def get_other_user_skills(user_id):
    """Get skills for a specific user"""
    # Check if user exists
    user = User.query.get(user_id)
    if not user:
        return APIResponse.error_response('User not found', status_code=404)
    
    # Get all user skills
    user_skills = UserSkill.query.filter_by(user_id=user_id).all()
    
    # Format response
    skills_data = []
    for user_skill in user_skills:
        skill = Skill.query.get(user_skill.skill_id)
        if not skill:
            continue
        
        skills_data.append({
            'skill_id': user_skill.skill_id,
            'skill_name': skill.name,
            'skill_category': skill.category,
            'proficiency_level': user_skill.proficiency_level,
            'years_of_experience': user_skill.years_of_experience
        })
    
    return APIResponse.success_response(data={'skills': skills_data})


@skills_bp.route('/user', methods=['POST'])
@jwt_required()
def add_user_skill():
    """Add a skill to the current user"""
    current_user_id = get_jwt_identity()
    data = request.get_json()
    
    if not data or 'skill_id' not in data:
        return APIResponse.error_response('Skill ID is required', status_code=400)
    
    skill_id = data['skill_id']
    proficiency_level = data.get('proficiency_level', 1)
    years_of_experience = data.get('years_of_experience')
    
    # Check if skill exists
    skill = Skill.query.get(skill_id)
    if not skill:
        return APIResponse.error_response('Skill not found', status_code=404)
    
    # Check if user already has this skill
    existing_user_skill = UserSkill.query.filter_by(
        user_id=current_user_id,
        skill_id=skill_id
    ).first()
    
    if existing_user_skill:
        return APIResponse.error_response('User already has this skill', status_code=409)
    
    # Validate proficiency level
    if not 1 <= proficiency_level <= 5:
        return APIResponse.error_response('Proficiency level must be between 1 and 5', status_code=400)
    
    try:
        user_skill = UserSkill(
            user_id=current_user_id,
            skill_id=skill_id,
            proficiency_level=proficiency_level,
            years_of_experience=years_of_experience
        )
        
        db.session.add(user_skill)
        db.session.commit()
        
        return APIResponse.success_response(
            message='Skill added successfully',
            data={
                'user_skill': {
                    'id': user_skill.id,
                    'user_id': user_skill.user_id,
                    'skill_id': user_skill.skill_id,
                    'skill_name': skill.name,
                    'proficiency_level': user_skill.proficiency_level,
                    'years_of_experience': user_skill.years_of_experience
                }
            },
            status_code=201
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@skills_bp.route('/user/<int:user_skill_id>', methods=['PUT'])
@jwt_required()
def update_user_skill(user_skill_id):
    """Update a user skill"""
    current_user_id = get_jwt_identity()
    
    # Check if user skill exists and belongs to current user
    user_skill = UserSkill.query.get(user_skill_id)
    if not user_skill or user_skill.user_id != current_user_id:
        return APIResponse.error_response('User skill not found', status_code=404)
    
    data = request.get_json()
    if not data:
        return APIResponse.error_response('No data provided', status_code=400)
    
    try:
        # Update fields
        if 'proficiency_level' in data:
            proficiency_level = data['proficiency_level']
            if not 1 <= proficiency_level <= 5:
                return APIResponse.error_response('Proficiency level must be between 1 and 5', status_code=400)
            user_skill.proficiency_level = proficiency_level
        
        if 'years_of_experience' in data:
            user_skill.years_of_experience = data['years_of_experience']
        
        db.session.commit()
        
        # Get skill name for response
        skill = Skill.query.get(user_skill.skill_id)
        skill_name = skill.name if skill else "Unknown"
        
        return APIResponse.success_response(
            message='User skill updated successfully',
            data={
                'user_skill': {
                    'id': user_skill.id,
                    'user_id': user_skill.user_id,
                    'skill_id': user_skill.skill_id,
                    'skill_name': skill_name,
                    'proficiency_level': user_skill.proficiency_level,
                    'years_of_experience': user_skill.years_of_experience
                }
            }
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@skills_bp.route('/user/<int:user_skill_id>', methods=['DELETE'])
@jwt_required()
def delete_user_skill(user_skill_id):
    """Delete a user skill"""
    current_user_id = get_jwt_identity()
    
    # Check if user skill exists and belongs to current user
    user_skill = UserSkill.query.get(user_skill_id)
    if not user_skill or user_skill.user_id != current_user_id:
        return APIResponse.error_response('User skill not found', status_code=404)
    
    try:
        db.session.delete(user_skill)
        db.session.commit()
        return APIResponse.success_response(message='User skill deleted successfully')
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500) 