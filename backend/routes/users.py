from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import db, User, Profile
from utils.auth import admin_required
from utils.response import APIResponse

users_bp = Blueprint('users', __name__)

@users_bp.route('', methods=['GET'])
@jwt_required()
def get_users():
    """
    Get a list of users with optional filtering
    
    Query parameters:
    - user_type: Filter by user type (student, alumni, admin)
    - is_mentor: Filter by mentor status (true/false)
    - search: Search by name or email
    - page: Page number (default: 1)
    - per_page: Results per page (default: 10, max: 50)
    """
    # Get query parameters
    user_type = request.args.get('user_type')
    is_mentor = request.args.get('is_mentor')
    search = request.args.get('search')
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 10)), 50)
    
    # Base query
    query = User.query
    
    # Apply filters
    if user_type:
        query = query.filter(User.user_type == user_type)
    
    if search:
        search_term = f'%{search}%'
        query = query.filter(
            (User.first_name.ilike(search_term)) | 
            (User.last_name.ilike(search_term)) | 
            (User.email.ilike(search_term))
        )
    
    if is_mentor:
        # Join with Profile to filter by mentor status
        is_mentor_bool = is_mentor.lower() == 'true'
        query = query.join(Profile).filter(Profile.is_mentor == is_mentor_bool)
    
    # Execute paginated query
    users_pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    users = users_pagination.items
    
    # Format response
    users_data = []
    for user in users:
        user_data = {
            'id': user.id,
            'email': user.email,
            'first_name': user.first_name,
            'last_name': user.last_name,
            'full_name': user.full_name,
            'user_type': user.user_type,
            'is_verified': user.is_verified,
            'created_at': user.created_at.isoformat()
        }
        
        if user.profile:
            user_data['profile'] = {
                'bio': user.profile.bio,
                'location': user.profile.location,
                'university': user.profile.university,
                'industry': user.profile.industry,
                'company': user.profile.company,
                'position': user.profile.position,
                'is_mentor': user.profile.is_mentor
            }
        
        users_data.append(user_data)
    
    return APIResponse.success_response(
        data={
            'users': users_data,
            'pagination': {
                'total': users_pagination.total,
                'pages': users_pagination.pages,
                'page': page,
                'per_page': per_page
            }
        }
    )


@users_bp.route('/<int:user_id>', methods=['GET'])
@jwt_required()
def get_user(user_id):
    """Get a specific user by ID"""
    user = User.query.get(user_id)
    
    if not user:
        return APIResponse.not_found('User')
    
    # Format response
    user_data = {
        'id': user.id,
        'email': user.email,
        'first_name': user.first_name,
        'last_name': user.last_name,
        'full_name': user.full_name,
        'user_type': user.user_type,
        'is_verified': user.is_verified,
        'created_at': user.created_at.isoformat()
    }
    
    # Include profile data if available
    if user.profile:
        user_data['profile'] = {
            'bio': user.profile.bio,
            'location': user.profile.location,
            'university': user.profile.university,
            'degree': user.profile.degree,
            'graduation_year': user.profile.graduation_year,
            'department': user.profile.department,
            'industry': user.profile.industry,
            'company': user.profile.company,
            'position': user.profile.position,
            'years_of_experience': user.profile.years_of_experience,
            'linkedin_url': user.profile.linkedin_url,
            'is_mentor': user.profile.is_mentor,
            'mentorship_areas': user.profile.mentorship_areas
        }
    
    return APIResponse.success_response(data={'user': user_data})


@users_bp.route('/<int:user_id>', methods=['PUT'])
@jwt_required()
def update_user(user_id):
    """Update a user (requires admin or self)"""
    current_user_id = get_jwt_identity()
    
    # Permission check: allow only admins or the user themselves
    current_user = User.query.get(current_user_id)
    if current_user_id != user_id and current_user.user_type != 'admin':
        return APIResponse.forbidden('Permission denied')
    
    user = User.query.get(user_id)
    if not user:
        return APIResponse.not_found('User')
    
    data = request.get_json()
    if not data:
        return APIResponse.error_response('No data provided', status_code=400)
    
    try:
        # Update allowed fields
        if 'first_name' in data:
            user.first_name = data['first_name']
            
        if 'last_name' in data:
            user.last_name = data['last_name']
            
        # Only admins can update certain fields
        if current_user.user_type == 'admin':
            if 'user_type' in data:
                user.user_type = data['user_type']
                
            if 'is_active' in data:
                user.is_active = data['is_active']
                
            if 'is_verified' in data:
                user.is_verified = data['is_verified']
        
        db.session.commit()
        
        return APIResponse.success_response(
            message='User updated successfully',
            data={
                'user': {
                    'id': user.id,
                    'email': user.email,
                    'first_name': user.first_name,
                    'last_name': user.last_name,
                    'user_type': user.user_type,
                    'is_active': user.is_active,
                    'is_verified': user.is_verified
                }
            }
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@users_bp.route('/<int:user_id>', methods=['DELETE'])
@admin_required()
def delete_user(user_id):
    """Delete a user (admin only)"""
    user = User.query.get(user_id)
    
    if not user:
        return APIResponse.not_found('User')
    
    try:
        db.session.delete(user)
        db.session.commit()
        return APIResponse.success_response(message='User deleted successfully')
    
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@users_bp.route('/alumni/search', methods=['GET'])
@jwt_required()
def search_alumni():
    """
    Search for alumni users with filtering options
    
    Query parameters:
    - name: Search by first name or last name
    - university: Filter by university
    - department: Filter by department
    - industry: Filter by industry
    - position: Filter by position/job title
    - page: Page number (default: 1)
    - per_page: Results per page (default: 20, max: 50)
    """
    # Get query parameters
    name = request.args.get('name', '')
    university = request.args.get('university')
    department = request.args.get('department')
    industry = request.args.get('industry')
    position = request.args.get('position')
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 20)), 50)
    
    # Base query: get alumni users
    query = User.query.filter_by(user_type='alumni')
    
    # Apply name search filter if provided
    if name:
        search_term = f'%{name}%'
        query = query.filter(
            (User.first_name.ilike(search_term)) | 
            (User.last_name.ilike(search_term))
        )
    
    # Apply additional filters that require joining with Profile
    if university or department or industry or position:
        query = query.join(Profile)
        
        if university:
            query = query.filter(Profile.university.ilike(f'%{university}%'))
        
        if department:
            query = query.filter(Profile.department.ilike(f'%{department}%'))
        
        if industry:
            query = query.filter(Profile.industry.ilike(f'%{industry}%'))
        
        if position:
            query = query.filter(Profile.position.ilike(f'%{position}%'))
    
    # Execute paginated query
    users_pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    users = users_pagination.items
    
    # Format response
    users_data = []
    for user in users:
        user_data = {
            'id': user.id,
            'first_name': user.first_name,
            'last_name': user.last_name,
            'full_name': user.full_name,
            'profile_picture': user.profile.profile_picture if user.profile else None
        }
        
        if user.profile:
            user_data.update({
                'university': user.profile.university,
                'department': user.profile.department,
                'industry': user.profile.industry,
                'position': user.profile.position,
                'is_mentor': user.profile.is_mentor
            })
        
        users_data.append(user_data)
    
    return APIResponse.success_response(
        data={
            'users': users_data,
            'pagination': {
                'total': users_pagination.total,
                'pages': users_pagination.pages,
                'page': page,
                'per_page': per_page
            },
            'filters': {
                'name': name,
                'university': university,
                'department': department,
                'industry': industry,
                'position': position
            }
        }
    )


@users_bp.route('/alumni/filter-options', methods=['GET'])
@jwt_required()
def get_alumni_filter_options():
    """
    Get available filter options for alumni search
    
    Returns lists of universities, departments, industries, and positions
    that are used by alumni in the system
    """
    try:
        # Query distinct values from profiles of alumni users
        universities_query = db.session.query(Profile.university).join(User).filter(
            User.user_type == 'alumni',
            Profile.university.isnot(None),
            Profile.university != ''
        ).distinct().order_by(Profile.university)
        
        departments_query = db.session.query(Profile.department).join(User).filter(
            User.user_type == 'alumni',
            Profile.department.isnot(None),
            Profile.department != ''
        ).distinct().order_by(Profile.department)
        
        industries_query = db.session.query(Profile.industry).join(User).filter(
            User.user_type == 'alumni',
            Profile.industry.isnot(None),
            Profile.industry != ''
        ).distinct().order_by(Profile.industry)
        
        positions_query = db.session.query(Profile.position).join(User).filter(
            User.user_type == 'alumni',
            Profile.position.isnot(None),
            Profile.position != ''
        ).distinct().order_by(Profile.position)
        
        # Convert query results to lists
        universities = [u[0] for u in universities_query.all()]
        departments = [d[0] for d in departments_query.all()]
        industries = [i[0] for i in industries_query.all()]
        positions = [p[0] for p in positions_query.all()]
        
        return APIResponse.success_response(
            data={
                'filter_options': {
                    'universities': universities,
                    'departments': departments,
                    'industries': industries,
                    'positions': positions
                }
            }
        )
    except Exception as e:
        return APIResponse.error_response(str(e), status_code=500) 