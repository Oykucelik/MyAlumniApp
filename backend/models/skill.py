from datetime import datetime
from . import db

class Skill(db.Model):
    """Skill model for storing available skills in the system"""
    __tablename__ = 'skills'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(50), nullable=False, unique=True)
    category = db.Column(db.String(50), nullable=True)  # Technical, Soft Skills, etc.
    description = db.Column(db.Text, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Relationship with UserSkill
    users = db.relationship('UserSkill', backref='skill')
    
    def __repr__(self):
        return f"<Skill {self.name}>"


class UserSkill(db.Model):
    """Association table between users and skills with proficiency level"""
    __tablename__ = 'user_skills'
    
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    skill_id = db.Column(db.Integer, db.ForeignKey('skills.id'), nullable=False)
    proficiency_level = db.Column(db.Integer, nullable=False, default=1)  # 1-5 scale
    years_of_experience = db.Column(db.Integer, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    __table_args__ = (
        db.UniqueConstraint('user_id', 'skill_id', name='uix_user_skill'),
    )
    
    def __repr__(self):
        return f"<UserSkill user_id={self.user_id} skill_id={self.skill_id}>" 