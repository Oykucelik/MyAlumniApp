from datetime import datetime
from . import db

class Notification(db.Model):
    """Model for user notifications"""
    __tablename__ = 'notifications'
    
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    title = db.Column(db.String(100), nullable=False)
    message = db.Column(db.Text, nullable=False)
    notification_type = db.Column(db.String(50), nullable=False)  # connection, message, event, job, mentorship
    related_id = db.Column(db.Integer, nullable=True)  # ID of related entity (job_id, event_id, etc.)
    is_read = db.Column(db.Boolean, default=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    @classmethod
    def create_notification(cls, user_id, title, message, notification_type, related_id=None):
        """Helper method to create a notification"""
        notification = cls(
            user_id=user_id,
            title=title,
            message=message,
            notification_type=notification_type,
            related_id=related_id
        )
        db.session.add(notification)
        db.session.commit()
        return notification
    
    @classmethod
    def mark_as_read(cls, notification_id):
        """Mark a notification as read"""
        notification = cls.query.get(notification_id)
        if notification:
            notification.is_read = True
            db.session.commit()
            return True
        return False
    
    @classmethod
    def mark_all_as_read(cls, user_id):
        """Mark all notifications for a user as read"""
        cls.query.filter_by(user_id=user_id, is_read=False).update({'is_read': True})
        db.session.commit()
    
    def __repr__(self):
        return f"<Notification {self.notification_type} for user_id={self.user_id}>" 