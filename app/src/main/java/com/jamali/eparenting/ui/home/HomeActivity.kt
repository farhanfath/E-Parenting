package com.jamali.eparenting.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.jamali.eparenting.R
import com.jamali.eparenting.Utility
import com.jamali.eparenting.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNav()

    }

    private fun setupBottomNav() {
        val navController = findNavController(R.id.nav_host_fragment_activity_home)

        val menu = binding.navView
        menu.setMenuOrientation(ChipNavigationBar.MenuOrientation.HORIZONTAL)
        menu.setMenuResource(R.menu.bottom_nav_menu)
        menu.setOnItemSelectedListener {
            when (it) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                }
                R.id.navigation_community -> {
                    navController.navigate(R.id.navigation_community)
                }
                R.id.navigation_chat -> {
                    navController.navigate(R.id.navigation_chat)
                }
                R.id.navigation_profile -> {
                    navController.navigate(R.id.navigation_profile)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setUserStatus("online")
    }

    override fun onPause() {
        super.onPause()
        setUserStatus("busy")
    }

    private fun setUserStatus(status: String) {
        val currentUserId = Utility.auth.currentUser?.uid ?: return
        val userRef = Utility.database.getReference("users").child(currentUserId).child("status")
        userRef.setValue(status)
    }
}