package com.example.alumniapp.utils

object Constants {
    // API Configuration  retrofit paketi bu urli kullnarak istek atıyor
    const val BASE_URL = "http://10.0.2.2:5000/api/" // Default for Android emulator to localhost
    
    // SharedPreferences Keys
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    
    // Navigation Keys
    const val KEY_IS_FIRST_LOGIN = "is_first_login"
    
    // Request Codes
    const val REQUEST_IMAGE_CAPTURE = 100
    const val REQUEST_IMAGE_PICK = 101
    
    // Event Status
    const val EVENT_STATUS_GOING = "going"
    const val EVENT_STATUS_MAYBE = "maybe"
    const val EVENT_STATUS_NOT_GOING = "not_going"
    
    // Mentorship Status
    const val MENTORSHIP_STATUS_PENDING = "pending"
    const val MENTORSHIP_STATUS_ACCEPTED = "accepted"
    const val MENTORSHIP_STATUS_REJECTED = "rejected"
    const val MENTORSHIP_STATUS_COMPLETED = "completed"
} 