package com.jamali.eparenting.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.jamali.eparenting.R
import com.jamali.eparenting.data.model.User
import com.jamali.eparenting.databinding.ActivityLoginBinding
import com.jamali.eparenting.ui.admin.AdminHomeActivity
import com.jamali.eparenting.ui.customer.CustomerMainActivity
import com.jamali.eparenting.ui.doctor.DoctorHomeActivity
import com.jamali.eparenting.utils.Utility

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private const val MAX_LOGIN_ATTEMPTS = 3
        private const val LOCKOUT_DURATION = 60000L // 1 menit dalam milidetik
    }

    private var loginAttempts = 0
    private var lastLoginAttemptTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener { loginController() }
        binding.toRegisterText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.toForgetPass.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        setupTextWatchers()
    }

    private fun setupTextWatchers() {
        binding.emailEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.emailEtLayout.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.passEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.passEtLayout.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loginController() {
        val email = binding.emailEt.text.toString().trim()
        val password = binding.passEt.text.toString()

        // Cek apakah akun sedang terkunci
        if (isAccountLocked()) {
            val remainingTime = (LOCKOUT_DURATION - (System.currentTimeMillis() - lastLoginAttemptTime)) / 1000
            showError("Akun terkunci. Coba lagi dalam $remainingTime detik")
            return
        }

        // Validasi input
        if (!validateInput(email, password)) {
            return
        }

        // Proses login
        attemptLogin(email, password)
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        // Validasi email
        when {
            email.isEmpty() -> {
                binding.emailEtLayout.error = getString(R.string.required)
                isValid = false
            }
            !isValidEmail(email) -> {
                binding.emailEtLayout.error = "Format email tidak valid"
                isValid = false
            }
        }

        // Validasi password
        when {
            password.isEmpty() -> {
                binding.passEtLayout.error = getString(R.string.required)
                isValid = false
            }
            password.length < MIN_PASSWORD_LENGTH -> {
                binding.passEtLayout.error = "Password minimal $MIN_PASSWORD_LENGTH karakter"
                isValid = false
            }
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun isAccountLocked(): Boolean {
        val currentTime = System.currentTimeMillis()
        return loginAttempts >= MAX_LOGIN_ATTEMPTS &&
                (currentTime - lastLoginAttemptTime) < LOCKOUT_DURATION
    }

    private fun attemptLogin(email: String, password: String) {
        setLoadingState(true)

        Utility.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = Utility.auth.currentUser
                    currentUser?.let { user ->
                        val userRef = Utility.database.reference
                            .child("users")
                            .child(user.uid)

                        userRef.get().addOnSuccessListener { snapshot ->
                            setLoadingState(false)

                            val userData = snapshot.getValue(User::class.java)

                            // Tambahkan pengecekan status akun
                            when (userData?.accountStatus) {
                                "blocked" -> {
                                    // Tampilkan dialog akun diblokir
                                    showBlockedAccountDialog()
                                    // Sign out user
                                    Utility.auth.signOut()
                                }
                                "active" -> {
                                    loginAttempts = 0 // Reset login attempts

                                    when (userData.role) {
                                        "admin" -> navigateTo(AdminHomeActivity::class.java)
                                        "customer" -> navigateTo(CustomerMainActivity::class.java)
                                        "doctor" -> navigateTo(DoctorHomeActivity::class.java)
                                        else -> showError("Invalid user role")
                                    }
                                }
                                else -> {
                                    // Default ke active jika status tidak didefinisikan
                                    when (userData?.role) {
                                        "admin" -> navigateTo(AdminHomeActivity::class.java)
                                        "customer" -> navigateTo(CustomerMainActivity::class.java)
                                        "doctor" -> navigateTo(DoctorHomeActivity::class.java)
                                        else -> showError("Invalid user role")
                                    }
                                }
                            }
                        }.addOnFailureListener {
                            setLoadingState(false)
                            showError("Failed to retrieve user data")
                        }
                    }
                } else {
                    setLoadingState(false)
                    handleLoginError(task.exception)
                }
            }
    }

    private fun showBlockedAccountDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Akun Diblokir")
            .setMessage("Akun Anda telah diblokir. Silakan hubungi administrator untuk informasi lebih lanjut.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun handleLoginError(exception: Exception?) {
        loginAttempts++
        lastLoginAttemptTime = System.currentTimeMillis()

        when {
            loginAttempts >= MAX_LOGIN_ATTEMPTS -> {
                showError("Terlalu banyak percobaan gagal. Akun dikunci selama 1 menit")
            }
            exception is FirebaseAuthInvalidCredentialsException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" ->
                        binding.emailEtLayout.error = "Format email tidak valid"
                    "ERROR_WRONG_PASSWORD" ->
                        binding.passEtLayout.error = "Password salah"
                    else ->
                        showError("Email atau password salah")
                }
            }
            exception is FirebaseAuthInvalidUserException -> {
                when (exception.errorCode) {
                    "ERROR_USER_NOT_FOUND" ->
                        showError("Email tidak terdaftar")
                    "ERROR_USER_DISABLED" ->
                        showError("Akun telah dinonaktifkan")
                    else ->
                        showError("Terjadi kesalahan: ${exception.message}")
                }
            }
            else -> {
                showError("Terjadi kesalahan: ${exception?.message}")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            // Sembunyikan teks dan ikon
            binding.loginBtn.text = ""
            binding.loginBtn.icon = null

            // Tampilkan progress bar
            binding.loadingBar.visibility = View.VISIBLE

            // Nonaktifkan button
            binding.loginBtn.isEnabled = false
        } else {
            // Kembalikan teks dan ikon
            binding.loginBtn.text = getString(R.string.login)

            // Sembunyikan progress bar
            binding.loadingBar.visibility = View.GONE

            // Aktifkan kembali button
            binding.loginBtn.isEnabled = true
        }
    }
}