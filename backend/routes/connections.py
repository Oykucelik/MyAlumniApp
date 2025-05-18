from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import db, User, Connection, Notification
from sqlalchemy import or_, and_
from utils.response import APIResponse

connections_bp = Blueprint('connections', __name__)

@connections_bp.route('', methods=['POST'])
@jwt_required()
def send_connection_request():
    """Send a connection request to another user"""
    current_user_id = get_jwt_identity()
    data = request.get_json()
    
    if not data or 'recipient_id' not in data:
        return APIResponse.error_response('Recipient ID is required', status_code=400)
    
    recipient_id = data['recipient_id']
    
    # Check if recipient exists
    recipient = User.query.get(recipient_id)
    if not recipient:
        return APIResponse.not_found('Recipient')
    
    # Check if connection already exists
    existing_connection = Connection.query.filter(
        or_(
            and_(Connection.requestor_id == current_user_id, Connection.recipient_id == recipient_id),
            and_(Connection.requestor_id == recipient_id, Connection.recipient_id == current_user_id)
        )
    ).first()
    
    if existing_connection:
        status = existing_connection.status
        if status == 'accepted':
            return APIResponse.error_response('Connection already exists', status_code=400)
        elif status == 'pending':
            if existing_connection.requestor_id == current_user_id:
                return APIResponse.error_response('Connection request already sent', status_code=400)
            else:
                # Accept the connection request that was sent to the current user
                existing_connection.status = 'accepted'
                db.session.commit()
                
                # Create notifications for both users
                current_user = User.query.get(current_user_id)
                Notification.create_notification(
                    recipient_id,
                    'Connection Accepted',
                    f'{current_user.full_name} accepted your connection request.',
                    'connection',
                    current_user_id
                )
                
                return APIResponse.success_response(
                    message='Connection request accepted',
                    data={
                        'connection': {
                            'id': existing_connection.id,
                            'requestor_id': existing_connection.requestor_id,
                            'recipient_id': existing_connection.recipient_id,
                            'status': existing_connection.status,
                            'created_at': existing_connection.created_at.isoformat()
                        }
                    }
                )
    
    # Create a new connection request
    try:
        connection = Connection(
            requestor_id=current_user_id,
            recipient_id=recipient_id,
            status='pending'
        )
        db.session.add(connection)
        db.session.commit()
        
        # Create notification for recipient
        current_user = User.query.get(current_user_id)
        Notification.create_notification(
            recipient_id,
            'New Connection Request',
            f'{current_user.full_name} sent you a connection request.',
            'connection',
            current_user_id
        )
        
        return APIResponse.success_response(
            message='Connection request sent',
            data={
                'connection': {
                    'id': connection.id,
                    'requestor_id': connection.requestor_id,
                    'recipient_id': connection.recipient_id,
                    'status': connection.status,
                    'created_at': connection.created_at.isoformat()
                }
            },
            status_code=201
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@connections_bp.route('/<int:connection_id>', methods=['PUT'])
@jwt_required()
def update_connection_status(connection_id):
    """
    Update a connection status (accept or reject)
    Required JSON data: {'status': 'accepted' or 'rejected'}
    """
    current_user_id = get_jwt_identity()
    connection = Connection.query.get(connection_id)
    
    if not connection:
        return APIResponse.not_found('Connection')
    
    # Check if the current user is the recipient of the connection request
    if connection.recipient_id != current_user_id:
        return APIResponse.error_response('Permission denied', status_code=403)
    
    data = request.get_json()
    if not data or 'status' not in data:
        return APIResponse.error_response('Status is required', status_code=400)
    
    # Validate status
    new_status = data['status']
    if new_status not in ['accepted', 'rejected']:
        return APIResponse.error_response('Status must be "accepted" or "rejected"', status_code=400)
    
    # Update the status
    try:
        connection.status = new_status
        db.session.commit()
        
        # Create notification for requestor
        if new_status == 'accepted':
            current_user = User.query.get(current_user_id)
            Notification.create_notification(
                connection.requestor_id,
                'Connection Accepted',
                f'{current_user.full_name} accepted your connection request.',
                'connection',
                current_user_id
            )
        
        return APIResponse.success_response(
            message=f'Connection {new_status}',
            data={
                'connection': {
                    'id': connection.id,
                    'requestor_id': connection.requestor_id,
                    'recipient_id': connection.recipient_id,
                    'status': connection.status,
                    'updated_at': connection.updated_at.isoformat()
                }
            }
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@connections_bp.route('', methods=['GET'])
@jwt_required()
def get_connections():
    """
    Get all connections for the current user
    
    Query parameters:
    - status: Filter by status (pending, accepted, rejected)
    - page: Page number (default: 1)
    - per_page: Results per page (default: 10, max: 50)
    """
    current_user_id = get_jwt_identity()
    status = request.args.get('status')
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 10)), 50)
    
    # Build query
    query = Connection.query.filter(
        or_(
            Connection.requestor_id == current_user_id,
            Connection.recipient_id == current_user_id
        )
    )
    
    if status:
        query = query.filter(Connection.status == status)
    
    # Execute paginated query
    connections_pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    connections = connections_pagination.items
    
    # Format response with user information
    connections_data = []
    for connection in connections:
        # Determine the other user in the connection
        other_user_id = connection.recipient_id if connection.requestor_id == current_user_id else connection.requestor_id
        other_user = User.query.get(other_user_id)
        
        connection_data = {
            'id': connection.id,
            'status': connection.status,
            'created_at': connection.created_at.isoformat(),
            'updated_at': connection.updated_at.isoformat(),
            'is_requestor': connection.requestor_id == current_user_id
        }
        
        # Add other user info
        if other_user:
            connection_data['other_user'] = {
                'id': other_user.id,
                'first_name': other_user.first_name,
                'last_name': other_user.last_name,
                'full_name': other_user.full_name,
                'user_type': other_user.user_type
            }
            
            if other_user.profile:
                connection_data['other_user']['profile'] = {
                    'company': other_user.profile.company,
                    'position': other_user.profile.position,
                    'industry': other_user.profile.industry,
                    'university': other_user.profile.university
                }
        
        connections_data.append(connection_data)
    
    return APIResponse.success_response(
        message='Connections retrieved successfully',
        data={
            'connections': connections_data,
            'pagination': {
                'total': connections_pagination.total,
                'pages': connections_pagination.pages,
                'page': page,
                'per_page': per_page,
                'has_next': connections_pagination.has_next,
                'has_prev': connections_pagination.has_prev
            }
        }
    )


@connections_bp.route('/<int:connection_id>', methods=['DELETE'])
@jwt_required()
def delete_connection(connection_id):
    """Delete a connection (either user can delete it)"""
    current_user_id = get_jwt_identity()
    connection = Connection.query.get(connection_id)
    
    if not connection:
        return APIResponse.not_found('Connection')
    
    # Check if the current user is part of the connection
    if connection.requestor_id != current_user_id and connection.recipient_id != current_user_id:
        return APIResponse.error_response('Permission denied', status_code=403)
    
    try:
        db.session.delete(connection)
        db.session.commit()
        return APIResponse.success_response(message='Connection deleted successfully')
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500) 