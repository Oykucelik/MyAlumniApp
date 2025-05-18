from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import db, User, Notification
from utils.auth import user_owns_resource
from utils.response import APIResponse

notifications_bp = Blueprint('notifications', __name__)

@notifications_bp.route('', methods=['GET'])
@jwt_required()
def get_notifications():
    """
    Get notifications for the current user
    
    Query parameters:
    - is_read: Filter by read status (true/false)
    - type: Filter by notification type (connection, message, event, job, mentorship)
    - page: Page number (default: 1)
    - per_page: Results per page (default: 20, max: 50)
    """
    current_user_id = get_jwt_identity()
    is_read = request.args.get('is_read')
    notification_type = request.args.get('type')
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 20)), 50)
    
    # Build query
    query = Notification.query.filter_by(user_id=current_user_id)
    
    if is_read is not None:
        is_read_bool = is_read.lower() == 'true'
        query = query.filter(Notification.is_read == is_read_bool)
    
    if notification_type:
        query = query.filter(Notification.notification_type == notification_type)
    
    # Order by creation date (newest first)
    query = query.order_by(Notification.created_at.desc())
    
    # Execute paginated query
    notifications_pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    notifications = notifications_pagination.items
    
    # Format response
    notifications_data = []
    for notification in notifications:
        notifications_data.append({
            'id': notification.id,
            'title': notification.title,
            'message': notification.message,
            'notification_type': notification.notification_type,
            'related_id': notification.related_id,
            'is_read': notification.is_read,
            'created_at': notification.created_at.isoformat()
        })
    
    # Get unread count for badge
    unread_count = Notification.query.filter_by(
        user_id=current_user_id,
        is_read=False
    ).count()
    
    return APIResponse.success_response(
        data={
            'notifications': notifications_data,
            'unread_count': unread_count,
            'pagination': {
                'total': notifications_pagination.total,
                'pages': notifications_pagination.pages,
                'page': page,
                'per_page': per_page
            }
        }
    )


@notifications_bp.route('/<int:notification_id>/read', methods=['PUT'])
@jwt_required()
def mark_as_read(notification_id):
    """Mark a notification as read"""
    current_user_id = get_jwt_identity()
    notification = Notification.query.get(notification_id)
    
    if not notification:
        return APIResponse.not_found('Notification')
    
    # Check if notification belongs to the current user
    if notification.user_id != current_user_id:
        return APIResponse.forbidden('Permission denied')
    
    # Mark as read
    if not notification.is_read:
        result = Notification.mark_as_read(notification_id)
        if not result:
            return APIResponse.error_response('Failed to mark notification as read', status_code=500)
    
    return APIResponse.success_response(
        message='Notification marked as read',
        data={
            'notification': {
                'id': notification.id,
                'is_read': True
            }
        }
    )


@notifications_bp.route('/read-all', methods=['PUT'])
@jwt_required()
def mark_all_as_read():
    """Mark all notifications as read for the current user"""
    current_user_id = get_jwt_identity()
    
    try:
        Notification.mark_all_as_read(current_user_id)
        return APIResponse.success_response(message='All notifications marked as read')
        
    except Exception as e:
        return APIResponse.error_response(str(e), status_code=500)


@notifications_bp.route('/<int:notification_id>', methods=['DELETE'])
@jwt_required()
def delete_notification(notification_id):
    """Delete a notification"""
    current_user_id = get_jwt_identity()
    notification = Notification.query.get(notification_id)
    
    if not notification:
        return APIResponse.not_found('Notification')
    
    # Check if notification belongs to the current user
    if notification.user_id != current_user_id:
        return APIResponse.forbidden('Permission denied')
    
    try:
        db.session.delete(notification)
        db.session.commit()
        return APIResponse.success_response(message='Notification deleted successfully')
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@notifications_bp.route('/delete-all', methods=['DELETE'])
@jwt_required()
def delete_all_notifications():
    """Delete all notifications for the current user"""
    current_user_id = get_jwt_identity()
    
    try:
        # Delete all notifications for the current user
        notifications = Notification.query.filter_by(user_id=current_user_id).all()
        for notification in notifications:
            db.session.delete(notification)
        
        db.session.commit()
        return APIResponse.success_response(message='All notifications deleted successfully')
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500) 