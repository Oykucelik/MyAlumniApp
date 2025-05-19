package com.example.alumniapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Mentorship(
    val id: String,
    val mentorName: String,
    val menteeName: String? = null, // Optional if it's a general mentorship offering
    val topic: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val status: String, // e.g., "Available", "Ongoing", "Completed"
    val mentorImageUrl: String? = null, // Optional image URL for the mentor
    val menteeImageUrl: String? = null  // Optional image URL for the mentee
) : Parcelable 