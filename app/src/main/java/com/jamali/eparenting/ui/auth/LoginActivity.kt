package com.jamali.eparenting.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.R
import com.jamali.eparenting.Utility
import com.jamali.eparenting.databinding.ActivityLoginBinding
import com.jamali.eparenting.ui.home.dashboard.HomeActivity

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
                showLoading(true)
                Utility.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showLoading(false)
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "Status: {${task.exception}}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showLoading(state: Boolean) {
        binding.loadingBar.visibility = if (state) View.VISIBLE else View.GONE
    }
}