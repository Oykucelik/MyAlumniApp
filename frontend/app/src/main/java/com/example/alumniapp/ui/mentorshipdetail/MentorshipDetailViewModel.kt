package com.example.alumniapp.ui.mentorshipdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.alumniapp.data.model.Mentorship

class MentorshipDetailViewModel : ViewModel() {
    private val _mentorship = MutableLiveData<Mentorship>()
    val mentorship: LiveData<Mentorship> = _mentorship

    fun setMentorship(mentorship: Mentorship) {
        _mentorship.value = mentorship
    }
} 