from datetime import datetime
from . import db

class Profile(db.Model):
    """User profile with detailed information"""
    __tablename__ = 'profiles'
    
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False, unique=True)
    
    # Basic Information
    bio = db.Column(db.Text, nullable=True)
    profile_picture = db.Column(db.String(255), nullable=True)
    phone_number = db.Column(db.String(20), nullable=True)
    
    # Location Information
    location = db.Column(db.String(100), nullable=True)
    
    # Education Information
    university = db.Column(db.String(100), nullable=True)
    degree = db.Column(db.String(100), nullable=True)
    graduation_year = db.Column(db.Integer, nullable=True)
    department = db.Column(db.String(100), nullable=True)
    
    # Professional Information (primarily for alumni)
    company = db.Column(db.String(100), nullable=True)
    position = db.Column(db.String(100), nullable=True)
    industry = db.Column(db.String(100), nullable=True)
    years_of_experience = db.Column(db.Integer, nullable=True)
    linkedin_url = db.Column(db.String(255), nullable=True)
    
    # Mentorship Information
    is_mentor = db.Column(db.Boolean, default=False)
    mentorship_areas = db.Column(db.Text, nullable=True)
    availability = db.Column(db.String(255), nullable=True)  # Comma-separated availability slots
    
    # General fields
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    def __repr__(self):
        return f"<Profile for User ID: {self.user_id}>" 