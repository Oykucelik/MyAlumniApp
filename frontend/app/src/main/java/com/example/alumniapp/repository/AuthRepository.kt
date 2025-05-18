package com.example.alumniapp.repository

import android.content.Context
import android.util.Log
import com.example.alumniapp.api.ApiClient
import com.example.alumniapp.models.*
import com.example.alumniapp.utils.Resource
import com.example.alumniapp.utils.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) : BaseRepository() {
    private val apiService = ApiClient.apiService
    private val sessionManager = SessionManager(context)
    private val gson = Gson()
    private val TAG = "AuthRepository"
    
    suspend fun login(email: String, password: String): Resource<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val loginRequest = LoginRequest(email, password)
                val response = apiService.login(loginRequest)
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    
                    // Get token from response
                    if (loginResponse?.data?.data?.access_token?.isNotEmpty() == true) {
                        sessionManager.saveAuthToken(loginResponse.data.data.access_token)
                    }
                    
                    // Get user ID from response
                    loginResponse?.data?.data?.user?.id?.let { 
                        sessionManager.saveUserId(it) 
                    }
                }
                handleResponse(response)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        userType: String = "student"
    ): Resource<RegisterResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val registerRequest = RegisterRequest(
                    first_name = firstName,
                    last_name = lastName,
                    email = email,
                    password = password,
                    user_type = userType
                )
                
                val response = apiService.register(registerRequest)
                
                if (response.isSuccessful) {
                    try {
                        val jsonResponse = response.body()
                        if (jsonResponse != null) {
                            Log.d(TAG, "Raw JSON response: $jsonResponse")
                            
                            // Extract values directly from JSON
                            val success = jsonResponse.get("success")?.asBoolean ?: true
                            val message = jsonResponse.get("message")?.asString ?: ""
                            
                            // Get data object
                            val dataObject = jsonResponse.getAsJsonObject("data")
                            
                            if (dataObject != null) {
                                val accessToken = dataObject.get("access_token")?.asString ?: ""
                                val userObject = dataObject.getAsJsonObject("user")
                                
                                // Parse user information
                                var userId = 0
                                var userEmail = ""
                                var userFirstName = ""
                                var userLastName = ""
                                var userType = ""
                                
                                if (userObject != null) {
                                    userId = userObject.get("id")?.asInt ?: 0
                                    userEmail = userObject.get("email")?.asString ?: ""
                                    userFirstName = userObject.get("first_name")?.asString ?: ""
                                    userLastName = userObject.get("last_name")?.asString ?: ""
                                    userType = userObject.get("user_type")?.asString ?: ""
                                }
                                
                                // Save token and user ID to session
                                if (accessToken.isNotEmpty()) {
                                    sessionManager.saveAuthToken(accessToken)
                                }
                                
                                if (userId > 0) {
                                    sessionManager.saveUserId(userId)
                                }
                                
                                // Create user object
                                val user = User(
                                    id = userId,
                                    email = userEmail,
                                    first_name = userFirstName,
                                    last_name = userLastName,
                                    user_type = userType
                                )
                                
                                // Create wrapper and response
                                val wrapper = RegisterWrapper(
                                    access_token = accessToken,
                                    user = user
                                )
                                
                                val registerResponse = RegisterResponse(
                                    success = success,
                                    message = message,
                                    data = wrapper
                                )
                                
                                return@withContext Resource.success(registerResponse)
                            } else {
                                return@withContext Resource.error("Data object is null in response")
                            }
                        } else {
                            return@withContext Resource.error("Empty response body")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing register response", e)
                        return@withContext Resource.error("Error parsing response: ${e.message}")
                    }
                } else {
                    return@withContext Resource.error("Error: ${response.message()}", response.code())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in register call", e)
                return@withContext handleException(e)
            }
        }
    }
    
    // Logout method
    fun logout() {
        sessionManager.clearSession()
    }
    
    // Check if user is logged in
    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
} 