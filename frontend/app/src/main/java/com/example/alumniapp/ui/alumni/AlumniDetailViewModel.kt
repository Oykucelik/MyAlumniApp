package com.example.alumniapp.ui.alumni

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alumniapp.models.Alumni
import com.example.alumniapp.repository.AlumniRepository
import com.example.alumniapp.utils.Resource
import kotlinx.coroutines.launch

class AlumniDetailViewModel : ViewModel() {

    private val repository = AlumniRepository()
    
    private val _alumniDetail = MutableLiveData<Resource<Alumni>>()
    val alumniDetail: LiveData<Resource<Alumni>> = _alumniDetail
    
    fun getAlumniDetails(alumniId: String) {
        _alumniDetail.value = Resource.loading()
        
        viewModelScope.launch {
            try {
                // In a real app, you would call the API to get details for a specific alumni
                // val result = repository.getAlumniById(alumniId)
                
                // For now, using mock data
                val alumni = repository.getAllAlumni().find { it.id == alumniId }
                
                if (alumni != null) {
                    _alumniDetail.value = Resource.success(alumni)
                } else {
                    _alumniDetail.value = Resource.error("Alumni not found")
                }
            } catch (e: Exception) {
                _alumniDetail.value = Resource.error(e.message ?: "Unknown error occurred")
            }
        }
    }
} 