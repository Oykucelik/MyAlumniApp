from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import db, User, Profile
from utils.auth import user_owns_resource
from utils.response import APIResponse

profiles_bp = Blueprint('profiles', __name__)

@profiles_bp.route('/<int:user_id>', methods=['GET'])
@jwt_required()
def get_profile(user_id):
    """Get a user's profile by user ID"""
    try:
        current_user_id = get_jwt_identity()
        # Print for debugging 
        print(f"JWT identity: {current_user_id}, type: {type(current_user_id)}")
        print(f"Requested profile for user_id: {user_id}, type: {type(user_id)}")
        
        user = User.query.get(user_id)
        
        if not user:
            return APIResponse.not_found('User')
        
        profile = user.profile
        
        if not profile:
            return APIResponse.not_found('Profile')
        
        # Format response
        profile_data = {
            'id': profile.id,
            'user_id': profile.user_id,
            'bio': profile.bio,
            'profile_picture': profile.profile_picture,
            'phone_number': profile.phone_number,
            'location': profile.location,
            'university': profile.university,
            'degree': profile.degree,
            'graduation_year': profile.graduation_year,
            'department': profile.department,
            'company': profile.company,
            'position': profile.position,
            'industry': profile.industry,
            'years_of_experience': profile.years_of_experience,
            'linkedin_url': profile.linkedin_url,
            'is_mentor': profile.is_mentor,
            'mentorship_areas': profile.mentorship_areas,
            'availability': profile.availability,
            'created_at': profile.created_at.isoformat(),
            'updated_at': profile.updated_at.isoformat()
        }
        
        return APIResponse.success_response(data={'profile': profile_data})
    except Exception as e:
        print(f"Error in get_profile: {str(e)}")
        return APIResponse.error_response(str(e), status_code=500)


@profiles_bp.route('/<int:user_id>', methods=['PUT'])
@jwt_required()
def update_profile(user_id):
    """Update a user's profile (requires ownership only)"""
    current_user_id = get_jwt_identity()
    
    # Permission check - only allow users to update their own profile
    if int(current_user_id) != user_id:
        return APIResponse.forbidden('Permission denied. You can only update your own profile.')
    
    user = User.query.get(user_id)
    if not user:
        return APIResponse.not_found('User')
    
    data = request.get_json()
    if not data:
        return APIResponse.error_response('No data provided', status_code=400)
    
    try:
        # Get or create profile
        profile = user.profile
        if not profile:
            profile = Profile(user_id=user_id)
            db.session.add(profile)
        
        # Update fields
        allowed_fields = [
            'bio', 'profile_picture', 'phone_number', 'location',
            'university', 'degree', 'graduation_year', 'department',
            'company', 'position', 'industry', 'years_of_experience',
            'linkedin_url', 'is_mentor', 'mentorship_areas', 'availability'
        ]
        
        for field in allowed_fields:
            if field in data:
                setattr(profile, field, data[field])
        
        db.session.commit()
        
        return APIResponse.success_response(
            message='Profile updated successfully',
            data={
                'profile': {
                    'id': profile.id,
                    'user_id': profile.user_id,
                    'bio': profile.bio,
                    'profile_picture': profile.profile_picture,
                    'phone_number': profile.phone_number,
                    'location': profile.location,
                    'university': profile.university,
                    'degree': profile.degree,
                    'graduation_year': profile.graduation_year,
                    'department': profile.department,
                    'company': profile.company,
                    'position': profile.position,
                    'industry': profile.industry,
                    'years_of_experience': profile.years_of_experience,
                    'linkedin_url': profile.linkedin_url,
                    'is_mentor': profile.is_mentor,
                    'mentorship_areas': profile.mentorship_areas,
                    'availability': profile.availability,
                    'updated_at': profile.updated_at.isoformat()
                }
            }
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@profiles_bp.route('/mentors', methods=['GET'])
@jwt_required()
def get_mentors():
    """
    Get a list of mentors (alumni with is_mentor=True)
    
    Query parameters:
    - industry: Filter by industry
    - university: Filter by university
    - mentorship_area: Filter by mentorship area (searches in mentorship_areas)
    - search: Search by name
    - page: Page number (default: 1)
    - per_page: Results per page (default: 10, max: 50)
    """
    # Get query parameters
    industry = request.args.get('industry')
    university = request.args.get('university')
    mentorship_area = request.args.get('mentorship_area')
    search = request.args.get('search')
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 10)), 50)
    
    # Base query: get all alumni users who are mentors
    query = User.query.join(Profile).filter(
        User.user_type == 'alumni',
        Profile.is_mentor == True
    )
    
    # Apply filters
    if industry:
        query = query.filter(Profile.industry == industry)
    
    if university:
        query = query.filter(Profile.university == university)
    
    if mentorship_area:
        query = query.filter(Profile.mentorship_areas.like(f'%{mentorship_area}%'))
    
    if search:
        search_term = f'%{search}%'
        query = query.filter(
            (User.first_name.ilike(search_term)) | 
            (User.last_name.ilike(search_term))
        )
    
    # Execute paginated query
    mentors_pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    mentors = mentors_pagination.items
    
    # Format response
    mentors_data = []
    for mentor in mentors:
        mentor_data = {
            'id': mentor.id,
            'first_name': mentor.first_name,
            'last_name': mentor.last_name,
            'full_name': mentor.full_name
        }
        
        if mentor.profile:
            mentor_data['profile'] = {
                'bio': mentor.profile.bio,
                'location': mentor.profile.location,
                'university': mentor.profile.university,
                'degree': mentor.profile.degree,
                'department': mentor.profile.department,
                'industry': mentor.profile.industry,
                'company': mentor.profile.company,
                'position': mentor.profile.position,
                'years_of_experience': mentor.profile.years_of_experience,
                'mentorship_areas': mentor.profile.mentorship_areas
            }
        
        mentors_data.append(mentor_data)
    
    return APIResponse.success_response(
        data={
            'mentors': mentors_data,
            'pagination': {
                'total': mentors_pagination.total,
                'pages': mentors_pagination.pages,
                'page': page,
                'per_page': per_page,
            }
        }
    ) 