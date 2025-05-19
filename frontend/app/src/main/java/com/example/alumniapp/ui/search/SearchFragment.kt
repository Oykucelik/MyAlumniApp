package com.example.alumniapp.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.alumniapp.R
import com.example.alumniapp.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }
    
    private fun setupListeners() {
        // Alumni section button
        binding.exploreAlumniButton.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_alumniListFragment)
        }
        
        // Events button
        binding.viewEventsButton.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_eventsFragment)
        }
        
        // Jobs button
        binding.viewJobsButton.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_jobsFragment)
        }
        
        // Mentorship button
        binding.mentorshipButton.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_mentorshipFragment)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 