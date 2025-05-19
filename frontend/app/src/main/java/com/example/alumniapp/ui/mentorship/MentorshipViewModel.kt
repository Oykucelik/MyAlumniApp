package com.example.alumniapp.ui.mentorship

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.alumniapp.data.model.Mentorship

class MentorshipViewModel : ViewModel() {

    private val _mentorships = MutableLiveData<List<Mentorship>>()
    val mentorships: LiveData<List<Mentorship>> = _mentorships

    init {
        loadDummyMentorships()
    }

    private fun loadDummyMentorships() {
        val dummyList = listOf(
            Mentorship(
                id = "1",
                mentorName = "Dr. Alice Wonderland",
                topic = "Career in AI Research",
                description = "Guidance on pursuing a research career in Artificial Intelligence, covering top institutions, required skills, and publication strategies.",
                startDate = "2024-08-01",
                endDate = "2024-12-31",
                status = "Available",
                mentorImageUrl = null // Add a URL if you have one
            ),
            Mentorship(
                id = "2",
                mentorName = "Bob The Builder",
                topic = "Software Project Management",
                description = "Learn agile methodologies, team leadership, and project delivery techniques for software development projects.",
                startDate = "2024-07-15",
                endDate = "2024-10-15",
                status = "Ongoing",
                mentorImageUrl = null,
                menteeName = "Charlie Brown"
            ),
            Mentorship(
                id = "3",
                mentorName = "Carol Danvers",
                topic = "Mobile App Development (Android)",
                description = "From basics of Kotlin and Jetpack Compose to advanced topics like performance optimization and app store deployment.",
                startDate = "2024-09-01",
                endDate = "2025-01-31",
                status = "Available",
                mentorImageUrl = null
            ),
             Mentorship(
                id = "4",
                mentorName = "David Copperfield",
                topic = "Entrepreneurship & Startups",
                description = "Insights into building a startup from idea to market, including funding, product development, and scaling.",
                startDate = "2024-06-01",
                endDate = "2024-09-30",
                status = "Completed",
                mentorImageUrl = null,
                menteeName = "Eve Harrington"
            )
        )
        _mentorships.value = dummyList
    }
} 