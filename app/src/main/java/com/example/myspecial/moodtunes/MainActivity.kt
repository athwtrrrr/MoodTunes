package com.example.myspecial.moodtunes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setupWithNavController(navController)

        // Force proper back stack handling
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.moodSelectFragment -> {
                    // Always pop back to home when home is selected
                    navController.popBackStack(R.id.moodSelectFragment, false)
                    true
                }
                R.id.moodHistoryFragment -> {
                    navController.navigate(R.id.moodHistoryFragment)
                    true
                }
                R.id.moodAnalysisFragment -> {
                    navController.navigate(R.id.moodAnalysisFragment)
                    true
                }
                else -> false
            }
        }
    }
}