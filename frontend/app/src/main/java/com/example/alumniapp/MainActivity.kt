package com.example.alumniapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.alumniapp.api.ApiClient
import com.example.alumniapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set the Toolbar
        setSupportActionBar(binding.toolbar) // Assuming your toolbar ID in XML is @+id/toolbar
        
        // Initialize ApiClient with session manager
        ApiClient.initialize(applicationContext)
        
        // Set up Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Define AppBarConfiguration - specify top-level destinations
        // These destinations will not show an "up" arrow.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.searchFragment, 
                R.id.chatFragment, R.id.notificationsFragment, R.id.profileFragment
                // Add other top-level destination IDs from your bottom nav or main screens
            )
        )
        
        // Set up ActionBar with NavController
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Set up Bottom Navigation
        // val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation) // No longer needed if using binding
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Hide/show bottom navigation based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment, R.id.loginFragment, R.id.registerFragment -> {
                    binding.toolbar.visibility = android.view.View.GONE // Hide toolbar as well for these screens
                    binding.bottomNavigation.visibility = android.view.View.GONE
                }
                else -> {
                    binding.toolbar.visibility = android.view.View.VISIBLE // Show toolbar for other screens
                    binding.bottomNavigation.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        // Corrected to use NavigationUI.navigateUp
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}