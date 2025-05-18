from datetime import datetime
from . import db

class Connection(db.Model):
    """
    Model for managing connections between users (networking)
    Status: 'pending', 'accepted', 'rejected'
    """
    __tablename__ = 'connections'
    
    id = db.Column(db.Integer, primary_key=True)
    requestor_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    recipient_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    status = db.Column(db.String(20), nullable=False, default='pending')
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    requestor = db.relationship('User', foreign_keys=[requestor_id], backref=db.backref('sent_connections', lazy='dynamic'))
    recipient = db.relationship('User', foreign_keys=[recipient_id], backref=db.backref('received_connections', lazy='dynamic'))
    
    __table_args__ = (
        db.UniqueConstraint('requestor_id', 'recipient_id', name='uix_connection_users'),
    )
    
    @classmethod
    def are_connected(cls, user1_id, user2_id):
        """Check if two users are connected"""
        return cls.query.filter(
            (
                (cls.requestor_id == user1_id) & 
                (cls.recipient_id == user2_id) & 
                (cls.status == 'accepted')
            ) | 
            (
                (cls.requestor_id == user2_id) & 
                (cls.recipient_id == user1_id) & 
                (cls.status == 'accepted')
            )
        ).first() is not None
    
    def __repr__(self):
        return f"<Connection requestor_id={self.requestor_id} recipient_id={self.recipient_id} status={self.status}>" 