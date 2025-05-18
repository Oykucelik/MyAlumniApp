package com.example.alumniapp.repository

import com.example.alumniapp.models.*
import com.example.alumniapp.utils.Resource
import retrofit2.Response

abstract class BaseRepository {
    /**
     * Generic function to handle API responses and convert them to Resource objects
     */
    protected fun <T> handleResponse(response: Response<ApiResponse<T>>): Resource<T> {
        if (response.isSuccessful) {
            response.body()?.let { apiResponse ->
                if (apiResponse.success) {
                    apiResponse.data?.let {
                        return Resource.success(it)
                    } ?: return Resource.error("Response data is null")
                } else {
                    val errorMessage = apiResponse.message.ifEmpty { 
                        apiResponse.errors?.firstOrNull()?.get("message") as? String 
                            ?: "Unknown error" 
                    }
                    return Resource.error(errorMessage, response.code())
                }
            } ?: return Resource.error("Response body is null", response.code())
        }
        return Resource.error("Error: ${response.message()}", response.code())
    }
    
    /**
     * Special handler for user profile responses with nested UserWrapper
     */
    protected fun handleUserProfileResponse(response: Response<ApiResponse<UserWrapper>>): Resource<UserProfile> {
        if (response.isSuccessful) {
            response.body()?.let { apiResponse ->
                if (apiResponse.success) {
                    apiResponse.data?.let { wrapper ->
                        return Resource.success(wrapper.user)
                    } ?: return Resource.error("Response data is null")
                } else {
                    val errorMessage = apiResponse.message.ifEmpty { 
                        apiResponse.errors?.firstOrNull()?.get("message") as? String 
                            ?: "Unknown error" 
                    }
                    return Resource.error(errorMessage, response.code())
                }
            } ?: return Resource.error("Response body is null", response.code())
        }
        return Resource.error("Error: ${response.message()}", response.code())
    }
    
    /**
     * Special handler for registration responses
     */
    protected fun handleRegisterResponse(response: Response<ApiResponse<RegisterResponse>>): Resource<RegisterResponse> {
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success) {
                // Get top-level data from API response
                val apiResponseData = apiResponse.data
                
                // Create a wrapper from the original JSON response structure
                if (apiResponseData != null) {
                    try {
                        // Assuming API returns data = { access_token, user }
                        // Extract these fields from apiResponse.data
                        val accessToken = (apiResponse.data as? Map<*, *>)?.get("access_token") as? String ?: ""
                        val user = (apiResponse.data as? Map<*, *>)?.get("user") as? User
                        
                        // Create a wrapper with the extracted data
                        val wrapper = RegisterWrapper(
                            access_token = accessToken,
                            user = user
                        )
                        
                        // Create a new response with the wrapper
                        val registerResponse = RegisterResponse(
                            success = true,
                            message = apiResponse.message,
                            data = wrapper
                        )
                        
                        return Resource.success(registerResponse)
                    } catch (e: Exception) {
                        return Resource.error("Error parsing registration response: ${e.message}")
                    }
                } else {
                    return Resource.error("Registration data is null")
                }
            } else {
                val errorMessage = apiResponse?.message?.ifEmpty { 
                    apiResponse?.errors?.firstOrNull()?.get("message") as? String 
                        ?: "Unknown error" 
                } ?: "Unknown error"
                return Resource.error(errorMessage, response.code())
            }
        }
        return Resource.error("Error: ${response.message()}", response.code())
    }
    
    /**
     * Handles network exceptions
     */
    protected fun handleException(e: Exception): Resource<Nothing> {
        return Resource.error("Network error: ${e.message}")
    }
} 