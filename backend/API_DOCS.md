# Alumni Network API Documentation

This document provides detailed API endpoint examples for the Alumni Network application.

## Base URL

All endpoints are relative to the base URL: `http://localhost:5000/api/`

## Authentication

Authentication is handled using JWT (JSON Web Token). After obtaining a token from the login or register endpoints, include it in the `Authorization` header for all protected endpoints:

```
Authorization: Bearer <your_jwt_token>
```

### Register a new user

**Endpoint:** `POST /auth/register`

**Request:**
```json
{
  "email": "john.doe@example.com",
  "password": "securepassword",
  "first_name": "John",
  "last_name": "Doe",
  "user_type": "student"  // "student" or "alumni"
}
```

**Response:**
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "email": "john.doe@example.com",
    "first_name": "John",
    "last_name": "Doe",
    "user_type": "student"
  },
  "access_token": "your_jwt_token"
}
```

### Login

**Endpoint:** `POST /auth/login`

**Request:**
```json
{
  "email": "john.doe@example.com",
  "password": "securepassword"
}
```

**Response:**
```json
{
  "message": "Login successful",
  "user": {
    "id": 1,
    "email": "john.doe@example.com",
    "first_name": "John",
    "last_name": "Doe",
    "user_type": "student"
  },
  "access_token": "your_jwt_token"
}
```

## Profiles

### Get user profile

**Endpoint:** `GET /profiles/{user_id}`

**Response:**
```json
{
  "profile": {
    "id": 1,
    "user_id": 1,
    "bio": "Computer Science student interested in AI and machine learning",
    "location": "Boston, MA",
    "university": "MIT",
    "degree": "Computer Science",
    "graduation_year": 2023,
    "company": null,
    "position": null,
    "industry": null,
    "linkedin_url": "https://linkedin.com/in/johndoe",
    "is_mentor": false,
    "mentorship_areas": null,
    "created_at": "2023-05-10T12:00:00.000000",
    "updated_at": "2023-05-10T12:00:00.000000"
  }
}
```

### Update user profile

**Endpoint:** `PUT /profiles/{user_id}`

**Request:**
```json
{
  "bio": "Updated bio information",
  "location": "New York, NY",
  "university": "MIT",
  "degree": "Computer Science",
  "graduation_year": 2023,
  "linkedin_url": "https://linkedin.com/in/johndoe"
}
```

**Response:**
```json
{
  "message": "Profile updated successfully",
  "profile": {
    "id": 1,
    "user_id": 1,
    "bio": "Updated bio information",
    "location": "New York, NY",
    "university": "MIT",
    "degree": "Computer Science",
    "graduation_year": 2023,
    "linkedin_url": "https://linkedin.com/in/johndoe",
    "updated_at": "2023-05-11T09:30:00.000000"
  }
}
```

## Connections

### Send connection request

**Endpoint:** `POST /connections`

**Request:**
```json
{
  "recipient_id": 2
}
```

**Response:**
```json
{
  "message": "Connection request sent",
  "connection": {
    "id": 1,
    "requestor_id": 1,
    "recipient_id": 2,
    "status": "pending",
    "created_at": "2023-05-12T10:15:00.000000"
  }
}
```

### Accept/Reject connection request

**Endpoint:** `PUT /connections/{connection_id}`

**Request:**
```json
{
  "status": "accepted"  // or "rejected"
}
```

**Response:**
```json
{
  "message": "Connection accepted",
  "connection": {
    "id": 1,
    "requestor_id": 1,
    "recipient_id": 2,
    "status": "accepted",
    "updated_at": "2023-05-12T10:30:00.000000"
  }
}
```

## Mentorships

### Request mentorship

**Endpoint:** `POST /mentorships`

**Request:**
```json
{
  "mentor_id": 3,
  "focus_area": "Machine Learning",
  "goals": "I want to learn practical ML applications and career guidance"
}
```

**Response:**
```json
{
  "message": "Mentorship request sent successfully",
  "mentorship": {
    "id": 1,
    "mentor_id": 3,
    "mentee_id": 1,
    "status": "pending",
    "focus_area": "Machine Learning",
    "goals": "I want to learn practical ML applications and career guidance",
    "created_at": "2023-05-15T14:00:00.000000"
  }
}
```

### Update mentorship status (for mentor)

**Endpoint:** `PUT /mentorships/{mentorship_id}`

**Request:**
```json
{
  "status": "active",
  "start_date": "2023-06-01",
  "end_date": "2023-09-01",
  "meeting_frequency": "biweekly"
}
```

**Response:**
```json
{
  "message": "Mentorship accepted successfully",
  "mentorship": {
    "id": 1,
    "status": "active",
    "updated_at": "2023-05-16T09:45:00.000000"
  }
}
```

## Events

### Create an event (alumni only)

**Endpoint:** `POST /events`

**Request:**
```json
{
  "title": "Career Panel: Tech Industry",
  "description": "Join our panel of industry experts to learn about career paths in tech.",
  "event_type": "panel",
  "location": "Online Webinar",
  "virtual_meeting_link": "https://zoom.us/j/123456789",
  "start_datetime": "2023-07-15T18:00:00",
  "end_datetime": "2023-07-15T20:00:00",
  "max_attendees": 100
}
```

**Response:**
```json
{
  "message": "Event created successfully",
  "event": {
    "id": 1,
    "title": "Career Panel: Tech Industry",
    "description": "Join our panel of industry experts to learn about career paths in tech.",
    "event_type": "panel",
    "location": "Online Webinar",
    "virtual_meeting_link": "https://zoom.us/j/123456789",
    "start_datetime": "2023-07-15T18:00:00",
    "end_datetime": "2023-07-15T20:00:00",
    "max_attendees": 100,
    "creator_id": 3,
    "created_at": "2023-05-20T10:00:00.000000"
  }
}
```

### RSVP to an event

**Endpoint:** `POST /events/{event_id}/rsvp`

**Response:**
```json
{
  "message": "RSVP successful",
  "rsvp": {
    "id": 1,
    "event_id": 1,
    "user_id": 1,
    "status": "registered",
    "registration_datetime": "2023-05-20T11:30:00.000000"
  }
}
```

## Jobs

### Create a job listing (alumni only)

**Endpoint:** `POST /jobs`

**Request:**
```json
{
  "title": "Software Engineer Intern",
  "company": "Tech Solutions Inc.",
  "location": "New York, NY",
  "job_type": "Internship",
  "description": "Summer internship opportunity for CS students...",
  "requirements": "- Proficiency in Python\n- Basic knowledge of web development\n- Currently pursuing CS degree",
  "salary_range": "$20-25/hour",
  "application_deadline": "2023-06-15"
}
```

**Response:**
```json
{
  "message": "Job listing created successfully",
  "job": {
    "id": 1,
    "title": "Software Engineer Intern",
    "company": "Tech Solutions Inc.",
    "location": "New York, NY",
    "job_type": "Internship",
    "description": "Summer internship opportunity for CS students...",
    "requirements": "- Proficiency in Python\n- Basic knowledge of web development\n- Currently pursuing CS degree",
    "salary_range": "$20-25/hour",
    "application_deadline": "2023-06-15",
    "created_at": "2023-05-25T15:00:00.000000"
  }
}
```

### Apply for a job

**Endpoint:** `POST /jobs/{job_id}/apply`

**Request:**
```json
{
  "cover_letter": "Dear Hiring Manager, I am excited to apply for the Software Engineer Intern position...",
  "resume_url": "https://example.com/my_resume.pdf"
}
```

**Response:**
```json
{
  "message": "Application submitted successfully",
  "application": {
    "id": 1,
    "job_id": 1,
    "applicant_id": 1,
    "status": "applied",
    "application_date": "2023-05-25T16:30:00.000000"
  }
}
```

## Messages

### Send a message

**Endpoint:** `POST /messages`

**Request:**
```json
{
  "receiver_id": 3,
  "content": "Hello, I'd like to ask about the mentorship program."
}
```

**Response:**
```json
{
  "message": "Message sent successfully",
  "message_data": {
    "id": 1,
    "sender_id": 1,
    "receiver_id": 3,
    "content": "Hello, I'd like to ask about the mentorship program.",
    "is_read": false,
    "created_at": "2023-05-26T09:15:00.000000"
  }
}
```

### Get conversation with a user

**Endpoint:** `GET /messages/conversation/{user_id}`

**Response:**
```json
{
  "messages": [
    {
      "id": 2,
      "sender_id": 3,
      "receiver_id": 1,
      "content": "Hi there! I'd be happy to chat about mentorship.",
      "is_read": false,
      "created_at": "2023-05-26T09:20:00.000000",
      "is_mine": false
    },
    {
      "id": 1,
      "sender_id": 1,
      "receiver_id": 3,
      "content": "Hello, I'd like to ask about the mentorship program.",
      "is_read": true,
      "created_at": "2023-05-26T09:15:00.000000",
      "is_mine": true
    }
  ],
  "pagination": {
    "total": 2,
    "pages": 1,
    "page": 1,
    "per_page": 20,
    "has_next": false,
    "has_prev": false
  }
}
```

## Skills

### Add a skill to your profile

**Endpoint:** `POST /skills/user`

**Request:**
```json
{
  "skill_id": 5,
  "proficiency_level": 4,
  "years_of_experience": 2
}
```

**Response:**
```json
{
  "message": "Skill added successfully",
  "user_skill": {
    "id": 1,
    "user_id": 1,
    "skill_id": 5,
    "skill_name": "Python",
    "proficiency_level": 4,
    "years_of_experience": 2
  }
}
```

## Notifications

### Get user notifications

**Endpoint:** `GET /notifications`

**Response:**
```json
{
  "notifications": [
    {
      "id": 1,
      "title": "New Connection Request",
      "message": "Jane Smith sent you a connection request.",
      "notification_type": "connection",
      "related_id": 2,
      "is_read": false,
      "created_at": "2023-05-27T10:00:00.000000"
    },
    {
      "id": 2,
      "title": "New Message",
      "message": "You have a new message from Mark Johnson.",
      "notification_type": "message",
      "related_id": 3,
      "is_read": false,
      "created_at": "2023-05-27T11:30:00.000000"
    }
  ],
  "unread_count": 2,
  "pagination": {
    "total": 2,
    "pages": 1,
    "page": 1,
    "per_page": 20,
    "has_next": false,
    "has_prev": false
  }
}
```

### Mark all notifications as read

**Endpoint:** `PUT /notifications/read-all`

**Response:**
```json
{
  "message": "All notifications marked as read"
}
```

## Error Responses

Most API errors will return with an appropriate HTTP status code and a JSON object with an error message:

```json
{
  "error": "Resource not found"
}
```

Common error codes:
- `400 Bad Request`: Missing or invalid parameters
- `401 Unauthorized`: Missing or invalid authentication
- `403 Forbidden`: Authenticated but not allowed to access the resource
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource already exists
- `500 Internal Server Error`: Server-side error 