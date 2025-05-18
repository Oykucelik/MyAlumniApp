import os
from flask import Flask, jsonify
from flask_cors import CORS
from flask_jwt_extended import JWTManager
from config import config
from models import db
from routes import register_routes

def create_app(config_name="default"):
    """
    Application factory function to create Flask app instance
    """
    app = Flask(__name__)
    app.config.from_object(config[config_name])
    
    # Initialize extensions
    db.init_app(app)
    jwt = JWTManager(app)
    
    # Set up JWT identity handler to ensure the subject is always a string
    @jwt.user_identity_loader
    def user_identity_lookup(user):
        print(f"JWT identity lookup: {user}, type: {type(user)}")
        if user is None:
            return ""
        return str(user)
    
    # Add error handlers for JWT-related issues
    @jwt.invalid_token_loader
    def invalid_token_callback(error_string):
        print(f"Invalid token: {error_string}")
        return jsonify({"msg": f"Invalid token: {error_string}"}), 401
    
    @jwt.unauthorized_loader
    def missing_token_callback(error_string):
        print(f"Missing token: {error_string}")
        return jsonify({"msg": f"Missing token: {error_string}"}), 401
    
    @jwt.expired_token_loader
    def expired_token_callback(jwt_header, jwt_payload):
        print(f"Token expired: {jwt_payload}")
        return jsonify({"msg": "Token has expired"}), 401
    
    @jwt.token_verification_failed_loader
    def token_verification_callback():
        print("Token verification failed")
        return jsonify({"msg": "Token verification failed"}), 401
    
    @jwt.needs_fresh_token_loader
    def token_not_fresh_callback():
        print("Fresh token required")
        return jsonify({"msg": "Fresh token required"}), 401
    
    @jwt.revoked_token_loader
    def revoked_token_callback(jwt_header, jwt_payload):
        print(f"Revoked token: {jwt_payload}")
        return jsonify({"msg": "Token has been revoked"}), 401
    
    @jwt.user_lookup_error_loader
    def user_lookup_callback(_jwt_header, jwt_payload):
        identity = jwt_payload["sub"]
        print(f"User lookup error for identity: {identity}")
        return jsonify({"msg": f"User {identity} not found"}), 404
    
    CORS(app)
    
    # Register blueprints
    register_routes(app)
    
    # Create tables if they don't exist
    with app.app_context():
        db.create_all()
    
    return app

if __name__ == "__main__":
    app = create_app(os.getenv("FLASK_ENV", "development"))
    app.run(host="0.0.0.0", port=5000) 