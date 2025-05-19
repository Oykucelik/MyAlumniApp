package com.example.alumniapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Job(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val description: String,
    var isApplied: Boolean = false
) : Parcelable 