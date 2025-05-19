package com.example.alumniapp.data.model

data class Chat(
    val id: String,
    val userName: String,
    val lastMessage: String,
    val timestamp: String, // For simplicity, using String. Consider Long for actual timestamps.
    val profileImageUrl: String? = null // Optional profile image
) 