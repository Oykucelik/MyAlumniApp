from datetime import datetime
from . import db

class Job(db.Model):
    """Model for job and internship listings"""
    __tablename__ = 'jobs'
    
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(255), nullable=False)
    company = db.Column(db.String(100), nullable=False)
    location = db.Column(db.String(100), nullable=False)
    job_type = db.Column(db.String(50), nullable=False)  # Full-time, Part-time, Internship, etc.
    description = db.Column(db.Text, nullable=False)
    requirements = db.Column(db.Text, nullable=False)
    salary_range = db.Column(db.String(100), nullable=True)
    application_url = db.Column(db.String(255), nullable=True)
    contact_email = db.Column(db.String(120), nullable=True)
    application_deadline = db.Column(db.Date, nullable=True)
    poster_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    is_active = db.Column(db.Boolean, default=True)
    is_approved = db.Column(db.Boolean, default=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    applications = db.relationship('JobApplication', backref='job', cascade='all, delete-orphan')
    
    @property
    def is_expired(self):
        """Check if the job post has expired based on application_deadline"""
        if not self.application_deadline:
            return False
        return self.application_deadline < datetime.utcnow().date()
    
    @property
    def applications_count(self):
        """Get the number of applications for this job"""
        return JobApplication.query.filter_by(job_id=self.id).count()
    
    def __repr__(self):
        return f"<Job {self.title} at {self.company}>"


class JobApplication(db.Model):
    """Model for job applications submitted by users"""
    __tablename__ = 'job_applications'
    
    id = db.Column(db.Integer, primary_key=True)
    job_id = db.Column(db.Integer, db.ForeignKey('jobs.id'), nullable=False)
    applicant_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    cover_letter = db.Column(db.Text, nullable=True)
    resume_url = db.Column(db.String(255), nullable=True)
    status = db.Column(db.String(20), nullable=False, default='applied')  # applied, reviewing, interviewed, accepted, rejected
    application_date = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    __table_args__ = (
        db.UniqueConstraint('job_id', 'applicant_id', name='uix_job_applicant'),
    )
    
    def __repr__(self):
        return f"<JobApplication job_id={self.job_id} applicant_id={self.applicant_id} status={self.status}>" 