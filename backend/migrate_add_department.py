from app import create_app
from models import db

def migrate_add_department():
    """Add department field to profiles table"""
    app = create_app('development')
    with app.app_context():
        # Execute raw SQL to add the column
        db.session.execute('ALTER TABLE profiles ADD COLUMN department VARCHAR(100);')
        db.session.commit()
        print("Migration completed: Added department column to profiles table")

if __name__ == "__main__":
    migrate_add_department() 