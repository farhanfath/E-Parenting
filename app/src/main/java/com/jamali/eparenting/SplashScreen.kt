package com.jamali.eparenting

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.ui.WelcomeActivity
import com.jamali.eparenting.ui.admin.AdminHomeActivity
import com.jamali.eparenting.ui.auth.LoginActivity
import com.jamali.eparenting.ui.customer.CustomerMainActivity
import com.jamali.eparenting.ui.doctor.DoctorHomeActivity
import com.jamali.eparenting.utils.Utility
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        MainScope().launch {
            val currentUser = Utility.auth.currentUser

            if (currentUser != null) {
                // Check user role in Firebase Realtime Database
                val userRef = Utility.database.reference
                    .child("users")
                    .child(currentUser.uid)

                userRef.child("role").get().addOnSuccessListener { snapshot ->
                    val userRole = snapshot.value as? String

                    val intent = when (userRole) {
                        "admin" -> Intent(this@SplashScreen, AdminHomeActivity::class.java)
                        "customer" -> Intent(this@SplashScreen, CustomerMainActivity::class.java)
                        "doctor" -> Intent(this@SplashScreen, DoctorHomeActivity::class.java)
                        else -> Intent(this@SplashScreen, WelcomeActivity::class.java)
                    }

                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    // If role retrieval fails, default to login
                    startActivity(Intent(this@SplashScreen, WelcomeActivity::class.java))
                    finish()
                }
            } else {
                startActivity(Intent(this@SplashScreen, WelcomeActivity::class.java))
                finish()
            }
        }
    }
}