package com.jamali.eparenting.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.FirebaseUtils
import com.jamali.eparenting.R
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerBtn.setOnClickListener { registerController() }
        binding.toLoginText.setOnClickListener {
            finish()
        }
    }

    private fun registerController() {
        val username = binding.usernameEt.text.toString()
        val email = binding.emailEt.text.toString()
        val password = binding.passEt.text.toString()

        when {
            username.isEmpty() -> {
                binding.usernameEtLayout.error = getString(R.string.required)
            }
            email.isEmpty() -> {
                binding.emailEtLayout.error = getString(R.string.required)
            }
            password.isEmpty() -> {
                binding.passEtLayout.error = getString(R.string.required)
            }
            else -> {
                showLoading(true)
                FirebaseUtils.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        processUserDataToDatabase(username, email)
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "User registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun processUserDataToDatabase(username: String, email: String) {
        val userData = User(email, username)
        val userId = FirebaseUtils.auth.currentUser?.uid

        userId?.let {
            FirebaseUtils.database.child("users").child(it).setValue(userData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showLoading(false)
                    Toast.makeText(this, "User registration success", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun showLoading(state: Boolean) {
        binding.loadingBar.visibility = if (state) View.VISIBLE else View.GONE
    }
}