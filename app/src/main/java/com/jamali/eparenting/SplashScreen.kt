package com.jamali.eparenting

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.ui.auth.LoginActivity
import com.jamali.eparenting.ui.home.MainActivity
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
            MainScope().launch {
                if (currentUser != null) {
                    startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@SplashScreen, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }
}