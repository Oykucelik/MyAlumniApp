package com.example.alumniapp.api

import com.example.alumniapp.models.*
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Authentication Endpoints
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<ApiResponse<LoginResponse>>
    
    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<JsonObject>
    
    // Profile Endpoints
    @GET("auth/me")
    suspend fun getProfile(): Response<ApiResponse<UserWrapper>>
    
    @PUT("profiles/{user_id}")
    suspend fun updateProfile(
        @Path("user_id") userId: Int,
        @Body profileRequest: ProfileUpdateRequest
    ): Response<ApiResponse<UserProfile>>
    
    // Alumni Endpoints
    @GET("alumni")
    suspend fun getAlumni(
        @Query("industry") industry: String? = null,
        @Query("skill") skill: String? = null,
        @Query("location") location: String? = null,
        @Query("page") page: Int = 1
    ): Response<ApiResponse<PagedResponse<AlumniProfile>>>
    
    @GET("alumni/{id}")
    suspend fun getAlumniDetail(
        @Path("id") alumniId: String
    ): Response<ApiResponse<AlumniProfile>>
    
    // Mentorship Endpoints
    @GET("mentorships")
    suspend fun getMentorships(
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<Mentorship>>>
    
    @POST("mentorships")
    suspend fun requestMentorship(
        @Body mentorshipRequest: MentorshipRequest
    ): Response<ApiResponse<Mentorship>>
    
    // Event Endpoints
    @GET("events")
    suspend fun getEvents(
        @Query("page") page: Int = 1
    ): Response<ApiResponse<PagedResponse<Event>>>
    
    @POST("events/{id}/rsvp")
    suspend fun rsvpEvent(
        @Path("id") eventId: String,
        @Body rsvpRequest: RsvpRequest
    ): Response<ApiResponse<RsvpResponse>>
    
    // Job Endpoints
    @GET("jobs")
    suspend fun getJobs(
        @Query("type") type: String? = null,
        @Query("industry") industry: String? = null,
        @Query("page") page: Int = 1
    ): Response<ApiResponse<PagedResponse<Job>>>
    
    @POST("jobs/{id}/apply")
    suspend fun applyJob(
        @Path("id") jobId: String
    ): Response<ApiResponse<JobApplicationResponse>>
} 