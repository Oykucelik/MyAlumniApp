# Alumni Network API

Backend API for the Alumni Network mobile application that connects university students with alumni for networking, mentorship, and career support.

## Features

- **User Authentication**: Register, login, and user profile management
- **Alumni Profiles**: Detailed alumni profiles with professional information
- **Networking & Connection**: Connect with other users in the network
- **Mentorship**: Request and manage mentorship relationships
- **Messaging**: Direct messaging between connected users
- **Events**: Create, manage, and RSVP to career events
- **Jobs & Internships**: Post and apply for job opportunities
- **Skills**: Manage user skills and proficiency levels
- **Notifications**: Real-time notifications for user activities

## Tech Stack

- **Framework**: Flask (Python)
- **Database**: MySQL (via SQLAlchemy)
- **Authentication**: JWT (JSON Web Tokens)
- **API**: RESTful endpoints
- **Notifications**: Firebase Cloud Messaging (optional)

## Setup Instructions

### Prerequisites

- Python 3.8 or higher
- MySQL Server (or XAMPP)
- Virtual environment (recommended)

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/alumni-network-api.git
   cd alumni-network-api
   ```

2. Create and activate a virtual environment:
   ```
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. Install dependencies:
   ```
   pip install -r requirements.txt
   ```

4. Set up the MySQL database:
   - Create a new database named `alumni_network`
   - Modify the database configuration in `.env` file (copy from `.env.example`)

5. Initialize the database with sample data:
   ```
   python db_init.py
   ```

6. Run database migrations (if needed):
   ```
   python migrate_add_department.py
   ```

7. Run the application:
   ```
   python app.py
   ```

The API will be available at `http://localhost:5000/api/`.

### Environment Variables

Create a `.env` file in the root directory with the following variables:

```
# Database configuration
DB_HOST=localhost
DB_PORT=3306
DB_USER=your_db_username
DB_PASSWORD=your_db_password
DB_NAME=alumni_network

# JWT configuration
JWT_SECRET_KEY=your_jwt_secret_key
JWT_ACCESS_TOKEN_EXPIRES=3600

# Flask configuration
FLASK_APP=app.py
FLASK_ENV=development
FLASK_DEBUG=1
SECRET_KEY=your_flask_secret_key

# Firebase configuration (optional)
FIREBASE_CREDENTIALS_PATH=path/to/firebase-credentials.json
```

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get access token
- `GET /api/auth/me` - Get current user info
- `PUT /api/auth/change-password` - Change password

### Users

- `GET /api/users` - Get list of users
- `GET /api/users/<id>` - Get user by ID
- `PUT /api/users/<id>` - Update user
- `DELETE /api/users/<id>` - Delete user (admin only)

### Profiles

- `GET /api/profiles/<user_id>` - Get user profile
- `PUT /api/profiles/<user_id>` - Update user profile
- `GET /api/profiles/mentors` - Get list of mentors

### Connections

- `POST /api/connections` - Send connection request
- `PUT /api/connections/<id>` - Accept/reject connection request
- `GET /api/connections` - Get user connections
- `DELETE /api/connections/<id>` - Delete connection

### Messages

- `POST /api/messages` - Send message
- `GET /api/messages/conversations` - Get message conversations
- `GET /api/messages/conversation/<user_id>` - Get conversation with user
- `PUT /api/messages/<id>/read` - Mark message as read
- `DELETE /api/messages/<id>` - Delete message

### Mentorships

- `POST /api/mentorships` - Request mentorship
- `PUT /api/mentorships/<id>` - Update mentorship status
- `GET /api/mentorships` - Get user mentorships
- `GET /api/mentorships/<id>` - Get mentorship details

### Events

- `POST /api/events` - Create event (alumni only)
- `GET /api/events` - Get events list
- `GET /api/events/<id>` - Get event details
- `PUT /api/events/<id>` - Update event
- `DELETE /api/events/<id>` - Delete event
- `POST /api/events/<id>/rsvp` - RSVP to event
- `POST /api/events/<id>/rsvp/cancel` - Cancel RSVP
- `GET /api/events/<id>/attendees` - Get event attendees

### Jobs

- `POST /api/jobs` - Create job listing (alumni only)
- `GET /api/jobs` - Get job listings
- `GET /api/jobs/<id>` - Get job details
- `PUT /api/jobs/<id>` - Update job listing
- `DELETE /api/jobs/<id>` - Delete job listing
- `POST /api/jobs/<id>/apply` - Apply for job
- `GET /api/jobs/applications` - Get user job applications
- `GET /api/jobs/<id>/applications` - Get job applications
- `PUT /api/jobs/applications/<id>/status` - Update application status

### Skills

- `GET /api/skills` - Get all skills
- `POST /api/skills` - Create skill (admin only)
- `PUT /api/skills/<id>` - Update skill (admin only)
- `DELETE /api/skills/<id>` - Delete skill (admin only)
- `GET /api/skills/user` - Get current user skills
- `GET /api/skills/user/<user_id>` - Get user skills
- `POST /api/skills/user` - Add skill to user
- `PUT /api/skills/user/<id>` - Update user skill
- `DELETE /api/skills/user/<id>` - Delete user skill

### Notifications

- `GET /api/notifications` - Get user notifications
- `PUT /api/notifications/<id>/read` - Mark notification as read
- `PUT /api/notifications/read-all` - Mark all notifications as read
- `DELETE /api/notifications/<id>` - Delete notification
- `DELETE /api/notifications/delete-all` - Delete all notifications

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributors

- Your Name - Initial work 