from datetime import datetime
from . import db

class Event(db.Model):
    """Model for career events, workshops, and networking meetups"""
    __tablename__ = 'events'
    
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(255), nullable=False)
    description = db.Column(db.Text, nullable=False)
    event_type = db.Column(db.String(50), nullable=False)  # webinar, workshop, networking, etc.
    location = db.Column(db.String(255), nullable=True)  # Physical location or "Virtual"
    virtual_meeting_link = db.Column(db.String(255), nullable=True)
    start_datetime = db.Column(db.DateTime, nullable=False)
    end_datetime = db.Column(db.DateTime, nullable=False)
    max_attendees = db.Column(db.Integer, nullable=True)
    creator_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    is_approved = db.Column(db.Boolean, default=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    attendees = db.relationship('EventAttendee', backref='event', cascade='all, delete-orphan')
    
    @property
    def is_upcoming(self):
        """Check if the event is in the future"""
        return self.start_datetime > datetime.utcnow()
    
    @property
    def current_attendees_count(self):
        """Get the current number of attendees"""
        return EventAttendee.query.filter_by(event_id=self.id).count()
    
    @property
    def is_full(self):
        """Check if the event has reached max attendees"""
        if not self.max_attendees:
            return False
        return self.current_attendees_count >= self.max_attendees
    
    def __repr__(self):
        return f"<Event {self.title} on {self.start_datetime}>"


class EventAttendee(db.Model):
    """Association table for users attending events"""
    __tablename__ = 'event_attendees'
    
    id = db.Column(db.Integer, primary_key=True)
    event_id = db.Column(db.Integer, db.ForeignKey('events.id'), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    status = db.Column(db.String(20), nullable=False, default='registered')  # registered, attended, cancelled
    registration_datetime = db.Column(db.DateTime, default=datetime.utcnow)
    
    __table_args__ = (
        db.UniqueConstraint('event_id', 'user_id', name='uix_event_attendee'),
    )
    
    def __repr__(self):
        return f"<EventAttendee event_id={self.event_id} user_id={self.user_id}>" 