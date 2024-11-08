package com.jamali.eparenting.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.R
import com.jamali.eparenting.application.Utility
import com.jamali.eparenting.data.entity.User
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
                Utility.showLoading(binding.loadingBar,true)
                Utility.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Utility.showLoading(binding.loadingBar,false)
                        val userId = Utility.auth.currentUser?.uid
                        val user = userId?.let { uid ->
                            User(uid, email, username)
                        }
                        userId?.let {
                            Utility.database.reference.child("users").child(it).setValue(user).addOnCompleteListener { userTask ->
                                if (userTask.isSuccessful) {
                                    Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "User registration failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Utility.showLoading(binding.loadingBar,false)
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}