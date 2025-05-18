package com.example.alumniapp.repository

import com.example.alumniapp.api.ApiClient
import com.example.alumniapp.models.ProfileUpdateRequest
import com.example.alumniapp.models.UserProfile
import com.example.alumniapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository : BaseRepository() {
    private val apiService = ApiClient.apiService
    
    suspend fun getProfile(): Resource<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getProfile()
                handleUserProfileResponse(response)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    
    suspend fun updateProfile(
        userId: Int,
        profileUpdateRequest: ProfileUpdateRequest
    ): Resource<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateProfile(userId, profileUpdateRequest)
                handleResponse(response)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
} 