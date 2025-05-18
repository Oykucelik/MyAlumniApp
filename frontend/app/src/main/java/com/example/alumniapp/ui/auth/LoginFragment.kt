package com.example.alumniapp.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.alumniapp.R
import com.example.alumniapp.databinding.FragmentLoginBinding
import com.example.alumniapp.utils.Constants
import com.example.alumniapp.utils.Resource
import com.example.alumniapp.viewmodel.AuthViewModel

class LoginFragment : Fragment() {
    
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var authViewModel: AuthViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        
        setupListeners()
        observeViewModels()
    }
    
    private fun setupListeners() {
        // Login button click listener
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            
            if (validateInputs(email, password)) {
                authViewModel.login(email, password)
            }
        }
        
        // Register text click listener
        binding.registerPromptText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
    
    private fun observeViewModels() {
        // Observe login results
        authViewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    Log.d("LoginFragment", "Login success: ${result.data}")
                    showLoading(false)
                    
                    // For new response format
                    if (result.data.access_token.isNotEmpty()) {
                        Log.d("LoginFragment", "Using new response format")
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    } 
                    // For old response format
                    else if (result.data.data != null) {
                        Log.d("LoginFragment", "Using old response format")
                        // Navigate to home screen - no is_first_login information available
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    } 
                    else {
                        Log.e("LoginFragment", "Unexpected response format")
                        Toast.makeText(requireContext(), "Login successful but received unexpected data", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                }
                is Resource.Error -> {
                    Log.e("LoginFragment", "Login error: ${result.message}")
                    showLoading(false)
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun handleLoginSuccess(isFirstLogin: Boolean) {
        if (isFirstLogin) {
            // If first login, navigate to profile setup
            val bundle = Bundle().apply {
                putBoolean(Constants.KEY_IS_FIRST_LOGIN, true)
            }
            findNavController().navigate(R.id.action_loginFragment_to_profileFragment, bundle)
        } else {
            // Otherwise, navigate to home screen
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true
        
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
        
        return isValid
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.loginButton.isEnabled = false
        } else {
            binding.loginProgressBar.visibility = View.GONE
            binding.loginButton.isEnabled = true
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 