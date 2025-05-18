package com.example.alumniapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

class PreferenceManager(context: Context) {
    
    private val TAG = "PreferenceManager"
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "AlumniAppPrefs", Context.MODE_PRIVATE
    )
    
    // Auth Token
    fun saveAuthToken(token: String): Boolean {
        if (token.isBlank()) {
            Log.e(TAG, "saveAuthToken: Attempted to save blank token")
            return false
        }
        
        try {
            sharedPreferences.edit {
                putString(Constants.PREF_AUTH_TOKEN, token)
                apply()
            }
            
            // Verify the token was saved correctly
            val savedToken = sharedPreferences.getString(Constants.PREF_AUTH_TOKEN, null)
            val success = savedToken == token
            
            if (success) {
                Log.d(TAG, "saveAuthToken: Token saved successfully, length: ${token.length}")
            } else {
                Log.e(TAG, "saveAuthToken: Failed to save token correctly")
            }
            
            return success
        } catch (e: Exception) {
            Log.e(TAG, "saveAuthToken: Exception while saving token", e)
            return false
        }
    }
    
    fun getAuthToken(): String? {
        val token = sharedPreferences.getString(Constants.PREF_AUTH_TOKEN, null)
        Log.d(TAG, "getAuthToken: Token exists: ${token != null}, ${if (token != null) "length: ${token.length}" else ""}")
        return token
    }
    
    // User ID
    fun saveUserId(userId: String): Boolean {
        if (userId.isBlank()) {
            Log.e(TAG, "saveUserId: Attempted to save blank user ID")
            return false
        }
        
        try {
            sharedPreferences.edit {
                putString(Constants.PREF_USER_ID, userId)
                apply()
            }
            
            // Verify the user ID was saved correctly
            val savedUserId = sharedPreferences.getString(Constants.PREF_USER_ID, null)
            val success = savedUserId == userId
            
            if (success) {
                Log.d(TAG, "saveUserId: User ID saved successfully: $userId")
            } else {
                Log.e(TAG, "saveUserId: Failed to save user ID correctly")
            }
            
            return success
        } catch (e: Exception) {
            Log.e(TAG, "saveUserId: Exception while saving user ID", e)
            return false
        }
    }
    
    fun getUserId(): String? {
        val userId = sharedPreferences.getString(Constants.PREF_USER_ID, null)
        Log.d(TAG, "getUserId: User ID exists: ${userId != null}, ${if (userId != null) "ID: $userId" else ""}")
        return userId
    }
    
    // Login Status
    fun setLoggedIn(isLoggedIn: Boolean): Boolean {
        try {
            sharedPreferences.edit {
                putBoolean(Constants.PREF_IS_LOGGED_IN, isLoggedIn)
                apply()
            }
            
            // Verify the login status was saved correctly
            val savedStatus = sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false)
            val success = savedStatus == isLoggedIn
            
            if (success) {
                Log.d(TAG, "setLoggedIn: Login status saved successfully: $isLoggedIn")
            } else {
                Log.e(TAG, "setLoggedIn: Failed to save login status correctly")
            }
            
            return success
        } catch (e: Exception) {
            Log.e(TAG, "setLoggedIn: Exception while saving login status", e)
            return false
        }
    }
    
    fun isLoggedIn(): Boolean {
        val status = sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false)
        Log.d(TAG, "isLoggedIn: Status: $status")
        return status
    }
    
    // Clear all data (for logout)
    fun clearAll() {
        try {
            sharedPreferences.edit {
                clear()
                apply()
            }
            Log.d(TAG, "clearAll: All preferences cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "clearAll: Exception while clearing preferences", e)
        }
    }
    
    // Validate auth state - ensures login flag and token are consistent
    fun validateAuthState(): Boolean {
        val isLoggedIn = isLoggedIn()
        val hasToken = getAuthToken() != null
        
        val isValid = isLoggedIn == hasToken
        if (!isValid) {
            Log.e(TAG, "validateAuthState: Inconsistent auth state - isLoggedIn: $isLoggedIn, hasToken: $hasToken")
        } else {
            Log.d(TAG, "validateAuthState: Auth state is consistent - isLoggedIn: $isLoggedIn, hasToken: $hasToken")
        }
        
        return isValid
    }
    
    // Fix inconsistent auth state
    fun fixAuthState() {
        val token = getAuthToken()
        if (token != null && !isLoggedIn()) {
            // We have a token but not logged in flag
            setLoggedIn(true)
            Log.d(TAG, "fixAuthState: Fixed - Set logged in flag to true")
        } else if (token == null && isLoggedIn()) {
            // We have logged in flag but no token
            setLoggedIn(false)
            Log.d(TAG, "fixAuthState: Fixed - Set logged in flag to false")
        }
    }
    
    // For formatted auth header
    fun getFormattedToken(): String? {
        val token = getAuthToken()
        return if (token != null) "Bearer $token" else null
    }
    
    // Add this method at the end of the class
    fun logAllPreferences() {
        Log.d(TAG, "========== STORED PREFERENCES ==========")
        Log.d(TAG, "AUTH_TOKEN: ${getAuthToken()?.let { "exists (length: ${it.length})" } ?: "null"}")
        Log.d(TAG, "USER_ID: ${getUserId() ?: "null"}")
        Log.d(TAG, "IS_LOGGED_IN: ${isLoggedIn()}")
        Log.d(TAG, "=======================================")
    }
} 