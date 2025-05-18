from flask_sqlalchemy import SQLAlchemy

# Initialize SQLAlchemy instance
db = SQLAlchemy()

# Import all models here to make them available
from .user import User
from .profile import Profile
from .skill import Skill, UserSkill
from .connection import Connection
from .message import Message
from .mentorship import Mentorship
from .event import Event, EventAttendee
from .job import Job, JobApplication
from .notification import Notification 