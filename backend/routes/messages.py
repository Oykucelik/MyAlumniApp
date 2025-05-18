from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import db, User, Message, Connection, Notification
from sqlalchemy import desc
from utils.response import APIResponse

messages_bp = Blueprint('messages', __name__)

@messages_bp.route('', methods=['POST'])
@jwt_required()
def send_message():
    """Send a message to another user"""
    current_user_id = get_jwt_identity()
    data = request.get_json()
    
    if not data or 'receiver_id' not in data or 'content' not in data:
        return APIResponse.error_response('Receiver ID and message content are required', status_code=400)
    
    receiver_id = data['receiver_id']
    content = data['content']
    
    # Check if receiver exists
    receiver = User.query.get(receiver_id)
    if not receiver:
        return APIResponse.not_found('Receiver')
    
    # Check if users are connected (optional, comment out if not needed)
    if not Connection.are_connected(current_user_id, receiver_id):
        return APIResponse.forbidden('You must be connected with this user to send messages')
    
    # Create the message
    try:
        message = Message(
            sender_id=current_user_id,
            receiver_id=receiver_id,
            content=content
        )
        db.session.add(message)
        db.session.commit()
        
        # Create notification for receiver
        sender = User.query.get(current_user_id)
        Notification.create_notification(
            receiver_id,
            'New Message',
            f'You have a new message from {sender.full_name}.',
            'message',
            current_user_id
        )
        
        return APIResponse.success_response(
            message='Message sent successfully',
            data={
                'message_data': {
                    'id': message.id,
                    'sender_id': message.sender_id,
                    'receiver_id': message.receiver_id,
                    'content': message.content,
                    'is_read': message.is_read,
                    'created_at': message.created_at.isoformat()
                }
            },
            status_code=201
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@messages_bp.route('/conversations', methods=['GET'])
@jwt_required()
def get_conversations():
    """
    Get all conversations for the current user
    
    Query parameters:
    - page: Page number (default: 1)
    - per_page: Results per page (default: 10, max: 50)
    """
    current_user_id = get_jwt_identity()
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 10)), 50)
    
    # Get unique conversation partners
    sent_messages = Message.query.filter_by(sender_id=current_user_id)
    received_messages = Message.query.filter_by(receiver_id=current_user_id)
    
    # Combine the queries and get unique user IDs
    conversation_partners = set()
    for message in sent_messages:
        conversation_partners.add(message.receiver_id)
    for message in received_messages:
        conversation_partners.add(message.sender_id)
    
    # Convert to list and paginate manually
    conversation_partners = list(conversation_partners)
    
    # Calculate pagination
    total = len(conversation_partners)
    pages = (total + per_page - 1) // per_page
    start = (page - 1) * per_page
    end = min(start + per_page, total)
    
    paginated_partners = conversation_partners[start:end]
    
    # Build response
    conversations = []
    for partner_id in paginated_partners:
        partner = User.query.get(partner_id)
        if not partner:
            continue
        
        # Get the latest message
        latest_message = Message.query.filter(
            ((Message.sender_id == current_user_id) & (Message.receiver_id == partner_id)) |
            ((Message.sender_id == partner_id) & (Message.receiver_id == current_user_id))
        ).order_by(desc(Message.created_at)).first()
        
        if not latest_message:
            continue
        
        # Count unread messages
        unread_count = Message.query.filter_by(
            sender_id=partner_id,
            receiver_id=current_user_id,
            is_read=False
        ).count()
        
        conversation = {
            'partner': {
                'id': partner.id,
                'first_name': partner.first_name,
                'last_name': partner.last_name,
                'full_name': partner.full_name
            },
            'latest_message': {
                'id': latest_message.id,
                'sender_id': latest_message.sender_id,
                'content': latest_message.content,
                'is_read': latest_message.is_read,
                'created_at': latest_message.created_at.isoformat()
            },
            'unread_count': unread_count
        }
        
        conversations.append(conversation)
    
    return APIResponse.success_response(
        data={
            'conversations': conversations,
            'pagination': {
                'total': total,
                'pages': pages,
                'page': page,
                'per_page': per_page,
                'has_next': page < pages,
                'has_prev': page > 1
            }
        }
    )


@messages_bp.route('/conversation/<int:user_id>', methods=['GET'])
@jwt_required()
def get_conversation(user_id):
    """
    Get conversation messages between current user and specified user
    
    Query parameters:
    - page: Page number (default: 1)
    - per_page: Results per page (default: 20, max: 50)
    """
    current_user_id = get_jwt_identity()
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 20)), 50)
    
    # Check if other user exists
    other_user = User.query.get(user_id)
    if not other_user:
        return APIResponse.not_found('User')
    
    # Check if users are connected (optional, comment out if not needed)
    if not Connection.are_connected(current_user_id, user_id):
        return APIResponse.forbidden('You must be connected with this user to view messages')
    
    # Get messages between users, ordered by time (newest first)
    messages_pagination = Message.query.filter(
        ((Message.sender_id == current_user_id) & (Message.receiver_id == user_id)) |
        ((Message.sender_id == user_id) & (Message.receiver_id == current_user_id))
    ).order_by(desc(Message.created_at)).paginate(page=page, per_page=per_page, error_out=False)
    
    messages = messages_pagination.items
    
    # Mark received messages as read
    Message.mark_conversation_as_read(user_id, current_user_id)
    
    # Format response
    messages_data = []
    for message in messages:
        messages_data.append({
            'id': message.id,
            'sender_id': message.sender_id,
            'receiver_id': message.receiver_id,
            'content': message.content,
            'is_read': message.is_read,
            'created_at': message.created_at.isoformat()
        })
    
    return APIResponse.success_response(
        data={
            'messages': messages_data,
            'pagination': {
                'total': messages_pagination.total,
                'pages': messages_pagination.pages,
                'page': page,
                'per_page': per_page
            },
            'other_user': {
                'id': other_user.id,
                'first_name': other_user.first_name,
                'last_name': other_user.last_name,
                'full_name': other_user.full_name
            }
        }
    )


@messages_bp.route('/<int:message_id>/read', methods=['PUT'])
@jwt_required()
def mark_as_read(message_id):
    """Mark a message as read"""
    current_user_id = get_jwt_identity()
    message = Message.query.get(message_id)
    
    if not message:
        return APIResponse.not_found('Message')
    
    # Check if user is the recipient
    if message.receiver_id != current_user_id:
        return APIResponse.forbidden('You can only mark messages sent to you as read')
    
    # Update message status
    if not message.is_read:
        message.is_read = True
        db.session.commit()
    
    return APIResponse.success_response(
        message='Message marked as read',
        data={
            'message': {
                'id': message.id,
                'is_read': message.is_read
            }
        }
    )


@messages_bp.route('/<int:message_id>', methods=['DELETE'])
@jwt_required()
def delete_message(message_id):
    """Delete a message"""
    current_user_id = get_jwt_identity()
    message = Message.query.get(message_id)
    
    if not message:
        return APIResponse.not_found('Message')
    
    # Check if user is sender or receiver
    if message.sender_id != current_user_id and message.receiver_id != current_user_id:
        return APIResponse.forbidden('You can only delete messages you sent or received')
    
    try:
        db.session.delete(message)
        db.session.commit()
        return APIResponse.success_response(message='Message deleted successfully')
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500) 