package com.example.alumniapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.alumniapp.R
import com.example.alumniapp.databinding.FragmentHomeBinding
import com.example.alumniapp.viewmodel.AuthViewModel
import com.example.alumniapp.viewmodel.ProfileViewModel

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var authViewModel: AuthViewModel
    private lateinit var profileViewModel: ProfileViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        
        setupListeners()
        
        // Load profile data to display welcome message
        profileViewModel.fetchProfile()
        
        // Observe profile data changes
        profileViewModel.profileData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is com.example.alumniapp.utils.Resource.Success -> {
                    // Show welcome with user name
                    binding.welcomeText.text = "Welcome, ${resource.data.first_name}!"
                }
                is com.example.alumniapp.utils.Resource.Loading -> {
                    // Show loading state if needed
                }
                is com.example.alumniapp.utils.Resource.Error -> {
                    // Handle error if needed
                    binding.welcomeText.text = "Welcome!"
                }
            }
        }
    }
    
    private fun setupListeners() {
        // Alumni section button
        binding.exploreAlumniButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_alumniListFragment)
        }
        
        // Events button
        binding.viewEventsButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_eventsFragment)
        }
        
        // Jobs button
        binding.viewJobsButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_jobsFragment)
        }
        
        // Mentorship button
        binding.mentorshipButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_mentorshipFragment)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
