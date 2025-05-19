package com.example.alumniapp.ui.notifications

data class NotificationItem(
    val id: String, // Unique ID for the notification
    val avatarText: String, // Text for the circular avatar (e.g., "A")
    val header: String,
    val subhead: String,
    val isRead: Boolean = false // Default to unread
    // We can add a field for a drawable resource for the right side icons if needed
    // val actionIconResId: Int? = null
) 