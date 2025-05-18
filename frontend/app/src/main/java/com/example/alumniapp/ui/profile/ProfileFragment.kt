package com.example.alumniapp.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.alumniapp.R
import com.example.alumniapp.databinding.FragmentProfileBinding
import com.example.alumniapp.models.ProfileUpdateRequest
import com.example.alumniapp.models.UserProfile
import com.example.alumniapp.utils.Constants
import com.example.alumniapp.utils.PreferenceManager
import com.example.alumniapp.utils.Resource
import com.example.alumniapp.viewmodel.AuthViewModel
import com.example.alumniapp.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var authViewModel: AuthViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var preferenceManager: PreferenceManager
    
    private var isFirstLogin = false
    private var resumeUri: Uri? = null
    private var currentProfile: UserProfile? = null
    
    companion object {
        private const val REQUEST_PICK_RESUME = 101
        private const val TAG = "ProfileFragment"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d(TAG, "onViewCreated: Initializing ProfileFragment")
        
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        preferenceManager = PreferenceManager(requireContext())
        
        // Check if this is first login from arguments
        isFirstLogin = arguments?.getBoolean(Constants.KEY_IS_FIRST_LOGIN, false) ?: false
        Log.d(TAG, "onViewCreated: isFirstLogin=$isFirstLogin")
        
        setupListeners()
        observeViewModels()
        
        // Show loading indicator
        showLoading(true)
        
        // Check auth status before loading profile
        checkAuthAndLoadProfile()
    }
    
    private fun checkAuthAndLoadProfile() {
        // First, log all preferences for debugging
        preferenceManager.logAllPreferences()
        
        // Get token directly from PreferenceManager
        val token = preferenceManager.getAuthToken()
        
        Log.d(TAG, "checkAuthAndLoadProfile: Token exists: ${token != null}")
        
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "checkAuthAndLoadProfile: Token is null or empty")
            
            // Check if user is logged in according to preferences
            if (preferenceManager.isLoggedIn()) {
                Log.d(TAG, "checkAuthAndLoadProfile: User is flagged as logged in but token is missing, attempting to fix inconsistent state")
                
                // Fix inconsistent state
                preferenceManager.fixAuthState()
                
                // Get token from AuthViewModel and try to save it again
                val viewModelToken = authViewModel.getAuthToken()
                if (!viewModelToken.isNullOrEmpty()) {
                    Log.d(TAG, "checkAuthAndLoadProfile: Got token from AuthViewModel, saving it")
                    val tokenSaved = preferenceManager.saveAuthToken(viewModelToken)
                    Log.d(TAG, "checkAuthAndLoadProfile: Token saved successfully: $tokenSaved")
                    
                    // Now try to load profile with the saved token
                    profileViewModel.fetchProfile()
                } else {
                    // Still no token - redirect to login
                    Log.e(TAG, "checkAuthAndLoadProfile: Still no token, redirecting to login")
                    navigateToLogin()
                }
            } else {
                // User not logged in, redirect to login
                Log.e(TAG, "checkAuthAndLoadProfile: User not logged in, redirecting to login")
                navigateToLogin()
            }
        } else {
            // We have a token, proceed with profile loading
            Log.d(TAG, "checkAuthAndLoadProfile: Token exists (length: ${token.length}), loading profile")
            profileViewModel.fetchProfile()
        }
    }
    
    private fun navigateToLogin() {
        // Clear any inconsistent login state
        preferenceManager.clearAll()
        
        // Show error message
        Toast.makeText(requireContext(), "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
        
        // Navigate to login screen
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }
    
    private fun logoutUser() {
        // Show confirmation dialog
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // Call logout in view model
                authViewModel.logout()
                
                // Show success message
                Toast.makeText(requireContext(), "Successfully logged out", Toast.LENGTH_SHORT).show()
                
                // Navigate to login
                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ProfileFragment resumed")
        
        // Check auth before fetching profile on resume
        checkAuthAndLoadProfile()
    }
    
    private fun setupListeners() {
        // Logout button
        binding.logoutButton.setOnClickListener {
            logoutUser()
        }
        
        // Settings button
        binding.settingsButton.setOnClickListener {
            // TODO: Implement settings functionality
            Toast.makeText(requireContext(), "Settings clicked", Toast.LENGTH_SHORT).show()
        }
        
        // Upload resume button
        binding.uploadResumeButton.setOnClickListener {
            openFileChooser()
        }
        
        // Profile picture click
        binding.profileImageView.setOnClickListener {
            // TODO: Implement profile picture change
            Toast.makeText(requireContext(), "Change profile picture", Toast.LENGTH_SHORT).show()
        }
        
        // Save button click
        binding.saveProfileButton.setOnClickListener {
            saveProfileData()
        }
    }
    
    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            startActivityForResult(
                Intent.createChooser(intent, "Select a resume PDF"),
                REQUEST_PICK_RESUME
            )
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Please install a file manager", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_PICK_RESUME && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                resumeUri = uri
                Toast.makeText(requireContext(), "Resume selected", Toast.LENGTH_SHORT).show()
                // TODO: Upload the resume to the server
            }
        }
    }
    
    private fun observeViewModels() {
        // Observe profile data
        profileViewModel.profileData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    Log.d(TAG, "Profile data: Loading")
                    showLoading(true)
                }
                is Resource.Success -> {
                    Log.d(TAG, "Profile data: Success - ${resource.data}")
                    showLoading(false)
                    resource.data?.let {
                        currentProfile = it
                        populateProfileData(it)
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "Profile data: Error - ${resource.message}")
                    showLoading(false)
                    
                    // Check if the error is an auth error and handle it
                    if (resource.message?.contains("Authentication token not found") == true || 
                        resource.message?.contains("Unauthorized") == true || 
                        resource.errorCode == 401) {
                        navigateToLogin()
                    } else {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        // Observe profile update result
        profileViewModel.updateProfileResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.saveProfileButton.isEnabled = false
                    binding.saveProgressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.saveProfileButton.isEnabled = true
                    binding.saveProgressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.saveProfileButton.isEnabled = true
                    binding.saveProgressBar.visibility = View.GONE
                    
                    // Check if the error is an auth error and handle it
                    if (resource.message?.contains("Authentication token not found") == true || 
                        resource.message?.contains("Unauthorized") == true || 
                        resource.errorCode == 401) {
                        navigateToLogin()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update profile: ${resource.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun populateProfileData(profile: UserProfile) {
        // Set basic user info
        val fullName = "${profile.first_name} ${profile.last_name}"
        binding.nameTextView.text = fullName
        binding.emailTextView.text = profile.email
        
        // Load profile image if available
        if (profile.profile?.profile_picture != null) {
            Glide.with(this)
                .load(profile.profile.profile_picture)
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .circleCrop()
                .into(binding.profileImageView)
        }
        
        // Fill in profile fields
        binding.aboutEditText.setText(profile.profile?.bio ?: "")
        binding.universityEditText.setText(profile.profile?.university ?: "")
        
        // Use department field from the API response instead of degree
        binding.departmentEditText.setText(profile.profile?.department ?: "")
        
        binding.dateEditText.setText(profile.profile?.graduation_year ?: "")
        binding.linkedinEditText.setText(profile.profile?.linkedin_url ?: "")
        
        // Handle mentorship areas if they exist and user is a mentor
        val isMentor = profile.profile?.is_mentor ?: false
        if (isMentor && !profile.profile?.mentorship_areas.isNullOrEmpty()) {
            binding.mentorshipAreasEditText.setText(profile.profile?.mentorship_areas)
        }
        
        // Set mentor switch state
        binding.mentorSwitch.isChecked = isMentor
    }
    
    private fun saveProfileData() {
        // Basic profile info
        val bio = binding.aboutEditText.text.toString().trim()
        val university = binding.universityEditText.text.toString().trim()
        val department = binding.departmentEditText.text.toString().trim()
        val graduationYear = binding.dateEditText.text.toString().trim()
        val linkedinUrl = binding.linkedinEditText.text.toString().trim()
        val isMentor = binding.mentorSwitch.isChecked
        val mentorshipAreas = binding.mentorshipAreasEditText.text.toString().trim()
        
        // Create profile update request
        val profileUpdateRequest = ProfileUpdateRequest(
            bio = bio,
            university = university,
            department = department, // Use the department field correctly
            graduation_year = graduationYear,
            linkedin_url = linkedinUrl.ifEmpty { null },
            is_mentor = isMentor,
            // Convert mentorship_areas string to list for compatibility
            mentorship_areas = if (mentorshipAreas.isEmpty()) null else listOf(mentorshipAreas)
        )
        
        // Send update request
        profileViewModel.updateProfile(profileUpdateRequest)
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.profileProgressBar.visibility = View.VISIBLE
            binding.profileScrollView.visibility = View.GONE
        } else {
            binding.profileProgressBar.visibility = View.GONE
            binding.profileScrollView.visibility = View.VISIBLE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 