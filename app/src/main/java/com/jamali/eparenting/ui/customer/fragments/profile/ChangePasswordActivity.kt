package com.jamali.eparenting.ui.customer.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.jamali.eparenting.utils.Utility
import com.jamali.eparenting.utils.Utility.auth
import com.jamali.eparenting.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun changePassword() {
        val currentPassword = binding.edtCurrentPassword.text.toString()
        val newPassword = binding.edtNewPassword.text.toString()
        val confirmPassword = binding.edtConfirmPassword.text.toString()

        // Validasi input
        if (!validateInput(currentPassword, newPassword, confirmPassword)) {
            return
        }

        showLoading(true)

        // Get current user email
        val user = auth.currentUser
        val email = user?.email

        if (user != null && email != null) {
            // Re-authenticate user before changing password
            val credential = EmailAuthProvider.getCredential(email, currentPassword)

            user.reauthenticate(credential)
                .addOnSuccessListener {
                    // Authentication successful, proceed with password change
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            showLoading(false)
                            Utility.showToast(this, "Password updated successfully")
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showLoading(false)
                            when (e) {
                                is FirebaseAuthWeakPasswordException ->
                                    Utility.showToast(this, "Password should be at least 6 characters")
                                else ->
                                    Utility.showToast(this, "Failed to update password: ${e.message}")
                            }
                        }
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    when (e) {
                        is FirebaseAuthInvalidCredentialsException ->
                            Utility.showToast(this, "Current password is incorrect")
                        else ->
                            Utility.showToast(this, "Authentication failed: ${e.message}")
                    }
                }
        } else {
            showLoading(false)
            Utility.showToast(this, "User not found")
        }
    }

    private fun validateInput(currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        // Reset errors
        binding.edtCurrentPassword.error = null
        binding.edtNewPassword.error = null
        binding.edtConfirmPassword.error = null

        when {
            currentPassword.isEmpty() -> {
                binding.edtCurrentPassword.error = "Current password is required"
                return false
            }
            newPassword.isEmpty() -> {
                binding.edtNewPassword.error = "New password is required"
                return false
            }
            newPassword.length < 6 -> {
                binding.edtNewPassword.error = "Password should be at least 6 characters"
                return false
            }
            confirmPassword.isEmpty() -> {
                binding.edtConfirmPassword.error = "Confirm password is required"
                return false
            }
            newPassword != confirmPassword -> {
                binding.edtConfirmPassword.error = "Passwords do not match"
                return false
            }
            currentPassword == newPassword -> {
                binding.edtNewPassword.error = "New password must be different from current password"
                return false
            }
        }
        return true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressFrame.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnChangePassword.isEnabled = !isLoading
        binding.edtCurrentPassword.isEnabled = !isLoading
        binding.edtNewPassword.isEnabled = !isLoading
        binding.edtConfirmPassword.isEnabled = !isLoading
    }
}