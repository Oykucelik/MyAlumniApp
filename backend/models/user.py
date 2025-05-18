from datetime import datetime
from werkzeug.security import generate_password_hash, check_password_hash
from . import db

class User(db.Model):
    """User model for authentication and basic information"""
    __tablename__ = 'users'
    
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(120), unique=True, nullable=False, index=True)
    password_hash = db.Column(db.String(256), nullable=False)
    first_name = db.Column(db.String(50), nullable=False)
    last_name = db.Column(db.String(50), nullable=False)
    user_type = db.Column(db.String(20), nullable=False, default='student')  # 'student', 'alumni', 'admin'
    is_active = db.Column(db.Boolean, default=True)
    is_verified = db.Column(db.Boolean, default=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    last_login = db.Column(db.DateTime, nullable=True)
    
    # Relationship with Profile
    profile = db.relationship('Profile', backref='user', uselist=False, cascade='all, delete-orphan')
    
    # Relationship with UserSkill
    skills = db.relationship('UserSkill', backref='user', cascade='all, delete-orphan')
    
    # Relationship with Event (events created by user)
    created_events = db.relationship('Event', backref='creator', foreign_keys='Event.creator_id')
    
    # Relationship with EventAttendee
    event_attendances = db.relationship('EventAttendee', backref='user')
    
    # Relationship with Job (jobs posted by user)
    posted_jobs = db.relationship('Job', backref='poster')
    
    # Relationship with JobApplication
    job_applications = db.relationship('JobApplication', backref='applicant')
    
    # Relationship with Notification
    notifications = db.relationship('Notification', backref='user')
    
    def __init__(self, email, password, first_name, last_name, user_type='student'):
        self.email = email
        self.password = password  # This will use the password setter
        self.first_name = first_name
        self.last_name = last_name
        self.user_type = user_type
    
    @property
    def password(self):
        """Prevent password from being accessed"""
        raise AttributeError('password is not a readable attribute')
    
    @password.setter
    def password(self, password):
        """Set password to a hashed password"""
        self.password_hash = generate_password_hash(password)
    
    def verify_password(self, password):
        """Check if password matches"""
        return check_password_hash(self.password_hash, password)
    
    @property
    def full_name(self):
        """Return user's full name"""
        return f"{self.first_name} {self.last_name}"
    
    def update_last_login(self):
        """Update user's last login time"""
        self.last_login = datetime.utcnow()
        db.session.commit()
    
    def __repr__(self):
        return f"<User {self.email}>" 