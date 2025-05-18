package com.example.alumniapp.utils

// A generic class to handle API responses and UI states
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val errorCode: Int? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    
    companion object {
        fun <T> success(data: T): Resource<T> = Success(data)
        fun error(message: String, errorCode: Int? = null): Resource<Nothing> = Error(message, errorCode)
        fun loading(): Resource<Nothing> = Loading
    }
} 