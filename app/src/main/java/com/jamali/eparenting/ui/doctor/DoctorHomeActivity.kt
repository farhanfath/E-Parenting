package com.jamali.eparenting.ui.doctor

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.jamali.eparenting.R
import com.jamali.eparenting.databinding.ActivityDoctorHomeBinding

class DoctorHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNav()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupBottomNav() {
        val navController = findNavController(R.id.nav_host_fragment_activity_doctor)
        val menu = binding.navView
        menu.setMenuOrientation(ChipNavigationBar.MenuOrientation.HORIZONTAL)
        menu.setMenuResource(R.menu.doctor_nav_menu)

        menu.setItemSelected(R.id.nav_doctor_consultation_menu)
        menu.setOnItemSelectedListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in)
                .build()

            when (it) {
                R.id.nav_doctor_consultation_menu -> {
                    navController.navigate(R.id.nav_doctor_consultation_menu,null, navOptions)
                }
                R.id.nav_doctor_community_menu -> {
                    navController.navigate(R.id.nav_doctor_community_menu,null, navOptions)
                }
                R.id.nav_doctor_profile_menu -> {
                    navController.navigate(R.id.nav_doctor_profile_menu,null, navOptions)
                }
            }
        }
    }
}