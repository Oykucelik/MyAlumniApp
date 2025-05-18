package com.example.alumniapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.alumniapp.R
import com.example.alumniapp.databinding.FragmentRegisterBinding
import com.example.alumniapp.utils.Constants
import com.example.alumniapp.utils.Resource
import com.example.alumniapp.viewmodel.AuthViewModel
import java.util.Calendar

class RegisterFragment : Fragment() {
    
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var authViewModel: AuthViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        
        setupListeners()
        observeViewModels()
    }
    
    private fun setupListeners() {
        // Register button click listener
        binding.registerButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
            val graduationYear = if (binding.graduationYearEditText.text.toString().isNotEmpty()) {
                binding.graduationYearEditText.text.toString().toInt()
            } else {
                null
            }
            
            if (validateInputs(name, email, password, confirmPassword, graduationYear)) {
                authViewModel.register(
                    firstName = name.split(" ").firstOrNull() ?: name,
                    lastName = name.split(" ").drop(1).joinToString(" ").ifEmpty { "" },
                    email = email,
                    password = password,
                    userType = "student" // Default userType, can be updated in profile
                )
            }
        }
        
        // Login text click listener
        binding.loginPromptText.setOnClickListener {
            findNavController().popBackStack()
        }
    }
    
    private fun observeViewModels() {
        // Observe register results
        authViewModel.registerResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    handleRegisterSuccess()
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun handleRegisterSuccess() {
        // Clear any auth tokens to force the user to login again
        authViewModel.logout()
        
        // Show success message
        Toast.makeText(
            requireContext(),
            "Registration successful! Please login with your credentials.",
            Toast.LENGTH_LONG
        ).show()
        
        // Return to login screen
        findNavController().popBackStack()
    }
    
    private fun validateInputs(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        graduationYear: Int?
    ): Boolean {
        var isValid = true
        
        // Validate name
        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Name is required"
            isValid = false
        } else {
            binding.nameInputLayout.error = null
        }
        
        // Validate email
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Please enter a valid email"
            isValid = false
        } else {
            binding.emailInputLayout.error = null
        }
        
        // Validate password
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.passwordInputLayout.error = null
        }
        
        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.error = "Confirm password is required"
            isValid = false
        } else if (confirmPassword != password) {
            binding.confirmPasswordInputLayout.error = "Passwords do not match"
            isValid = false
        } else {
            binding.confirmPasswordInputLayout.error = null
        }
        
        // Validate graduation year for all users
        if (graduationYear == null) {
            binding.graduationYearInputLayout.error = "Graduation year is required"
            isValid = false
        } else {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            if (graduationYear > currentYear) {
                binding.graduationYearInputLayout.error = "Graduation year cannot be in the future"
                isValid = false
            } else if (graduationYear < 1900) {
                binding.graduationYearInputLayout.error = "Please enter a valid graduation year"
                isValid = false
            } else {
                binding.graduationYearInputLayout.error = null
            }
        }
        
        return isValid
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.registerProgressBar.visibility = View.VISIBLE
            binding.registerButton.isEnabled = false
        } else {
            binding.registerProgressBar.visibility = View.GONE
            binding.registerButton.isEnabled = true
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 