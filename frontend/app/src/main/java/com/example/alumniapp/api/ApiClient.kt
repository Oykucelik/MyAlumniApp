package com.example.alumniapp.api

import android.content.Context
import com.example.alumniapp.utils.Constants
import com.example.alumniapp.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    
    private lateinit var sessionManager: SessionManager
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // Auth interceptor to automatically add the token to requests
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        
        // If token exists and the request requires auth, add the token
        val token = sessionManager.getAuthToken()
        if (token != null && needsAuthentication(originalRequest)) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return@Interceptor chain.proceed(newRequest)
        }
        
        chain.proceed(originalRequest)
    }
    
    private fun needsAuthentication(request: Request): Boolean {
        // Skip authentication for login and register endpoints
        val url = request.url.toString()
        return !url.contains("auth/login") && !url.contains("auth/register")
    }
    
    // Initialize with application context
    fun initialize(context: Context) {
        sessionManager = SessionManager(context)
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
    
    // Helper method to get formatted auth token
    fun getAuthToken(): String? = sessionManager.getFormattedToken()
} 