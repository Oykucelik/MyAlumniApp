from datetime import datetime
from . import db

class Mentorship(db.Model):
    """
    Model for mentorship relationships between alumni (mentors) and students (mentees)
    Status: 'pending', 'active', 'completed', 'rejected'
    """
    __tablename__ = 'mentorships'
    
    id = db.Column(db.Integer, primary_key=True)
    mentor_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    mentee_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    status = db.Column(db.String(20), nullable=False, default='pending')
    focus_area = db.Column(db.String(255), nullable=True)
    goals = db.Column(db.Text, nullable=True)
    start_date = db.Column(db.Date, nullable=True)
    end_date = db.Column(db.Date, nullable=True)
    meeting_frequency = db.Column(db.String(50), nullable=True)  # e.g., "weekly", "monthly"
    notes = db.Column(db.Text, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    mentor = db.relationship('User', foreign_keys=[mentor_id], backref=db.backref('mentorships_given', lazy='dynamic'))
    mentee = db.relationship('User', foreign_keys=[mentee_id], backref=db.backref('mentorships_received', lazy='dynamic'))
    
    __table_args__ = (
        db.UniqueConstraint('mentor_id', 'mentee_id', name='uix_mentorship_users'),
    )
    
    @classmethod
    def get_active_mentorships_for_user(cls, user_id):
        """Get all active mentorships for a user (both as mentor and mentee)"""
        return cls.query.filter(
            (
                ((cls.mentor_id == user_id) | (cls.mentee_id == user_id)) &
                (cls.status == 'active')
            )
        ).all()
    
    def __repr__(self):
        return f"<Mentorship mentor_id={self.mentor_id} mentee_id={self.mentee_id} status={self.status}>" 