package com.jamali.eparenting.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.jamali.eparenting.R
import com.jamali.eparenting.databinding.ActivityMainBinding
import com.marsad.stylishdialogs.StylishAlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNav()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })
    }

    /**
     * with navigation component and bottom navigation
     */
    private fun setupBottomNav() {
        val navController = findNavController(R.id.nav_host_fragment_activity_home)

        val menu = binding.navView
        menu.setMenuOrientation(ChipNavigationBar.MenuOrientation.HORIZONTAL)
        menu.setMenuResource(R.menu.bottom_nav_menu)
        menu.setOnItemSelectedListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in)
                .build()

            when (it) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home,null, navOptions)
                }
                R.id.navigation_forum -> {
                    navController.navigate(R.id.navigation_forum,null, navOptions)
                }
                R.id.navigation_chat -> {
                    navController.navigate(R.id.navigation_chat,null, navOptions)
                }
                R.id.navigation_profile -> {
                    navController.navigate(R.id.navigation_profile,null, navOptions)
                }
            }
        }
    }

    private fun showExitConfirmationDialog() {
        StylishAlertDialog(this, StylishAlertDialog.WARNING)
            .setTitleText("Konfirmasi Keluar")
            .setContentText("Apakah Anda yakin ingin keluar dari aplikasi?")
            .setConfirmText("Ya, keluar")
            .setConfirmClickListener {
                finish()
            }
            .setCancelButton("Tidak", StylishAlertDialog::dismissWithAnimation)
            .show()
    }
}