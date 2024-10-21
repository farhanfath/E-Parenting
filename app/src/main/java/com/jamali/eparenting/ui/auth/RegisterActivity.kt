package com.jamali.eparenting.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.R
import com.jamali.eparenting.data.repository.AppRepository
import com.jamali.eparenting.databinding.ActivityRegisterBinding
import com.jamali.eparenting.utils.ViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(AppRepository())
    }

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
                viewModel.registerUser(username, email, password) { result ->
                    showLoading(false)
                    if (result.isSuccess) {
                        Toast.makeText(this, "User registration success", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "User registration failed", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }

    private fun showLoading(state: Boolean) {
        binding.loadingBar.visibility = if (state) View.VISIBLE else View.GONE
    }
}