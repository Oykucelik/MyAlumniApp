from flask import Blueprint
from .auth import auth_bp
from .users import users_bp
from .profiles import profiles_bp
from .connections import connections_bp
from .messages import messages_bp
from .mentorships import mentorships_bp
from .events import events_bp
from .jobs import jobs_bp
from .skills import skills_bp
from .notifications import notifications_bp

def register_routes(app):
    """Register all blueprint routes with the app"""
    app.register_blueprint(auth_bp, url_prefix='/api/auth')
    app.register_blueprint(users_bp, url_prefix='/api/users')
    app.register_blueprint(profiles_bp, url_prefix='/api/profiles')
    app.register_blueprint(connections_bp, url_prefix='/api/connections')
    app.register_blueprint(messages_bp, url_prefix='/api/messages')
    app.register_blueprint(mentorships_bp, url_prefix='/api/mentorships')
    app.register_blueprint(events_bp, url_prefix='/api/events')
    app.register_blueprint(jobs_bp, url_prefix='/api/jobs')
    app.register_blueprint(skills_bp, url_prefix='/api/skills')
    app.register_blueprint(notifications_bp, url_prefix='/api/notifications') 