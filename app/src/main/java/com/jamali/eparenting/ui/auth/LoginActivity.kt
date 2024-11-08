package com.jamali.eparenting.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.R
import com.jamali.eparenting.application.Utility
import com.jamali.eparenting.databinding.ActivityLoginBinding
import com.jamali.eparenting.ui.home.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener { loginController() }
        binding.toRegisterText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginController() {
        val email = binding.emailEt.text.toString()
        val password = binding.passEt.text.toString()

        when {
            email.isEmpty() -> {
                binding.emailEtLayout.error = getString(R.string.required)
            }
            password.isEmpty() -> {
                binding.passEtLayout.error = getString(R.string.required)
            }
            else -> {
                Utility.showLoading(binding.loadingBar, true)
                Utility.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Utility.showLoading(binding.loadingBar, false)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Utility.showLoading(binding.loadingBar, false)
                        Toast.makeText(this, "Status: {${task.exception}}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


}