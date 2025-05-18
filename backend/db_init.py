import os
from app import create_app
from models import db, User, Profile, Skill
from werkzeug.security import generate_password_hash

def init_db():
    """Initialize the database with some sample data"""
    app = create_app('development')
    with app.app_context():
        # Create tables
        db.create_all()
        
        # Check if data already exists
        if User.query.first() is not None:
            print("Database already contains data, skipping initialization")
            return
        
        print("Initializing database with sample data...")
        
        # Create admin user
        admin = User(
            email='admin@example.com',
            password='adminpass',
            first_name='Admin',
            last_name='User',
            user_type='admin'
        )
        admin.is_verified = True
        db.session.add(admin)
        
        # Create sample alumni users
        alumni1 = User(
            email='alumni1@example.com',
            password='alumnipass',
            first_name='John',
            last_name='Doe',
            user_type='alumni'
        )
        alumni1.is_verified = True
        
        alumni2 = User(
            email='alumni2@example.com',
            password='alumnipass',
            first_name='Jane',
            last_name='Smith',
            user_type='alumni'
        )
        alumni2.is_verified = True
        
        # Create sample student users
        student1 = User(
            email='student1@example.com',
            password='studentpass',
            first_name='Alex',
            last_name='Johnson',
            user_type='student'
        )
        student1.is_verified = True
        
        student2 = User(
            email='student2@example.com',
            password='studentpass',
            first_name='Emily',
            last_name='Brown',
            user_type='student'
        )
        student2.is_verified = True
        
        db.session.add_all([alumni1, alumni2, student1, student2])
        db.session.commit()
        
        # Create profiles for users
        profile_alumni1 = Profile(
            user_id=alumni1.id,
            bio="Experienced software engineer with 10 years in the industry",
            location="New York, NY",
            university="MIT",
            degree="Computer Science",
            graduation_year=2010,
            company="Tech Solutions Inc.",
            position="Senior Software Engineer",
            industry="Technology",
            years_of_experience=10,
            is_mentor=True,
            mentorship_areas="Software Development, Career Guidance"
        )
        
        profile_alumni2 = Profile(
            user_id=alumni2.id,
            bio="Marketing professional specializing in digital marketing strategies",
            location="San Francisco, CA",
            university="Stanford",
            degree="Marketing",
            graduation_year=2012,
            company="Marketing Pros",
            position="Marketing Director",
            industry="Marketing",
            years_of_experience=8,
            is_mentor=True,
            mentorship_areas="Digital Marketing, Personal Branding"
        )
        
        profile_student1 = Profile(
            user_id=student1.id,
            bio="Computer Science student interested in AI and machine learning",
            location="Boston, MA",
            university="MIT",
            degree="Computer Science",
            graduation_year=2023
        )
        
        profile_student2 = Profile(
            user_id=student2.id,
            bio="Marketing student with a focus on social media marketing",
            location="Palo Alto, CA",
            university="Stanford",
            degree="Marketing",
            graduation_year=2023
        )
        
        db.session.add_all([profile_alumni1, profile_alumni2, profile_student1, profile_student2])
        
        # Create sample skills
        skills = [
            Skill(name="Python", category="Technical"),
            Skill(name="JavaScript", category="Technical"),
            Skill(name="SQL", category="Technical"),
            Skill(name="Data Analysis", category="Technical"),
            Skill(name="Machine Learning", category="Technical"),
            Skill(name="Digital Marketing", category="Marketing"),
            Skill(name="Content Creation", category="Marketing"),
            Skill(name="SEO", category="Marketing"),
            Skill(name="Project Management", category="Professional"),
            Skill(name="Leadership", category="Professional"),
            Skill(name="Public Speaking", category="Soft Skills"),
            Skill(name="Communication", category="Soft Skills")
        ]
        
        db.session.add_all(skills)
        db.session.commit()
        
        print("Database initialized successfully!")

if __name__ == "__main__":
    init_db() 