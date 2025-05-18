package com.example.alumniapp.models

/**
 * A generic API response class to match the backend format
 *
 * Attributes:
 *     success (Boolean): Indicates if the request was successful
 *     message (String): A human-readable message about the response
 *     data (T): The main response payload
 *     errors (List<Map<String, Any>>): List of errors if any occurred
 */
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String = "",
    val data: T? = null,
    val errors: List<Map<String, Any>>? = null
)

/**
 * Wrapper for user data response
 */
data class UserWrapper(
    val user: UserProfile
) 