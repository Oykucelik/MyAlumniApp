package com.example.alumniapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.alumniapp.models.LoginResponse
import com.example.alumniapp.models.RegisterResponse
import com.example.alumniapp.repository.AuthRepository
import com.example.alumniapp.utils.PreferenceManager
import com.example.alumniapp.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "AuthViewModel"
    private val authRepository = AuthRepository(application.applicationContext)
    private val preferenceManager = PreferenceManager(application)
    
    private val _loginResult = MutableLiveData<Resource<LoginResponse>>()
    val loginResult: LiveData<Resource<LoginResponse>> = _loginResult
    
    private val _registerResult = MutableLiveData<Resource<RegisterResponse>>()
    val registerResult: LiveData<Resource<RegisterResponse>> = _registerResult
    
    init {
        // Check and fix any inconsistent auth state on initialization
        validateAndFixAuthState()
    }
    
    fun login(email: String, password: String) {
        Log.d(TAG, "login: Attempting login for email: $email")
        _loginResult.value = Resource.loading()
        viewModelScope.launch {
            try {
                val result = authRepository.login(email, password)
                
                if (result is Resource.Success) {
                    // Log the entire response for debugging
                    Log.d(TAG, "login: Success response received: ${result.data}")
                    
                    // Check if we have an access_token directly (new format)
                    if (result.data.access_token.isNotEmpty()) {
                        Log.d(TAG, "login: New response format detected with access_token")
                        val token = result.data.access_token
                        
                        // Get user ID from the user object
                        val userId = result.data.user?.id?.toString() ?: ""
                        Log.d(TAG, "login: User ID extracted from response: $userId")
                        
                        if (token.isNotEmpty() && userId.isNotEmpty()) {
                            Log.d(TAG, "login: Valid token and user ID received, saving credentials")
                            val tokenSaved = preferenceManager.saveAuthToken(token)
                            val userIdSaved = preferenceManager.saveUserId(userId)
                            val loggedInSet = preferenceManager.setLoggedIn(true)
                            
                            Log.d(TAG, "login: Credentials saved - Token: $tokenSaved, UserId: $userIdSaved (value: $userId), LoggedIn: $loggedInSet")
                            preferenceManager.validateAuthState()
                        } else if (userId.isEmpty()) {
                            Log.e(TAG, "login: User ID missing in response")
                            _loginResult.postValue(Resource.error("Authentication failed: User ID missing in response"))
                            return@launch
                        } else {
                            Log.e(TAG, "login: Empty token in new format response")
                            _loginResult.postValue(Resource.error("Authentication failed: Empty token received"))
                            return@launch
                        }
                    }
                    // Fallback to old format checking
                    else if (result.data.data?.access_token?.isNotEmpty() == true) {
                        Log.d(TAG, "login: Old response format detected with data.auth_token")
                        val data = result.data.data
                        
                        Log.d(TAG, "login: Login successful, saving credentials")
                        val tokenSaved = preferenceManager.saveAuthToken(data.access_token)
                        val userIdSaved = preferenceManager.saveUserId(data.user?.id.toString())
                        val loggedInSet = preferenceManager.setLoggedIn(true)
                        
                        Log.d(TAG, "login: Credentials saved - Token: $tokenSaved, UserId: $userIdSaved, LoggedIn: $loggedInSet")
                        preferenceManager.validateAuthState()
                    } else {
                        Log.e(TAG, "login: No valid token found in response")
                        _loginResult.postValue(Resource.error("Authentication failed: No token received"))
                        return@launch
                    }
                } else if (result is Resource.Error) {
                    Log.e(TAG, "login: Error response: ${result.message}")
                }
                
                _loginResult.postValue(result)
            } catch (e: Exception) {
                Log.e(TAG, "login: Exception during login", e)
                _loginResult.postValue(Resource.error("Login failed: ${e.message}"))
            }
        }
    }
    
    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        userType: String = "student"
    ) {
        Log.d(TAG, "register: Attempting registration for email: $email")
        _registerResult.value = Resource.loading()
        viewModelScope.launch {
                        try {
                val result = authRepository.register(
                    firstName, lastName, email, password, userType
                )
                
                if (result is Resource.Success) {
                    // Check for data in the response
                    val registerWrapper = result.data.data
                    
                    if (registerWrapper != null && registerWrapper.access_token.isNotEmpty()) {
                        Log.d(TAG, "register: Registration successful, saving credentials")
                        val tokenSaved = preferenceManager.saveAuthToken(registerWrapper.access_token)
                        val userIdSaved = preferenceManager.saveUserId(registerWrapper.user?.id.toString())
                        val loggedInSet = preferenceManager.setLoggedIn(true)
                        
                        Log.d(TAG, "register: Credentials saved - Token: $tokenSaved, UserId: $userIdSaved, LoggedIn: $loggedInSet")
                        
                        // Verify auth state is consistent
                        preferenceManager.validateAuthState()
                    } else {
                        // For debugging
                        Log.e(TAG, "register: Response data issue - RegisterWrapper null: ${registerWrapper == null}, Raw response: ${result.data}")
                        _registerResult.postValue(Resource.error("Registration failed: Invalid response data"))
                        return@launch
                    }
                } else if (result is Resource.Error) {
                    Log.e(TAG, "register: Error - ${result.message}")
                    _registerResult.postValue(result)
                    return@launch
                }
                
                _registerResult.postValue(result)
            } catch (e: Exception) {
                Log.e(TAG, "register: Exception during registration", e)
                _registerResult.postValue(Resource.error("Registration failed: ${e.message}"))
            }
        }
    }
    
    fun logout() {
        Log.d(TAG, "logout: Clearing all user data")
        preferenceManager.clearAll()
    }
    
    fun isLoggedIn(): Boolean {
        val loggedIn = preferenceManager.isLoggedIn()
        val hasToken = getAuthToken() != null
        
        Log.d(TAG, "isLoggedIn: Status from preferences: $loggedIn, Has token: $hasToken")
        
        // Return true only if both conditions are met
        return loggedIn && hasToken
    }
    
    fun getAuthToken(): String? {
        val token = preferenceManager.getAuthToken()
        Log.d(TAG, "getAuthToken: Token exists: ${token != null}")
        return token
    }
    
    private fun validateAndFixAuthState() {
        Log.d(TAG, "validateAndFixAuthState: Checking auth state consistency")
        if (!preferenceManager.validateAuthState()) {
            Log.d(TAG, "validateAndFixAuthState: Fixing inconsistent auth state")
            preferenceManager.fixAuthState()
        }
    }
} 