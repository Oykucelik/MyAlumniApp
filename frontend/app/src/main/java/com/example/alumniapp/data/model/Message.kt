package com.example.alumniapp.data.model

data class Message(
    val id: String,
    val chatId: String, // To associate message with a chat
    val senderId: String, // To identify the sender
    val text: String,
    val timestamp: Long, // Using Long for actual timestamps
    val isSentByUser: Boolean // To differentiate user's messages from others'
) 