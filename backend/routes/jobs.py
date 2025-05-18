from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import db, User, Job, JobApplication, Notification
from utils.auth import alumni_required
from datetime import datetime, date
from utils.response import APIResponse

jobs_bp = Blueprint('jobs', __name__)

@jobs_bp.route('', methods=['POST'])
@jwt_required()
@alumni_required()
def create_job():
    """Create a new job listing (alumni only)"""
    current_user_id = get_jwt_identity()
    data = request.get_json()
    
    # Validate required fields
    required_fields = ['title', 'company', 'location', 'job_type', 'description', 'requirements']
    for field in required_fields:
        if field not in data:
            return APIResponse.error_response(f'Missing required field: {field}', status_code=400)
    
    # Convert application_deadline if provided
    application_deadline = None
    if 'application_deadline' in data and data['application_deadline']:
        try:
            application_deadline = datetime.fromisoformat(data['application_deadline'].replace('Z', '+00:00')).date()
            # Ensure the deadline is in the future
            if application_deadline < date.today():
                return APIResponse.error_response('Application deadline cannot be in the past', status_code=400)
        except ValueError:
            return APIResponse.error_response('Invalid date format for application_deadline', status_code=400)
    
    # Create the job listing
    try:
        job = Job(
            title=data['title'],
            company=data['company'],
            location=data['location'],
            job_type=data['job_type'],
            description=data['description'],
            requirements=data['requirements'],
            salary_range=data.get('salary_range'),
            application_url=data.get('application_url'),
            contact_email=data.get('contact_email'),
            application_deadline=application_deadline,
            poster_id=current_user_id,
            is_active=True,
            is_approved=True  # Auto-approve by default, can change based on business requirements
        )
        
        db.session.add(job)
        db.session.commit()
        
        return APIResponse.success_response(
            message='Job listing created successfully',
            data={
                'job': {
                    'id': job.id,
                    'title': job.title,
                    'company': job.company,
                    'location': job.location,
                    'job_type': job.job_type,
                    'description': job.description,
                    'requirements': job.requirements,
                    'salary_range': job.salary_range,
                    'application_url': job.application_url,
                    'contact_email': job.contact_email,
                    'application_deadline': job.application_deadline.isoformat() if job.application_deadline else None,
                    'poster_id': job.poster_id,
                    'is_active': job.is_active,
                    'is_approved': job.is_approved,
                    'created_at': job.created_at.isoformat()
                }
            },
            status_code=201
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@jobs_bp.route('', methods=['GET'])
@jwt_required()
def get_jobs():
    """
    Get a list of jobs with optional filtering
    
    Query parameters:
    - job_type: Filter by job type (Full-time, Part-time, Internship, etc.)
    - company: Filter by company
    - location: Filter by location
    - active_only: If 'true', show only active listings
    - search: Search in title, company, description
    - page: Page number (default: 1)
    - per_page: Results per page (default: 10, max: 50)
    """
    job_type = request.args.get('job_type')
    company = request.args.get('company')
    location = request.args.get('location')
    active_only = request.args.get('active_only', 'true').lower() == 'true'
    search = request.args.get('search')
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 10)), 50)
    
    # Base query
    query = Job.query.filter(Job.is_approved == True)
    
    # Apply filters
    if job_type:
        query = query.filter(Job.job_type == job_type)
    
    if company:
        query = query.filter(Job.company.ilike(f'%{company}%'))
    
    if location:
        query = query.filter(Job.location.ilike(f'%{location}%'))
    
    if active_only:
        query = query.filter(Job.is_active == True)
        # Also exclude expired jobs
        today = date.today()
        query = query.filter((Job.application_deadline == None) | (Job.application_deadline >= today))
    
    if search:
        search_term = f'%{search}%'
        query = query.filter(
            (Job.title.ilike(search_term)) |
            (Job.company.ilike(search_term)) |
            (Job.description.ilike(search_term))
        )
    
    # Order by creation date (newest first)
    query = query.order_by(Job.created_at.desc())
    
    # Execute paginated query
    jobs_pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    jobs = jobs_pagination.items
    
    # Format response
    jobs_data = []
    for job in jobs:
        poster = User.query.get(job.poster_id)
        poster_name = poster.full_name if poster else "Unknown"
        
        job_data = {
            'id': job.id,
            'title': job.title,
            'company': job.company,
            'location': job.location,
            'job_type': job.job_type,
            'description': job.description,
            'requirements': job.requirements,
            'salary_range': job.salary_range,
            'application_url': job.application_url,
            'contact_email': job.contact_email,
            'application_deadline': job.application_deadline.isoformat() if job.application_deadline else None,
            'is_expired': job.is_expired,
            'poster_id': job.poster_id,
            'poster_name': poster_name,
            'applications_count': job.applications_count,
            'created_at': job.created_at.isoformat()
        }
        
        jobs_data.append(job_data)
    
    return APIResponse.success_response(
        message='Jobs retrieved successfully',
        data={
            'jobs': jobs_data,
            'pagination': {
                'total': jobs_pagination.total,
                'pages': jobs_pagination.pages,
                'page': page,
                'per_page': per_page,
                'has_next': jobs_pagination.has_next,
                'has_prev': jobs_pagination.has_prev
            }
        },
        status_code=200
    )


@jobs_bp.route('/<int:job_id>', methods=['GET'])
@jwt_required()
def get_job(job_id):
    """Get a specific job by ID"""
    job = Job.query.get(job_id)
    
    if not job:
        return APIResponse.error_response('Job listing not found', status_code=404)
    
    # Check if job is approved or if current user is the poster
    current_user_id = get_jwt_identity()
    if not job.is_approved and job.poster_id != current_user_id:
        return APIResponse.error_response('Job listing not found', status_code=404)
    
    # Get poster information
    poster = User.query.get(job.poster_id)
    poster_name = poster.full_name if poster else "Unknown"
    
    # Format response
    job_data = {
        'id': job.id,
        'title': job.title,
        'company': job.company,
        'location': job.location,
        'job_type': job.job_type,
        'description': job.description,
        'requirements': job.requirements,
        'salary_range': job.salary_range,
        'application_url': job.application_url,
        'contact_email': job.contact_email,
        'application_deadline': job.application_deadline.isoformat() if job.application_deadline else None,
        'is_active': job.is_active,
        'is_approved': job.is_approved,
        'is_expired': job.is_expired,
        'poster_id': job.poster_id,
        'poster_name': poster_name,
        'applications_count': job.applications_count,
        'created_at': job.created_at.isoformat(),
        'updated_at': job.updated_at.isoformat()
    }
    
    # Check if current user has applied for this job
    has_applied = False
    application = JobApplication.query.filter_by(
        job_id=job_id,
        applicant_id=current_user_id
    ).first()
    
    if application:
        has_applied = True
        job_data['application'] = {
            'id': application.id,
            'status': application.status,
            'application_date': application.application_date.isoformat()
        }
    
    job_data['has_applied'] = has_applied
    
    return APIResponse.success_response(
        message='Job retrieved successfully',
        data={
            'job': job_data
        },
        status_code=200
    )


@jobs_bp.route('/<int:job_id>', methods=['PUT'])
@jwt_required()
def update_job(job_id):
    """Update a job listing (poster only)"""
    current_user_id = get_jwt_identity()
    job = Job.query.get(job_id)
    
    if not job:
        return APIResponse.error_response('Job listing not found', status_code=404)
    
    # Check if current user is the poster
    if job.poster_id != current_user_id:
        return APIResponse.error_response('Permission denied', status_code=403)
    
    data = request.get_json()
    if not data:
        return APIResponse.error_response('No data provided', status_code=400)
    
    try:
        # Update fields
        allowed_fields = [
            'title', 'company', 'location', 'job_type', 'description', 
            'requirements', 'salary_range', 'application_url', 'contact_email', 
            'is_active'
        ]
        
        for field in allowed_fields:
            if field in data:
                setattr(job, field, data[field])
        
        # Handle application_deadline separately
        if 'application_deadline' in data:
            if data['application_deadline']:
                try:
                    application_deadline = datetime.fromisoformat(data['application_deadline'].replace('Z', '+00:00')).date()
                    job.application_deadline = application_deadline
                except ValueError:
                    return APIResponse.error_response('Invalid date format for application_deadline', status_code=400)
            else:
                job.application_deadline = None
        
        db.session.commit()
        
        return APIResponse.success_response(
            message='Job listing updated successfully',
            data={
                'job': {
                    'id': job.id,
                    'title': job.title,
                    'updated_at': job.updated_at.isoformat()
                }
            },
            status_code=200
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@jobs_bp.route('/<int:job_id>', methods=['DELETE'])
@jwt_required()
def delete_job(job_id):
    """Delete a job listing (poster only)"""
    current_user_id = get_jwt_identity()
    job = Job.query.get(job_id)
    
    if not job:
        return APIResponse.error_response('Job listing not found', status_code=404)
    
    # Check if current user is the poster
    if job.poster_id != current_user_id:
        return APIResponse.error_response('Permission denied', status_code=403)
    
    try:
        # Notify applicants of job deletion
        applications = JobApplication.query.filter_by(job_id=job_id).all()
        for application in applications:
            Notification.create_notification(
                application.applicant_id,
                'Job Posting Removed',
                f'The job "{job.title}" at {job.company} has been removed.',
                'job',
                job.id
            )
        
        db.session.delete(job)
        db.session.commit()
        
        return APIResponse.success_response(
            message='Job listing deleted successfully',
            status_code=200
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@jobs_bp.route('/<int:job_id>/apply', methods=['POST'])
@jwt_required()
def apply_for_job(job_id):
    """
    Apply for a job
    
    Required JSON data:
    - cover_letter (optional): Text of cover letter
    - resume_url (optional): URL to resume file
    """
    current_user_id = get_jwt_identity()
    job = Job.query.get(job_id)
    
    if not job:
        return APIResponse.error_response('Job listing not found', status_code=404)
    
    # Check if job is active and approved
    if not job.is_active or not job.is_approved:
        return APIResponse.error_response('This job listing is no longer active', status_code=400)
    
    # Check if job is expired
    if job.is_expired:
        return APIResponse.error_response('The application deadline for this job has passed', status_code=400)
    
    # Check if user has already applied
    existing_application = JobApplication.query.filter_by(
        job_id=job_id,
        applicant_id=current_user_id
    ).first()
    
    if existing_application:
        return APIResponse.error_response('You have already applied for this job', status_code=400)
    
    data = request.get_json() or {}
    
    # Create the application
    try:
        application = JobApplication(
            job_id=job_id,
            applicant_id=current_user_id,
            cover_letter=data.get('cover_letter'),
            resume_url=data.get('resume_url'),
            status='applied',
            application_date=datetime.utcnow()
        )
        
        db.session.add(application)
        db.session.commit()
        
        # Notify job poster
        applicant = User.query.get(current_user_id)
        Notification.create_notification(
            job.poster_id,
            'New Job Application',
            f'{applicant.full_name} has applied for your job posting "{job.title}".',
            'job',
            job.id
        )
        
        return APIResponse.success_response(
            message='Application submitted successfully',
            data={
                'application': {
                    'id': application.id,
                    'job_id': application.job_id,
                    'applicant_id': application.applicant_id,
                    'status': application.status,
                    'application_date': application.application_date.isoformat()
                }
            },
            status_code=201
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500)


@jobs_bp.route('/applications', methods=['GET'])
@jwt_required()
def get_my_applications():
    """
    Get all job applications for the current user
    
    Query parameters:
    - status: Filter by status (applied, reviewing, interviewed, accepted, rejected)
    - page: Page number (default: 1)
    - per_page: Results per page (default: 10, max: 50)
    """
    current_user_id = get_jwt_identity()
    status = request.args.get('status')
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 10)), 50)
    
    # Build query
    query = JobApplication.query.filter_by(applicant_id=current_user_id)
    
    if status:
        query = query.filter_by(status=status)
    
    # Order by application date (newest first)
    query = query.order_by(JobApplication.application_date.desc())
    
    # Execute paginated query
    applications_pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    applications = applications_pagination.items
    
    # Format response
    applications_data = []
    for application in applications:
        job = Job.query.get(application.job_id)
        if not job:
            continue
        
        application_data = {
            'id': application.id,
            'job_id': application.job_id,
            'status': application.status,
            'cover_letter': application.cover_letter,
            'resume_url': application.resume_url,
            'application_date': application.application_date.isoformat(),
            'updated_at': application.updated_at.isoformat(),
            'job': {
                'title': job.title,
                'company': job.company,
                'location': job.location,
                'job_type': job.job_type,
                'application_deadline': job.application_deadline.isoformat() if job.application_deadline else None,
                'is_active': job.is_active
            }
        }
        
        applications_data.append(application_data)
    
    return APIResponse.success_response(
        message='Applications retrieved successfully',
        data={
            'applications': applications_data,
            'pagination': {
                'total': applications_pagination.total,
                'pages': applications_pagination.pages,
                'page': page,
                'per_page': per_page,
                'has_next': applications_pagination.has_next,
                'has_prev': applications_pagination.has_prev
            }
        },
        status_code=200
    )


@jobs_bp.route('/<int:job_id>/applications', methods=['GET'])
@jwt_required()
def get_job_applications(job_id):
    """
    Get all applications for a specific job (poster only)
    
    Query parameters:
    - status: Filter by status 
    - page: Page number (default: 1)
    - per_page: Results per page (default: 20, max: 50)
    """
    current_user_id = get_jwt_identity()
    job = Job.query.get(job_id)
    
    if not job:
        return APIResponse.error_response('Job listing not found', status_code=404)
    
    # Check if current user is the poster
    if job.poster_id != current_user_id:
        return APIResponse.error_response('Permission denied', status_code=403)
    
    # Get query parameters
    status = request.args.get('status')
    page = int(request.args.get('page', 1))
    per_page = min(int(request.args.get('per_page', 20)), 50)
    
    # Build query
    query = JobApplication.query.filter_by(job_id=job_id)
    
    if status:
        query = query.filter_by(status=status)
    
    # Execute paginated query
    applications_pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    applications = applications_pagination.items
    
    # Format response
    applications_data = []
    for application in applications:
        applicant = User.query.get(application.applicant_id)
        if not applicant:
            continue
        
        application_data = {
            'id': application.id,
            'status': application.status,
            'cover_letter': application.cover_letter,
            'resume_url': application.resume_url,
            'application_date': application.application_date.isoformat(),
            'updated_at': application.updated_at.isoformat(),
            'applicant': {
                'id': applicant.id,
                'first_name': applicant.first_name,
                'last_name': applicant.last_name,
                'full_name': applicant.full_name,
                'email': applicant.email
            }
        }
        
        if applicant.profile:
            application_data['applicant']['profile'] = {
                'university': applicant.profile.university,
                'degree': applicant.profile.degree,
                'graduation_year': applicant.profile.graduation_year
            }
        
        applications_data.append(application_data)
    
    return APIResponse.success_response(
        message='Applications retrieved successfully',
        data={
            'applications': applications_data,
            'pagination': {
                'total': applications_pagination.total,
                'pages': applications_pagination.pages,
                'page': page,
                'per_page': per_page,
                'has_next': applications_pagination.has_next,
                'has_prev': applications_pagination.has_prev
            }
        },
        status_code=200
    )


@jobs_bp.route('/applications/<int:application_id>/status', methods=['PUT'])
@jwt_required()
def update_application_status(application_id):
    """
    Update the status of a job application (job poster only)
    
    Required JSON data:
    - status: New status value (reviewing, interviewed, accepted, rejected)
    """
    current_user_id = get_jwt_identity()
    application = JobApplication.query.get(application_id)
    
    if not application:
        return APIResponse.error_response('Application not found', status_code=404)
    
    # Get the job and check if current user is the poster
    job = Job.query.get(application.job_id)
    if not job or job.poster_id != current_user_id:
        return APIResponse.error_response('Permission denied', status_code=403)
    
    data = request.get_json()
    if not data or 'status' not in data:
        return APIResponse.error_response('Status is required', status_code=400)
    
    new_status = data['status']
    valid_statuses = ['applied', 'reviewing', 'interviewed', 'accepted', 'rejected']
    if new_status not in valid_statuses:
        return APIResponse.error_response(f'Invalid status. Must be one of: {", ".join(valid_statuses)}', status_code=400)
    
    try:
        old_status = application.status
        application.status = new_status
        db.session.commit()
        
        # Create notification for applicant
        status_messages = {
            'reviewing': 'Your application is now being reviewed',
            'interviewed': 'You have been selected for an interview',
            'accepted': 'Congratulations! Your application has been accepted',
            'rejected': 'Unfortunately, your application has been rejected'
        }
        
        if new_status in status_messages:
            Notification.create_notification(
                application.applicant_id,
                f'Application Status Update: {job.title}',
                f'{status_messages[new_status]} for the position of {job.title} at {job.company}.',
                'job',
                job.id
            )
        
        return APIResponse.success_response(
            message='Application status updated successfully',
            data={
                'application': {
                    'id': application.id,
                    'job_id': application.job_id,
                    'status': application.status,
                    'previous_status': old_status,
                    'updated_at': application.updated_at.isoformat()
                }
            },
            status_code=200
        )
        
    except Exception as e:
        db.session.rollback()
        return APIResponse.error_response(str(e), status_code=500) 