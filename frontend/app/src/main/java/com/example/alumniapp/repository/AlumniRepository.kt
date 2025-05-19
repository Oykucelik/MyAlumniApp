package com.example.alumniapp.repository

import com.example.alumniapp.models.Alumni
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlumniRepository {
    
    suspend fun getAllAlumni(): List<Alumni> = withContext(Dispatchers.IO) {
        // In a real implementation, this would call your API service
        // For now, using mock data for demonstration
        return@withContext getMockAlumniData()
    }
    
    suspend fun searchAlumni(query: String): List<Alumni> = withContext(Dispatchers.IO) {
        // In a real implementation, you might call a specific search endpoint
        // For demo, filtering the mock data
        return@withContext getMockAlumniData().filter { 
            "${it.firstName} ${it.lastName}".contains(query, ignoreCase = true) 
        }
    }
    
    suspend fun filterAlumni(department: String? = null, graduationYear: Int? = null): List<Alumni> = 
        withContext(Dispatchers.IO) {
            // In a real implementation, you would call a filtered API endpoint
            var filteredList = getMockAlumniData()
            
            department?.let { dept ->
                filteredList = filteredList.filter { it.department == dept }
            }
            
            graduationYear?.let { year ->
                filteredList = filteredList.filter { it.graduationYear == year }
            }
            
            return@withContext filteredList
        }
    
    private fun getMockAlumniData(): List<Alumni> {
        // This is temporary mock data for demonstration
        return listOf(
            Alumni(
                id = "1",
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com",
                graduationYear = 2020,
                department = "Computer Science",
                company = "Tech Corp",
                position = "Software Engineer",
                location = "New York"
            ),
            Alumni(
                id = "2",
                firstName = "Jane",
                lastName = "Smith",
                email = "jane.smith@example.com",
                graduationYear = 2019,
                department = "Business Administration",
                company = "Finance Inc",
                position = "Project Manager",
                location = "Chicago"
            ),
            Alumni(
                id = "3",
                firstName = "Michael",
                lastName = "Johnson",
                email = "michael.j@example.com",
                graduationYear = 2021,
                department = "Computer Science",
                company = "Tech Solutions",
                position = "Mobile Developer",
                location = "San Francisco"
            ),
            Alumni(
                id = "4",
                firstName = "Emily",
                lastName = "Williams",
                email = "emily.w@example.com",
                graduationYear = 2018,
                department = "Electrical Engineering",
                company = "Power Systems",
                position = "Hardware Engineer",
                location = "Boston"
            ),
            Alumni(
                id = "5",
                firstName = "David",
                lastName = "Brown",
                email = "david.b@example.com",
                graduationYear = 2020,
                department = "Mechanical Engineering",
                company = "Auto Innovations",
                position = "Design Engineer",
                location = "Detroit"
            ),
            Alumni(
                id = "6",
                firstName = "Sarah",
                lastName = "Miller",
                email = "sarah.m@example.com",
                graduationYear = 2021,
                department = "Computer Science",
                company = "Mobile Apps Inc",
                position = "Android Developer",
                location = "Seattle"
            ),
            Alumni(
                id = "7",
                firstName = "James",
                lastName = "Wilson",
                email = "james.w@example.com",
                graduationYear = 2019,
                department = "Mathematics",
                company = "Data Analytics",
                position = "Data Scientist",
                location = "Austin"
            ),
            Alumni(
                id = "8",
                firstName = "Jessica",
                lastName = "Taylor",
                email = "jessica.t@example.com",
                graduationYear = 2020,
                department = "Computer Science",
                company = "Cloud Services",
                position = "Backend Developer",
                location = "Portland"
            )
        )
    }
} 