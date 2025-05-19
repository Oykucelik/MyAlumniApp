package com.example.alumniapp.model

data class Post(
    val id: String,
    val username: String,
    val location: String,
    val avatarUrl: String?, // Nullable if avatar can be absent
    val imageUrl: String,   // Assuming post image is mandatory
    val description: String,
    val likes: Int = 0,
    val comments: Int = 0,
    val shares: Int = 0
) 