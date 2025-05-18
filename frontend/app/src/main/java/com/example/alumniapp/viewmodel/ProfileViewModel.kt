package com.example.alumniapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.alumniapp.models.ProfileUpdateRequest
import com.example.alumniapp.models.UserProfile
import com.example.alumniapp.repository.ProfileRepository
import com.example.alumniapp.utils.PreferenceManager
import com.example.alumniapp.utils.Resource
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "ProfileViewModel"
    private val profileRepository = ProfileRepository()
    private val preferenceManager = PreferenceManager(application)
    
    private val _profileData = MutableLiveData<Resource<UserProfile>>()
    val profileData: LiveData<Resource<UserProfile>> = _profileData
    
    private val _updateProfileResult = MutableLiveData<Resource<UserProfile>>()
    val updateProfileResult: LiveData<Resource<UserProfile>> = _updateProfileResult
    
    fun fetchProfile() {
        Log.d(TAG, "fetchProfile: Starting to fetch profile")
        _profileData.value = Resource.loading()
        
        viewModelScope.launch {
            try {
                val token = preferenceManager.getAuthToken()
                
                if (token != null) {
                    Log.d(TAG, "fetchProfile: Token exists, making API request")
                    
                    val result = profileRepository.getProfile()
                    
                    when (result) {
                        is Resource.Success -> {
                            Log.d(TAG, "fetchProfile: Success - received profile data: ${result.data}")
                            _profileData.postValue(result)
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "fetchProfile: Error - ${result.message}, code: ${result.errorCode}")
                            
                            // Special handling for token-related errors
                            if (result.errorCode == 422 || result.errorCode == 401) {
                                Log.e(TAG, "fetchProfile: Token validation error, attempting to refresh login state")
                                preferenceManager.clearAll()
                                _profileData.postValue(Resource.error("Authentication error: Please log in again"))
                            } else {
                                _profileData.postValue(result)
                            }
                        }
                        is Resource.Loading -> {
                            // This shouldn't happen, but handle just in case
                            Log.d(TAG, "fetchProfile: Loading state returned from repository")
                            _profileData.postValue(Resource.loading())
                        }
                    }
                } else {
                    Log.e(TAG, "fetchProfile: Authentication token not found")
                    _profileData.postValue(Resource.error("Authentication token not found"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchProfile: Exception occurred", e)
                _profileData.postValue(Resource.error("Error: ${e.message}"))
            }
        }
    }
    
    fun updateProfile(profileUpdateRequest: ProfileUpdateRequest) {
        _updateProfileResult.value = Resource.loading()
        viewModelScope.launch {
            try {
                val token = preferenceManager.getAuthToken()
                val userId = preferenceManager.getUserId()?.toIntOrNull()
                
                Log.d(TAG, "updateProfile: Token exists: ${token != null}, User ID: $userId")
                
                if (token != null && userId != null) {
                    Log.d(TAG, "updateProfile: Making API request to update profile for user ID: $userId")
                    val result = profileRepository.updateProfile(userId, profileUpdateRequest)
                    
                    // Update profile data if update was successful
                    if (result is Resource.Success) {
                        _profileData.postValue(result)
                    }
                    
                    _updateProfileResult.postValue(result)
                } else if (token == null) {
                    Log.e(TAG, "updateProfile: Authentication token not found")
                    _updateProfileResult.postValue(Resource.error("Authentication token not found"))
                } else {
                    Log.e(TAG, "updateProfile: User ID not found or invalid")
                    _updateProfileResult.postValue(Resource.error("User ID not found or invalid"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateProfile: Exception occurred", e)
                _updateProfileResult.postValue(Resource.error("Error: ${e.message}"))
            }
        }
    }
    
    // Method to manually set a token - useful for debugging
    fun setAuthToken(token: String): Boolean {
        return try {
            preferenceManager.saveAuthToken(token)
            preferenceManager.setLoggedIn(true)
            Log.d(TAG, "setAuthToken: Token saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "setAuthToken: Failed to save token", e)
            false
        }
    }
} 