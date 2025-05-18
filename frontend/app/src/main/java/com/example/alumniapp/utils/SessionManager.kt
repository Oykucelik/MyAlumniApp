package com.example.alumniapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.alumniapp.utils.Constants.PREF_AUTH_TOKEN
import com.example.alumniapp.utils.Constants.PREF_IS_LOGGED_IN
import com.example.alumniapp.utils.Constants.PREF_USER_ID

/**
 * SessionManager handles the authentication state and tokens
 * for the application.
 */
class SessionManager(context: Context) {
    
    private var prefs: SharedPreferences = context.getSharedPreferences(
        "AlumniAppPrefs", Context.MODE_PRIVATE
    )
    
    /**
     * Save auth token and set logged in state
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(PREF_AUTH_TOKEN, token)
        editor.putBoolean(PREF_IS_LOGGED_IN, true)
        editor.apply()
    }
    
    /**
     * Save user ID received from login
     */
    fun saveUserId(userId: Int) {
        val editor = prefs.edit()
        editor.putInt(PREF_USER_ID, userId)
        editor.apply()
    }
    
    /**
     * Get saved auth token
     */
    fun getAuthToken(): String? {
        return prefs.getString(PREF_AUTH_TOKEN, null)
    }
    
    /**
     * Get saved user ID
     */
    fun getUserId(): Int {
        return prefs.getInt(PREF_USER_ID, -1)
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(PREF_IS_LOGGED_IN, false)
    }
    
    /**
     * Get formatted auth token for API requests
     */
    fun getFormattedToken(): String? {
        val token = getAuthToken()
        return if (token != null) "Bearer $token" else null
    }
    
    /**
     * Clear all session data (logout)
     */
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
} 