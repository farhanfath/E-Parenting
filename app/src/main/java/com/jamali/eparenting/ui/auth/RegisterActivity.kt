package com.jamali.eparenting.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.jamali.eparenting.R
import com.jamali.eparenting.utils.Utility
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

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun registerController() {
        val username = binding.usernameEt.text.toString().trim()
        val email = binding.emailEt.text.toString().trim()
        val password = binding.passEt.text.toString()

        // Reset error messages
        binding.usernameEtLayout.error = null
        binding.emailEtLayout.error = null
        binding.passEtLayout.error = null

        // Validasi input
        when {
            username.isEmpty() -> {
                binding.usernameEtLayout.error = getString(R.string.required)
                return
            }
            email.isEmpty() -> {
                binding.emailEtLayout.error = getString(R.string.required)
                return
            }
            password.isEmpty() -> {
                binding.passEtLayout.error = getString(R.string.required)
                return
            }
            password.length < 6 -> {
                binding.passEtLayout.error = "Password harus minimal 6 karakter"
                return
            }
            else -> {
                setLoadingState(true)

                // Cek apakah username sudah terdaftar
                Utility.database.reference.child("users").get()
                    .addOnSuccessListener { snapshot ->
                        var isUsernameTaken = false

                        // Iterasi semua children untuk mencari username
                        for (userSnapshot in snapshot.children) {
                            val existingUsername = userSnapshot.child("username").getValue(String::class.java)
                            if (existingUsername == username) {
                                isUsernameTaken = true
                                break
                            }
                        }

                        if (isUsernameTaken) {
                            setLoadingState(false)
                            binding.usernameEtLayout.error = "Username sudah digunakan"
                        } else {
                            // Username unik, lanjutkan proses registrasi
                            registerEmail(email, password, username)
                        }
                    }
                    .addOnFailureListener {
                        setLoadingState(false)
                        Toast.makeText(this, "Gagal memvalidasi username", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun registerEmail(email: String, password: String, username: String) {
        Utility.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = Utility.auth.currentUser?.uid
                    val user = userId?.let { uid ->
                        val role = "customer"
                        User(uid, email, username, role = role)
                    }

                    userId?.let {
                        // Simpan data pengguna ke Realtime Database
                        Utility.database.reference.child("users").child(it)
                            .setValue(user)
                            .addOnCompleteListener { userTask ->
                                setLoadingState(false)
                                if (userTask.isSuccessful) {
                                    Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "Gagal menyimpan data pengguna", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    setLoadingState(false)
                    // Tangani error Firebase Authentication
                    when (task.exception) {
                        is FirebaseAuthUserCollisionException -> {
                            binding.emailEtLayout.error = "Email sudah terdaftar"
                        }
                        else -> {
                            Toast.makeText(this, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            // Sembunyikan teks dan ikon
            binding.registerBtn.text = ""
            binding.registerBtn.icon = null

            // Tampilkan progress bar
            binding.loadingBar.visibility = View.VISIBLE

            // Nonaktifkan button
            binding.registerBtn.isEnabled = false
        } else {
            // Kembalikan teks dan ikon
            binding.registerBtn.text = getString(R.string.register)

            // Sembunyikan progress bar
            binding.loadingBar.visibility = View.GONE

            // Aktifkan kembali button
            binding.registerBtn.isEnabled = true
        }
    }
}
