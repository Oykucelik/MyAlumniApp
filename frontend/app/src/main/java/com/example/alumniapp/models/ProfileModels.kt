package com.example.alumniapp.models

data class UserProfile(
    val id: Int,
    val email: String,
    val first_name: String,
    val last_name: String,
    val created_at: String,
    val last_login: String,
    val is_verified: Boolean,
    val user_type: String,
    val profile: ProfileDetails? = null
)

data class ProfileDetails(
    val id: Int? = null,
    val bio: String? = null,
    val company: String? = null,
    val degree: String? = null,
    val department: String? = null,
    val graduation_year: String? = null,
    val industry: String? = null,
    val is_mentor: Boolean = false,
    val linkedin_url: String? = null,
    val location: String? = null,
    val mentorship_areas: String? = null,
    val phone_number: String? = null,
    val position: String? = null,
    val profile_picture: String? = null,
    val university: String? = null,
    val years_of_experience: String? = null,
    val availability: String? = null
)

data class ProfileUpdateRequest(
    val name: String? = null,
    val bio: String? = null,
    val profile_image: String? = null,
    val university: String? = null,
    val department: String? = null,
    val graduation_year: String? = null,
    val resume_url: String? = null,
    val skills: List<String>? = null,
    val industry: String? = null,
    val location: String? = null,
    val position: String? = null,
    val company: String? = null,
    val experience_years: Int? = null,
    val is_mentor: Boolean? = null,
    val linkedin_url: String? = null,
    val mentorship_areas: List<String>? = null
)

data class AlumniProfile(
    val id: String,
    val name: String,
    val profile_image: String? = null,
    val bio: String? = null,
    val industry: String? = null,
    val skills: List<String> = listOf(),
    val position: String? = null,
    val company: String? = null,
    val experience_years: Int? = null,
    val is_mentor: Boolean = false,
    val mentorship_areas: List<String> = listOf(),
    val graduation_year: String? = null,
    val university: String? = null,
    val department: String? = null,
    val location: String? = null,
    val linkedin_url: String? = null
) 