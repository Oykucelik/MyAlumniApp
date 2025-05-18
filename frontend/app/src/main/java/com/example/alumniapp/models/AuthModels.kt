package com.example.alumniapp.models

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val access_token: String = "",
    val message: String = "",
    val user: User? = null,
    // Keep these for backward compatibility
    val success: Boolean = true,
    val data: LoginData? = null
)

data class User(
    val id: Int = 0,
    val email: String = "",
    val first_name: String = "",
    val last_name: String = "",
    val user_type: String = ""
)

data class LoginData(
    val access_token: String = "",
    val user: User? = null,
)

data class RegisterRequest(
    val first_name: String,
    val last_name: String,
    val email: String,
    val password: String,
    val user_type: String = "student"
)

data class RegisterResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: RegisterWrapper? = null
)

data class RegisterWrapper(
    val access_token: String = "",
    val user: User? = null
)

// Exactly matches the API response structure
data class RegisterResponseWrapper(
    val success: Boolean = true,
    val message: String = "",
    val data: RegisterDataWrapper? = null
)

data class RegisterDataWrapper(
    val access_token: String = "",
    val user: User? = null
)

// Keeping this for backward compatibility
data class RegisterData(
    val access_token: String = "",
    val user: User? = null
)