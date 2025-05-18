from datetime import datetime
from . import db

class Message(db.Model):
    """Model for direct messages between users"""
    __tablename__ = 'messages'
    
    id = db.Column(db.Integer, primary_key=True)
    sender_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    receiver_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    content = db.Column(db.Text, nullable=False)
    is_read = db.Column(db.Boolean, default=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Relationships
    sender = db.relationship('User', foreign_keys=[sender_id], backref=db.backref('sent_messages', lazy='dynamic'))
    receiver = db.relationship('User', foreign_keys=[receiver_id], backref=db.backref('received_messages', lazy='dynamic'))
    
    @classmethod
    def get_conversation(cls, user1_id, user2_id, limit=50, offset=0):
        """Retrieve conversation between two users"""
        return cls.query.filter(
            (
                (cls.sender_id == user1_id) & 
                (cls.receiver_id == user2_id)
            ) | 
            (
                (cls.sender_id == user2_id) & 
                (cls.receiver_id == user1_id)
            )
        ).order_by(cls.created_at.desc()).limit(limit).offset(offset).all()
    
    @classmethod
    def mark_conversation_as_read(cls, sender_id, receiver_id):
        """Mark all messages from sender to receiver as read"""
        messages = cls.query.filter_by(
            sender_id=sender_id, 
            receiver_id=receiver_id, 
            is_read=False
        ).all()
        
        for message in messages:
            message.is_read = True
        
        db.session.commit()
    
    def __repr__(self):
        return f"<Message sender_id={self.sender_id} receiver_id={self.receiver_id} created_at={self.created_at}>" 