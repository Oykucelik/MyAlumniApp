package com.example.alumniapp.ui.alumni

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alumniapp.models.Alumni
import com.example.alumniapp.repository.AlumniRepository
import com.example.alumniapp.utils.Resource
import kotlinx.coroutines.launch

class AlumniViewModel : ViewModel() {

    private val repository = AlumniRepository()
    
    private val _alumniData = MutableLiveData<Resource<List<Alumni>>>()
    val alumniData: LiveData<Resource<List<Alumni>>> = _alumniData
    
    fun fetchAlumni() {
        _alumniData.value = Resource.loading()
        
        viewModelScope.launch {
            try {
                val result = repository.getAllAlumni()
                _alumniData.value = Resource.success(result)
            } catch (e: Exception) {
                _alumniData.value = Resource.error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun searchAlumni(query: String) {
        _alumniData.value = Resource.loading()
        
        viewModelScope.launch {
            try {
                val result = repository.searchAlumni(query)
                _alumniData.value = Resource.success(result)
            } catch (e: Exception) {
                _alumniData.value = Resource.error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun filterAlumni(department: String? = null, graduationYear: Int? = null) {
        _alumniData.value = Resource.loading()
        
        viewModelScope.launch {
            try {
                val result = repository.filterAlumni(department, graduationYear)
                _alumniData.value = Resource.success(result)
            } catch (e: Exception) {
                _alumniData.value = Resource.error(e.message ?: "Unknown error occurred")
            }
        }
    }
} 