package com.example.alumniapp.ui.alumni

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.alumniapp.databinding.FragmentAlumniDetailBinding
import com.example.alumniapp.models.Alumni
import com.example.alumniapp.utils.Resource

class AlumniDetailFragment : Fragment() {

    private var _binding: FragmentAlumniDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AlumniDetailViewModel
    private val args: AlumniDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlumniDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupListeners()
        
        // Load alumni details using the ID from navigation args
        viewModel.getAlumniDetails(args.alumniId)
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[AlumniDetailViewModel::class.java]
        
        viewModel.alumniDetail.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    showLoading(false)
                    displayAlumniDetails(resource.data)
                }
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.sendMessageButton.setOnClickListener {
            // Implement sending message functionality
            Toast.makeText(requireContext(), "Message feature will be implemented soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun displayAlumniDetails(alumni: Alumni) {
        with(binding) {
            nameTextView.text = "${alumni.firstName} ${alumni.lastName}"
            positionTextView.text = "${alumni.position ?: "No position"} at ${alumni.company ?: "No company"}"
            departmentTextView.text = "Department: ${alumni.department}"
            graduationYearTextView.text = "Class of ${alumni.graduationYear}"
            locationTextView.text = "Location: ${alumni.location ?: "N/A"}"
            emailTextView.text = alumni.email
            
            bioTextView.text = alumni.bio ?: "No bio provided"
            
            // Display skills
            if (alumni.skills.isNotEmpty()) {
                val skillsText = alumni.skills.joinToString(", ")
                skillsTextView.text = "Skills: $skillsText"
                skillsTextView.visibility = View.VISIBLE
            } else {
                skillsTextView.visibility = View.GONE
            }
            
            // TODO: Load profile image when available
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 