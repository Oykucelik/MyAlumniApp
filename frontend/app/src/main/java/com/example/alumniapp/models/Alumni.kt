package com.example.alumniapp.models

data class Alumni(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val graduationYear: Int,
    val department: String,
    val company: String? = null,
    val position: String? = null,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val skills: List<String> = emptyList(),
    val location: String? = null
) 