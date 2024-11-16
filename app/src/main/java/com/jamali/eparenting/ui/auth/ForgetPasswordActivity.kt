package com.jamali.eparenting.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.utils.Utility
import com.jamali.eparenting.databinding.ActivityForgetPasswordBinding

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sendResetLinkButton.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()

            if (email.isEmpty()) {
                Utility.showToast(this, "Email tidak boleh kosong")
            } else {
                sendPasswordResetEmail(email)
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        Utility.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utility.showToast(this, "Tautan reset password telah dikirim ke email Anda")
                    finish() // Kembali ke halaman login setelah tautan terkirim
                } else {
                    val errorMessage = task.exception?.message ?: "Terjadi kesalahan"
                    Utility.showToast(this, errorMessage)
                }
            }
    }
}