package com.example.alumniapp.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.alumniapp.R
import com.example.alumniapp.databinding.FragmentSplashBinding
import com.example.alumniapp.viewmodel.AuthViewModel

class SplashFragment : Fragment() {
    
    private val TAG = "SplashFragment"
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private lateinit var authViewModel: AuthViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d(TAG, "onViewCreated: Initializing SplashFragment")
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        
        // Delay for splash screen display
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 2000) // 2 seconds delay
    }
    
    private fun checkLoginStatus() {
        Log.d(TAG, "checkLoginStatus: Checking login status")
        
        // First check if the user is marked as logged in
        val isLoggedIn = authViewModel.isLoggedIn()
        Log.d(TAG, "checkLoginStatus: isLoggedIn = $isLoggedIn")
        
        if (isLoggedIn) {
            // User is logged in, verify token
            val token = authViewModel.getAuthToken()
            
            if (token != null) {
                Log.d(TAG, "checkLoginStatus: Valid token found, navigating to home")
                Toast.makeText(requireContext(), "Welcome back!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
            } else {
                // This shouldn't happen with our improved isLoggedIn check,
                // but we'll handle it just in case
                Log.e(TAG, "checkLoginStatus: Token missing despite user being logged in")
                Toast.makeText(requireContext(), "Please log in again", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            }
        } else {
            // Not logged in, navigate to login
            Log.d(TAG, "checkLoginStatus: No valid login found, navigating to login")
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 