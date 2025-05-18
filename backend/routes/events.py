from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import db, User, Event, EventAttendee, Notification
from utils.auth import alumni_required, user_owns_resource
from datetime import datetime
from utils.response import APIResponse

events_bp = Blueprint('events', __name__)

@events_bp.route('', methods=['POST'])
@jwt_required()
@alumni_required()
def create_event():
    """Create a new event (alumni only)"""
    current_user_id = get_jwt_identity()
    data = request.get_json()
    
    # Validate required fields
    required_fields = ['title', 'description', 'event_type', 'start_datetime', 'end_datetime']
    for field in required_fields:
        if field not in data:
            return APIResponse.error_response(f'Missing required field: {field}', status_code=400)
    
    # Validate dates
    try:
        start_datetime = datetime.fromisoformat(data['start_datetime'].replace('Z', '+00:00'))
        end_datetime = datetime.fromisoformat(data['end_datetime'].replace('Z', '+00:00'))
        
        if start_datetime >= end_datetime:
            return APIResponse.error_response('End time must be after start time', status_code=400)
        
        if start_datetime < datetime.utcnow():
            return APIResponse.error_response('Event cannot be scheduled in the past', status_code=400)
    except ValueError:
        return APIResponse.error_response('Invalid datetime format', status_code=400)
    
    # Create the event
    try:
        event = Event(
            title=data['title'],
            description=data['description'],
            event_type=data['event_type'],
            location=data.get('location'),
            virtual_meeting_link=data.get('virtual_meeting_link'),
            start_datetime=start_datetime,
            end_datetime=end_datetime,
            max_attendees=data.get('max_attendees'),
            creator_id=current_user_id
        )
        
        db.session.add(event)
        db.session.commit()
        
        return APIResponse.success_response(
            message='Event created successfully',
            data={
                'event': {
                    'id': event.id,
                    'title': event.title,
                    'description': event.description,
                    'event_type': event.event_type,
                    'location': event.location,
                    'virtual_meeting_link': event.virtual_meeting_link,
                    'start_datetime': event.start_datetime.isoformat(),
                    'end_datetime': event.end_datetime.isoformat(),
                    'max_attendees': event.max_attendees,
                    'creator_id': event.creator_id,
                    'created_at': event.created_at.isoformat()
                }
            },
            status_code=201
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@events_bp.route('', methods=['GET'])
@jwt_required()
def get_events():
    """
    Get a list of events with optional filtering
    
    Query parameters:
    - event_type: Filter by event type (webinar, workshop, networking, etc.)
    - upcoming: If 'true', show only upcoming events
    - search: Search by title or description
    - page: Page number (default: 1)
    - per_page: Results per page (default: 10, max: 50)
    """
    event_type = request.args.get('event_type')
    upcoming = request.args.get('upcoming', 'false').lower() == 'true'
    search = request.args.get('search')
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 10)), 50)
    
    # Base query
    query = Event.query
    
    # Apply filters
    if event_type:
        query = query.filter(Event.event_type == event_type)
    
    if upcoming:
        query = query.filter(Event.start_datetime > datetime.utcnow())
    
    if search:
        search_term = f'%{search}%'
        query = query.filter(
            (Event.title.ilike(search_term)) |
            (Event.description.ilike(search_term))
        )
    
    # Order by start date (soonest first)
    query = query.order_by(Event.start_datetime)
    
    # Execute paginated query
    events_pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    events = events_pagination.items
    
    # Format response
    events_data = []
    for event in events:
        creator = User.query.get(event.creator_id)
        creator_name = creator.full_name if creator else "Unknown"
        
        event_data = {
            'id': event.id,
            'title': event.title,
            'description': event.description,
            'event_type': event.event_type,
            'location': event.location,
            'start_datetime': event.start_datetime.isoformat(),
            'end_datetime': event.end_datetime.isoformat(),
            'max_attendees': event.max_attendees,
            'current_attendees': event.current_attendees_count,
            'is_full': event.is_full,
            'is_upcoming': event.is_upcoming,
            'creator_id': event.creator_id,
            'creator_name': creator_name,
            'created_at': event.created_at.isoformat()
        }
        
        events_data.append(event_data)
    
    return APIResponse.success_response(
        data={
            'events': events_data,
            'pagination': {
                'total': events_pagination.total,
                'pages': events_pagination.pages,
                'page': page,
                'per_page': per_page
            }
        }
    )


@events_bp.route('/<int:event_id>', methods=['GET'])
@jwt_required()
def get_event(event_id):
    """Get a specific event by ID"""
    event = Event.query.get(event_id)
    
    if not event:
        return APIResponse.not_found('Event')
    
    # Get creator information
    creator = User.query.get(event.creator_id)
    creator_name = creator.full_name if creator else "Unknown"
    
    # Format response
    event_data = {
        'id': event.id,
        'title': event.title,
        'description': event.description,
        'event_type': event.event_type,
        'location': event.location,
        'virtual_meeting_link': event.virtual_meeting_link,
        'start_datetime': event.start_datetime.isoformat(),
        'end_datetime': event.end_datetime.isoformat(),
        'max_attendees': event.max_attendees,
        'current_attendees': event.current_attendees_count,
        'is_full': event.is_full,
        'is_upcoming': event.is_upcoming,
        'creator_id': event.creator_id,
        'creator_name': creator_name,
        'created_at': event.created_at.isoformat(),
        'updated_at': event.updated_at.isoformat()
    }
    
    # Get current user's RSVP status if any
    current_user_id = get_jwt_identity()
    attendee = EventAttendee.query.filter_by(
        event_id=event_id,
        user_id=current_user_id
    ).first()
    
    if attendee:
        event_data['user_rsvp'] = {
            'status': attendee.status,
            'registration_datetime': attendee.registration_datetime.isoformat()
        }
    
    return APIResponse.success_response(data={'event': event_data})


@events_bp.route('/<int:event_id>', methods=['PUT'])
@jwt_required()
def update_event(event_id):
    """Update an event (creator only)"""
    current_user_id = get_jwt_identity()
    event = Event.query.get(event_id)
    
    if not event:
        return APIResponse.not_found('Event')
    
    # Check if current user is the creator
    if event.creator_id != current_user_id:
        return APIResponse.error_response('Permission denied', status_code=403)
    
    # Check if event has already happened
    if event.start_datetime <= datetime.utcnow():
        return APIResponse.error_response('Cannot update past events', status_code=400)
    
    data = request.get_json()
    if not data:
        return APIResponse.error_response('No data provided', status_code=400)
    
    try:
        # Update allowed fields
        allowed_fields = [
            'title', 'description', 'event_type', 'location', 
            'virtual_meeting_link', 'max_attendees'
        ]
        
        for field in allowed_fields:
            if field in data:
                setattr(event, field, data[field])
        
        # Handle date updates separately for validation
        if 'start_datetime' in data:
            start_datetime = datetime.fromisoformat(data['start_datetime'].replace('Z', '+00:00'))
            
            # Validate start datetime is in future
            if start_datetime < datetime.utcnow():
                return APIResponse.error_response('Event cannot be scheduled in the past', status_code=400)
            
            # If end datetime is also provided, validate
            if 'end_datetime' in data:
                end_datetime = datetime.fromisoformat(data['end_datetime'].replace('Z', '+00:00'))
                if start_datetime >= end_datetime:
                    return APIResponse.error_response('End time must be after start time', status_code=400)
                event.end_datetime = end_datetime
            # If only start is provided, ensure it's before existing end
            elif start_datetime >= event.end_datetime:
                return APIResponse.error_response('Start time must be before end time', status_code=400)
            
            event.start_datetime = start_datetime
            
        # If only end datetime is provided
        elif 'end_datetime' in data:
            end_datetime = datetime.fromisoformat(data['end_datetime'].replace('Z', '+00:00'))
            if event.start_datetime >= end_datetime:
                return APIResponse.error_response('End time must be after start time', status_code=400)
            event.end_datetime = end_datetime
        
        db.session.commit()
        
        # Notify registered attendees of the update
        for attendee in event.attendees:
            Notification.create_notification(
                attendee.user_id,
                'Event Updated',
                f'The event "{event.title}" has been updated.',
                'event',
                event.id
            )
        
        return APIResponse.success_response(
            message='Event updated successfully',
            data={
                'event': {
                    'id': event.id,
                    'title': event.title,
                    'updated_at': event.updated_at.isoformat()
                }
            }
        )
        
    except ValueError:
        return APIResponse.error_response('Invalid datetime format', status_code=400)
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@events_bp.route('/<int:event_id>', methods=['DELETE'])
@jwt_required()
def delete_event(event_id):
    """Delete an event (creator only)"""
    current_user_id = get_jwt_identity()
    event = Event.query.get(event_id)
    
    if not event:
        return APIResponse.not_found('Event')
    
    # Check if current user is the creator
    if event.creator_id != current_user_id:
        return APIResponse.error_response('Permission denied', status_code=403)
    
    try:
        # Notify registered attendees of cancellation
        for attendee in event.attendees:
            Notification.create_notification(
                attendee.user_id,
                'Event Cancelled',
                f'The event "{event.title}" has been cancelled.',
                'event',
                event.id
            )
        
        db.session.delete(event)
        db.session.commit()
        
        return APIResponse.success_response(message='Event deleted successfully')
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@events_bp.route('/<int:event_id>/rsvp', methods=['POST'])
@jwt_required()
def rsvp_event(event_id):
    """RSVP to an event"""
    current_user_id = get_jwt_identity()
    event = Event.query.get(event_id)
    
    if not event:
        return APIResponse.not_found('Event')
    
    # Check if event is in the future
    if not event.is_upcoming:
        return APIResponse.error_response('Cannot RSVP to past events', status_code=400)
    
    # Check if event is full
    if event.is_full:
        return APIResponse.error_response('Event is already full', status_code=400)
    
    # Check if user already RSVP'd
    existing_rsvp = EventAttendee.query.filter_by(
        event_id=event_id,
        user_id=current_user_id
    ).first()
    
    if existing_rsvp:
        if existing_rsvp.status == 'registered':
            return APIResponse.error_response('You are already registered for this event', status_code=400)
        elif existing_rsvp.status == 'cancelled':
            # Update from cancelled to registered
            existing_rsvp.status = 'registered'
            existing_rsvp.registration_datetime = datetime.utcnow()
            db.session.commit()
            
            return APIResponse.success_response(
                message='RSVP updated successfully',
                data={
                    'rsvp': {
                        'id': existing_rsvp.id,
                        'event_id': existing_rsvp.event_id,
                        'user_id': existing_rsvp.user_id,
                        'status': existing_rsvp.status,
                        'registration_datetime': existing_rsvp.registration_datetime.isoformat()
                    }
                }
            )
    
    # Create new RSVP
    try:
        attendee = EventAttendee(
            event_id=event_id,
            user_id=current_user_id,
            status='registered',
            registration_datetime=datetime.utcnow()
        )
        
        db.session.add(attendee)
        db.session.commit()
        
        # Send notification to creator
        user = User.query.get(current_user_id)
        Notification.create_notification(
            event.creator_id,
            'New Event RSVP',
            f'{user.full_name} has registered for your event "{event.title}".',
            'event',
            event.id
        )
        
        return APIResponse.success_response(
            message='RSVP successful',
            data={
                'rsvp': {
                    'id': attendee.id,
                    'event_id': attendee.event_id,
                    'user_id': attendee.user_id,
                    'status': attendee.status,
                    'registration_datetime': attendee.registration_datetime.isoformat()
                }
            }
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@events_bp.route('/<int:event_id>/rsvp/cancel', methods=['POST'])
@jwt_required()
def cancel_rsvp(event_id):
    """Cancel RSVP to an event"""
    current_user_id = get_jwt_identity()
    
    # Check if RSVP exists
    attendee = EventAttendee.query.filter_by(
        event_id=event_id,
        user_id=current_user_id
    ).first()
    
    if not attendee:
        return APIResponse.not_found('RSVP')
    
    if attendee.status == 'cancelled':
        return APIResponse.error_response('RSVP already cancelled', status_code=400)
    
    # Update RSVP status
    try:
        attendee.status = 'cancelled'
        db.session.commit()
        
        # Send notification to creator
        event = Event.query.get(event_id)
        user = User.query.get(current_user_id)
        
        Notification.create_notification(
            event.creator_id,
            'Event RSVP Cancellation',
            f'{user.full_name} has cancelled their registration for your event "{event.title}".',
            'event',
            event.id
        )
        
        return APIResponse.success_response(
            message='RSVP cancelled successfully',
            data={
                'rsvp': {
                    'id': attendee.id,
                    'event_id': attendee.event_id,
                    'user_id': attendee.user_id,
                    'status': attendee.status
                }
            }
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@events_bp.route('/<int:event_id>/attendees', methods=['GET'])
@jwt_required()
def get_event_attendees(event_id):
    """
    Get a list of attendees for an event (creator or admin only)
    
    Query parameters:
    - status: Filter by status (registered, attended, cancelled)
    - page: Page number (default: 1)
    - per_page: Results per page (default: 20, max: 100)
    """
    current_user_id = get_jwt_identity()
    event = Event.query.get(event_id)
    
    if not event:
        return APIResponse.not_found('Event')
    
    # Check if current user is the creator or an admin
    current_user = User.query.get(current_user_id)
    if event.creator_id != current_user_id and current_user.user_type != 'admin':
        return APIResponse.error_response('Permission denied', status_code=403)
    
    # Get query parameters
    status = request.args.get('status')
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 20)), 100)
    
    # Build query
    query = EventAttendee.query.filter_by(event_id=event_id)
    
    if status:
        query = query.filter_by(status=status)
    
    # Execute paginated query
    attendees_pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    attendees = attendees_pagination.items
    
    # Format response
    attendees_data = []
    for attendee in attendees:
        user = User.query.get(attendee.user_id)
        if not user:
            continue
        
        attendee_data = {
            'id': attendee.id,
            'user_id': attendee.user_id,
            'status': attendee.status,
            'registration_datetime': attendee.registration_datetime.isoformat(),
            'user': {
                'first_name': user.first_name,
                'last_name': user.last_name,
                'full_name': user.full_name,
                'email': user.email
            }
        }
        
        if user.profile:
            attendee_data['user']['profile'] = {
                'university': user.profile.university,
                'company': user.profile.company,
                'position': user.profile.position
            }
        
        attendees_data.append(attendee_data)
    
    return APIResponse.success_response(
        data={
            'attendees': attendees_data,
            'pagination': {
                'total': attendees_pagination.total,
                'pages': attendees_pagination.pages,
                'page': page,
                'per_page': per_page
            }
        }
    ) 