package com.example.alumniapp.ui.events

data class Event(
    val id: String,
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val description: String,
    val imageUrl: String? = null, // Optional image URL
    var isAttended: Boolean = false // To track attendance status
) 