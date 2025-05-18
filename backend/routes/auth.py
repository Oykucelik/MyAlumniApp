from flask import Blueprint, request, jsonify
from flask_jwt_extended import create_access_token, jwt_required, get_jwt_identity, get_jwt
from datetime import datetime, timedelta
from models import db, User, Profile
from werkzeug.security import check_password_hash
from utils import APIResponse

auth_bp = Blueprint('auth', __name__)

@auth_bp.route('/register', methods=['POST'])
def register():
    """Register a new user"""
    data = request.get_json()
    
    # Check required fields
    required_fields = ['email', 'password', 'first_name', 'last_name']
    for field in required_fields:
        if field not in data:
            return APIResponse.error_response(f'Missing required field: {field}', status_code=400)
    
    # Check if email already exists
    if User.query.filter_by(email=data['email']).first():
        return APIResponse.error_response('Email already registered', status_code=409)
    
    # Create new user
    user_type = data.get('user_type', 'student')
    if user_type not in ['student', 'alumni']:
        return APIResponse.error_response('Invalid user type, must be student or alumni', status_code=400)
    
    try:
        user = User(
            email=data['email'],
            password=data['password'],
            first_name=data['first_name'],
            last_name=data['last_name'],
            user_type=user_type
        )
        db.session.add(user)
        db.session.flush()  # Flush to get the user ID before creating profile
        
        # Create an empty profile for the user
        profile = Profile(user_id=user.id)
        db.session.add(profile)
        
        db.session.commit()
        
        # Create token and return user data
        access_token = create_access_token(identity=str(user.id))
        
        return APIResponse.success_response(
            data={
                'user': {
                    'id': user.id,
                    'email': user.email,
                    'first_name': user.first_name,
                    'last_name': user.last_name,
                    'user_type': user.user_type
                },
                'access_token': access_token
            },
            message='User registered successfully with empty profile',
            status_code=201
        )
    
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@auth_bp.route('/login', methods=['POST'])
def login():
    """Login a user and return an access token"""
    data = request.get_json()
    
    # Check required fields
    if not data or 'email' not in data or 'password' not in data:
        return APIResponse.error_response('Email and password are required', status_code=400)
    
    # Verify credentials
    user = User.query.filter_by(email=data['email']).first()
    
    if not user or not user.verify_password(data['password']):
        return APIResponse.unauthorized('Invalid email or password')
    
    if not user.is_active:
        return APIResponse.forbidden('Account is deactivated')
    
    # Update last login time
    user.update_last_login()
    
    # Create token and return user data
    access_token = create_access_token(identity=str(user.id))
    
    user_data = {
        'id': user.id,
        'email': user.email,
        'first_name': user.first_name,
        'last_name': user.last_name,
        'user_type': user.user_type
    }
    
    # Include profile data if available
    if user.profile:
        user_data['profile'] = {
            'id': user.profile.id,
            'bio': user.profile.bio,
            'profile_picture': user.profile.profile_picture,
            'location': user.profile.location,
            'university': user.profile.university,
            'degree': user.profile.degree,
            'department': user.profile.department,
            'position': user.profile.position,
            'industry': user.profile.industry,
            'is_mentor': user.profile.is_mentor
        }
    
    return APIResponse.success_response(
        data={
            'user': user_data,
            'access_token': access_token
        },
        message='Login successful'
    )


@auth_bp.route('/me', methods=['GET'])
@jwt_required()
def get_current_user():
    """Get the current authenticated user"""
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    
    if not user:
        return APIResponse.not_found('User')
    
    user_data = {
        'id': user.id,
        'email': user.email,
        'first_name': user.first_name,
        'last_name': user.last_name,
        'user_type': user.user_type,
        'is_verified': user.is_verified,
        'created_at': user.created_at.isoformat(),
        'last_login': user.last_login.isoformat() if user.last_login else None
    }
    
    # Include profile data if available
    if user.profile:
        user_data['profile'] = {
            'id': user.profile.id,
            'bio': user.profile.bio,
            'profile_picture': user.profile.profile_picture,
            'phone_number': user.profile.phone_number, 
            'location': user.profile.location,
            'university': user.profile.university,
            'degree': user.profile.degree,
            'graduation_year': user.profile.graduation_year,
            'department': user.profile.department,
            'company': user.profile.company,
            'position': user.profile.position,
            'industry': user.profile.industry,
            'years_of_experience': user.profile.years_of_experience,
            'linkedin_url': user.profile.linkedin_url,
            'is_mentor': user.profile.is_mentor,
            'mentorship_areas': user.profile.mentorship_areas,
            'availability': user.profile.availability
        }
    
    return APIResponse.success_response(
        data={
            'user': user_data
        }
    )


@auth_bp.route('/change-password', methods=['PUT'])
@jwt_required()
def change_password():
    """Change the current user's password"""
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    
    if not user:
        return APIResponse.not_found('User')
    
    data = request.get_json()
    
    if not data or 'current_password' not in data or 'new_password' not in data:
        return APIResponse.error_response('Current password and new password are required', status_code=400)
    
    if not user.verify_password(data['current_password']):
        return APIResponse.unauthorized('Current password is incorrect')
    
    user.password = data['new_password']
    db.session.commit()
    
    return APIResponse.success_response(message='Password changed successfully')


@auth_bp.route('/logout', methods=['POST'])
@jwt_required()
def logout():
    """Logout the current user"""
    jti = get_jwt()["jti"]
    # Note: In a production system, you would add this token to a blocklist
    # to properly invalidate it. This would require a persistent store like Redis.
    
    return APIResponse.success_response(
        message='Successfully logged out',
        status_code=200
    ) 