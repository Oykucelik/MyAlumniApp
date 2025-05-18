package com.example.alumniapp.models

import java.util.Date

// Generic Paged Response
data class PagedResponse<T>(
    val items: List<T>,
    val total_count: Int,
    val current_page: Int,
    val total_pages: Int
)

// Mentorship Models
data class Mentorship(
    val id: String,
    val mentor_id: String,
    val mentor_name: String,
    val mentor_image: String? = null,
    val mentee_id: String,
    val mentee_name: String,
    val mentee_image: String? = null,
    val status: String, // pending, accepted, rejected, completed
    val topic: String,
    val message: String? = null,
    val created_at: Date,
    val updated_at: Date
)

data class MentorshipRequest(
    val mentor_id: String,
    val topic: String,
    val message: String? = null
)

// Event Models
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val event_type: String, // webinar, workshop, networking, other
    val location: String? = null, // can be physical location or online link
    val is_online: Boolean,
    val start_date: Date,
    val end_date: Date,
    val created_by: String,
    val creator_name: String,
    val creator_image: String? = null,
    val rsvp_count: Int,
    val has_user_rsvp: Boolean
)

data class RsvpRequest(
    val status: String // going, maybe, not_going
)

data class RsvpResponse(
    val success: Boolean,
    val message: String,
    val event_id: String,
    val status: String
)

// Job Models
data class Job(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val description: String,
    val requirements: String,
    val job_type: String, // full-time, part-time, internship, contract
    val industry: String,
    val posted_by: String,
    val poster_name: String,
    val posted_at: Date,
    val application_deadline: Date,
    val has_user_applied: Boolean
)

data class JobApplicationResponse(
    val success: Boolean,
    val message: String,
    val job_id: String,
    val application_id: String
) 