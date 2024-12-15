package com.jamali.eparenting.ui.admin.management.doctormanagement

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.jamali.eparenting.databinding.FragmentManagementDoctorBinding
import com.jamali.eparenting.utils.Utility

class DoctorManagementFragment : Fragment() {

    private var _binding: FragmentManagementDoctorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentManagementDoctorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDoctorRegistration()
    }

    private fun setupDoctorRegistration() {
        binding.tambahDokterBtn.setOnClickListener {
            val username = binding.namaDokterEt.text.toString().trim()
            val speciality = binding.spesialisasiEt.text.toString().trim()
            val email = binding.emailDokterEt.text.toString().trim()
            val password = binding.passwordDokterEt.text.toString().trim()

            if (validateInput(username, speciality, email, password)) {
                // Create user with email and password
                Utility.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            // Get the newly created user
                            val firebaseUser = authTask.result?.user
                            firebaseUser?.let { user ->
                                // Prepare doctor data
                                val doctorData = mapOf(
                                    "uid" to user.uid,
                                    "username" to username,
                                    "email" to email,
                                    "speciality" to speciality,
                                    "role" to "doctor"
                                )

                                val doctorRef = Utility.database.reference
                                    .child("users")
                                    .child(user.uid)

                                doctorRef.setValue(doctorData)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Dokter berhasil didaftarkan",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        clearInputs()
                                    }
                                    .addOnFailureListener {
                                        // Delete the created user if database save fails
                                        user.delete()
                                        Toast.makeText(
                                            requireContext(),
                                            "Gagal menyimpan data dokter",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            // Handle authentication errors
                            handleRegistrationError(authTask.exception)
                        }
                    }
            }
        }
    }

    private fun validateInput(
        nama: String,
        spesialisasi: String,
        email: String,
        password: String
    ): Boolean {
        var isValid = true

        // Nama validation
        if (nama.isEmpty()) {
            binding.namaDokterLayout.error = "Nama dokter harus diisi"
            isValid = false
        } else {
            binding.namaDokterLayout.error = null
        }

        // Spesialisasi validation
        if (spesialisasi.isEmpty()) {
            binding.spesialisasiLayout.error = "Spesialisasi harus diisi"
            isValid = false
        } else {
            binding.spesialisasiLayout.error = null
        }

        // Email validation
        if (email.isEmpty()) {
            binding.emailDokterLayout.error = "Email harus diisi"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailDokterLayout.error = "Format email tidak valid"
            isValid = false
        } else {
            binding.emailDokterLayout.error = null
        }

        // Password validation
        if (password.isEmpty()) {
            binding.passwordDokterLayout.error = "Password harus diisi"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordDokterLayout.error = "Password minimal 6 karakter"
            isValid = false
        } else {
            binding.passwordDokterLayout.error = null
        }

        return isValid
    }

    private fun handleRegistrationError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthUserCollisionException -> {
                Toast.makeText(
                    requireContext(),
                    "Email sudah terdaftar",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Toast.makeText(
                    requireContext(),
                    "Registrasi gagal: ${exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun clearInputs() {
        binding.namaDokterEt.text?.clear()
        binding.spesialisasiEt.text?.clear()
        binding.emailDokterEt.text?.clear()
        binding.passwordDokterEt.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}