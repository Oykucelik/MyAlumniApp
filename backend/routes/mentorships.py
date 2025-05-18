from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import db, User, Profile, Mentorship, Notification
from utils.auth import user_owns_resource
from utils.response import APIResponse

mentorships_bp = Blueprint('mentorships', __name__)

@mentorships_bp.route('', methods=['POST'])
@jwt_required()
def request_mentorship():
    """Request mentorship from an alumni (student only)"""
    current_user_id = get_jwt_identity()
    
    # Check if current user is a student
    current_user = User.query.get(current_user_id)
    if not current_user or current_user.user_type != 'student':
        return APIResponse.forbidden('Only students can request mentorship')
    
    data = request.get_json()
    if not data or 'mentor_id' not in data:
        return APIResponse.error_response('Mentor ID is required', status_code=400)
    
    mentor_id = data['mentor_id']
    
    # Check if mentor exists and is an alumni with is_mentor=True
    mentor = User.query.join(Profile).filter(
        User.id == mentor_id,
        User.user_type == 'alumni',
        Profile.is_mentor == True
    ).first()
    
    if not mentor:
        return APIResponse.not_found('Mentor not available for mentorship')
    
    # Check if mentorship already exists
    existing_mentorship = Mentorship.query.filter_by(
        mentor_id=mentor_id,
        mentee_id=current_user_id
    ).first()
    
    if existing_mentorship:
        return APIResponse.error_response(
            f'Mentorship request already exists with status: {existing_mentorship.status}', 
            status_code=400
        )
    
    # Create mentorship request
    try:
        mentorship = Mentorship(
            mentor_id=mentor_id,
            mentee_id=current_user_id,
            status='pending',
            focus_area=data.get('focus_area'),
            goals=data.get('goals')
        )
        db.session.add(mentorship)
        db.session.commit()
        
        # Create notification for mentor
        Notification.create_notification(
            mentor_id,
            'New Mentorship Request',
            f'{current_user.full_name} has requested mentorship.',
            'mentorship',
            mentorship.id
        )
        
        return APIResponse.success_response(
            message='Mentorship request sent successfully',
            data={
                'mentorship': {
                    'id': mentorship.id,
                    'mentor_id': mentorship.mentor_id,
                    'mentee_id': mentorship.mentee_id,
                    'status': mentorship.status,
                    'focus_area': mentorship.focus_area,
                    'goals': mentorship.goals,
                    'created_at': mentorship.created_at.isoformat()
                }
            },
            status_code=201
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@mentorships_bp.route('/<int:mentorship_id>', methods=['PUT'])
@jwt_required()
def update_mentorship_status(mentorship_id):
    """Update mentorship status (mentor only can accept/reject)"""
    current_user_id = get_jwt_identity()
    mentorship = Mentorship.query.get(mentorship_id)
    
    if not mentorship:
        return APIResponse.not_found('Mentorship not found')
    
    # Check if current user is the mentor
    if mentorship.mentor_id != current_user_id:
        return APIResponse.forbidden('Permission denied')
    
    data = request.get_json()
    if not data or 'status' not in data:
        return APIResponse.error_response('Status is required', status_code=400)
    
    new_status = data['status']
    if new_status not in ['active', 'rejected', 'completed']:
        return APIResponse.error_response('Invalid status', status_code=400)
    
    try:
        # Update the status
        mentorship.status = new_status
        
        # Update additional fields if provided
        if 'start_date' in data:
            mentorship.start_date = data['start_date']
        if 'end_date' in data:
            mentorship.end_date = data['end_date']
        if 'meeting_frequency' in data:
            mentorship.meeting_frequency = data['meeting_frequency']
        if 'notes' in data:
            mentorship.notes = data['notes']
        
        db.session.commit()
        
        # Create notification for mentee
        mentor = User.query.get(mentorship.mentor_id)
        status_verb = 'accepted' if new_status == 'active' else ('declined' if new_status == 'rejected' else 'completed')
        
        Notification.create_notification(
            mentorship.mentee_id,
            f'Mentorship {status_verb.capitalize()}',
            f'{mentor.full_name} has {status_verb} your mentorship request.',
            'mentorship',
            mentorship.id
        )
        
        return APIResponse.success_response(
            message=f'Mentorship {status_verb} successfully',
            data={
                'mentorship': {
                    'id': mentorship.id,
                    'status': mentorship.status,
                    'updated_at': mentorship.updated_at.isoformat()
                }
            },
            status_code=200
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@mentorships_bp.route('', methods=['GET'])
@jwt_required()
def get_mentorships():
    """
    Get all mentorships for the current user (as mentor or mentee)
    
    Query parameters:
    - role: Filter by role (mentor, mentee, all) (default: all)
    - status: Filter by status (pending, active, completed, rejected)
    - page: Page number (default: 1) 
    - per_page: Results per page (default: 10, max: 50)
    """
    current_user_id = get_jwt_identity()
    role = request.args.get('role', 'all')
    status = request.args.get('status')
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 10)), 50)
    
    # Build query based on role
    if role == 'mentor':
        query = Mentorship.query.filter_by(mentor_id=current_user_id)
    elif role == 'mentee':
        query = Mentorship.query.filter_by(mentee_id=current_user_id)
    else:  # 'all'
        query = Mentorship.query.filter(
            (Mentorship.mentor_id == current_user_id) | 
            (Mentorship.mentee_id == current_user_id)
        )
    
    # Filter by status if provided
    if status:
        query = query.filter_by(status=status)
    
    # Execute paginated query
    mentorships_pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    mentorships = mentorships_pagination.items
    
    # Format response
    mentorships_data = []
    for mentorship in mentorships:
        # Get other user info (mentor or mentee)
        is_mentor = mentorship.mentor_id == current_user_id
        other_id = mentorship.mentee_id if is_mentor else mentorship.mentor_id
        other_user = User.query.get(other_id)
        
        mentorship_data = {
            'id': mentorship.id,
            'mentor_id': mentorship.mentor_id,
            'mentee_id': mentorship.mentee_id,
            'status': mentorship.status,
            'focus_area': mentorship.focus_area,
            'goals': mentorship.goals,
            'start_date': mentorship.start_date.isoformat() if mentorship.start_date else None,
            'end_date': mentorship.end_date.isoformat() if mentorship.end_date else None,
            'meeting_frequency': mentorship.meeting_frequency,
            'notes': mentorship.notes,
            'created_at': mentorship.created_at.isoformat(),
            'updated_at': mentorship.updated_at.isoformat(),
            'is_mentor': is_mentor
        }
        
        # Add other user info
        if other_user:
            mentorship_data['other_user'] = {
                'id': other_user.id,
                'first_name': other_user.first_name,
                'last_name': other_user.last_name,
                'full_name': other_user.full_name
            }
            
            if other_user.profile:
                mentorship_data['other_user']['profile'] = {
                    'university': other_user.profile.university,
                    'degree': other_user.profile.degree,
                    'industry': other_user.profile.industry,
                    'company': other_user.profile.company,
                    'position': other_user.profile.position
                }
        
        mentorships_data.append(mentorship_data)
    
    return APIResponse.success_response(
        message='Mentorships retrieved successfully',
        data={
            'mentorships': mentorships_data,
            'pagination': {
                'total': mentorships_pagination.total,
                'pages': mentorships_pagination.pages,
                'page': page,
                'per_page': per_page,
                'has_next': mentorships_pagination.has_next,
                'has_prev': mentorships_pagination.has_prev
            }
        },
        status_code=200
    )


@mentorships_bp.route('/<int:mentorship_id>', methods=['GET'])
@jwt_required()
def get_mentorship(mentorship_id):
    """Get a specific mentorship by ID"""
    current_user_id = get_jwt_identity()
    mentorship = Mentorship.query.get(mentorship_id)
    
    if not mentorship:
        return APIResponse.not_found('Mentorship not found')
    
    # Check if user is part of this mentorship
    if mentorship.mentor_id != current_user_id and mentorship.mentee_id != current_user_id:
        return APIResponse.forbidden('Permission denied')
    
    # Format response with user information
    is_mentor = mentorship.mentor_id == current_user_id
    other_id = mentorship.mentee_id if is_mentor else mentorship.mentor_id
    other_user = User.query.get(other_id)
    
    mentorship_data = {
        'id': mentorship.id,
        'mentor_id': mentorship.mentor_id,
        'mentee_id': mentorship.mentee_id,
        'status': mentorship.status,
        'focus_area': mentorship.focus_area,
        'goals': mentorship.goals,
        'start_date': mentorship.start_date.isoformat() if mentorship.start_date else None,
        'end_date': mentorship.end_date.isoformat() if mentorship.end_date else None,
        'meeting_frequency': mentorship.meeting_frequency,
        'notes': mentorship.notes,
        'created_at': mentorship.created_at.isoformat(),
        'updated_at': mentorship.updated_at.isoformat(),
        'is_mentor': is_mentor
    }
    
    # Add other user info
    if other_user:
        mentorship_data['other_user'] = {
            'id': other_user.id,
            'first_name': other_user.first_name,
            'last_name': other_user.last_name,
            'full_name': other_user.full_name,
            'email': other_user.email
        }
        
        if other_user.profile:
            mentorship_data['other_user']['profile'] = {
                'university': other_user.profile.university,
                'degree': other_user.profile.degree,
                'graduation_year': other_user.profile.graduation_year,
                'industry': other_user.profile.industry,
                'company': other_user.profile.company,
                'position': other_user.profile.position,
                'bio': other_user.profile.bio,
                'phone_number': other_user.profile.phone_number,
                'linkedin_url': other_user.profile.linkedin_url
            }
    
    # Get mentor info
    mentor = User.query.get(mentorship.mentor_id)
    if mentor and mentor.profile:
        mentorship_data['mentor_details'] = {
            'full_name': mentor.full_name,
            'company': mentor.profile.company,
            'position': mentor.profile.position,
            'industry': mentor.profile.industry,
            'years_of_experience': mentor.profile.years_of_experience,
            'mentorship_areas': mentor.profile.mentorship_areas
        }
    
    return APIResponse.success_response(
        message='Mentorship retrieved successfully',
        data={
            'mentorship': mentorship_data
        },
        status_code=200
    ) 