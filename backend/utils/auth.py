from functools import wraps
from flask import request, jsonify, current_app
from flask_jwt_extended import verify_jwt_in_request, get_jwt_identity
from models import User

def admin_required():
    """
    Decorator to require admin privileges for a route
    """
    def wrapper(fn):
        @wraps(fn)
        def decorator(*args, **kwargs):
            verify_jwt_in_request()
            user_id = get_jwt_identity()
            user = User.query.get(user_id)
            
            if not user or user.user_type != 'admin':
                return jsonify({'error': 'Admin privileges required'}), 403
            return fn(*args, **kwargs)
        return decorator
    return wrapper

def alumni_required():
    """
    Decorator to require alumni status for a route
    """
    def wrapper(fn):
        @wraps(fn)
        def decorator(*args, **kwargs):
            verify_jwt_in_request()
            user_id = get_jwt_identity()
            user = User.query.get(user_id)
            
            if not user or user.user_type != 'alumni':
                return jsonify({'error': 'Alumni status required'}), 403
            return fn(*args, **kwargs)
        return decorator
    return wrapper

def get_current_user():
    """
    Helper function to get the current authenticated user
    """
    verify_jwt_in_request()
    user_id = get_jwt_identity()
    return User.query.get(user_id)

def user_owns_resource(model, resource_id, user_id=None):
    """
    Check if a user owns a particular resource
    
    Args:
        model: The SQLAlchemy model class
        resource_id: The ID of the resource to check
        user_id: The user ID to check against (if None, uses the authenticated user)
        
    Returns:
        bool: True if the user owns the resource, False otherwise
    """
    if user_id is None:
        verify_jwt_in_request()
        user_id = get_jwt_identity()
    
    resource = model.query.get(resource_id)
    if not resource:
        return False
    
    # Check different ownership attributes based on model
    if hasattr(resource, 'user_id'):
        return resource.user_id == user_id
    elif hasattr(resource, 'creator_id'):
        return resource.creator_id == user_id
    elif hasattr(resource, 'poster_id'):
        return resource.poster_id == user_id
    elif hasattr(resource, 'sender_id'):
        return resource.sender_id == user_id
    
    return False 