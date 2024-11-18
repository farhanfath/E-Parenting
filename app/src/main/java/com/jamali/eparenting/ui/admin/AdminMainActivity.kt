package com.jamali.eparenting.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.databinding.ActivityMainAdminBinding
import com.jamali.eparenting.ui.customer.fragments.profile.LogOutFragment
import com.jamali.eparenting.utils.Utility

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainAdminBinding
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pieChart = binding.userStatisticsChart

        setupUserStatisticsChart()
        setupDoctorRegistration()

        binding.logoutBtn.setOnClickListener {
            val logoutFragment =
                LogOutFragment()
            logoutFragment.show(supportFragmentManager, logoutFragment.tag)
        }
    }

    private fun setupUserStatisticsChart() {
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)

        val userRef = Utility.database.reference.child("users")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCount = snapshot.children.count {
                    it.child("role").value == "customer"
                }
                val doctorCount = snapshot.children.count {
                    it.child("role").value == "doctor"
                }
                val adminCount = snapshot.children.count {
                    it.child("role").value == "admin"
                }

                val entries = arrayListOf(
                    PieEntry(userCount.toFloat(), "Customer"),
                    PieEntry(doctorCount.toFloat(), "Dokter"),
                    PieEntry(adminCount.toFloat(), "Admin")
                )

                val dataSet = PieDataSet(entries, "Statistik Pengguna")
                dataSet.colors = listOf(
                    ContextCompat.getColor(this@AdminMainActivity, R.color.user_color),
                    ContextCompat.getColor(this@AdminMainActivity, R.color.doctor_color),
                    ContextCompat.getColor(this@AdminMainActivity, R.color.admin_color)
                )

                dataSet.valueTextColor = Color.BLACK
                dataSet.valueTextSize = 12f

                val data = PieData(dataSet)
                data.setValueFormatter(PercentFormatter(pieChart))

                pieChart.data = data
                pieChart.animateY(1000)
                pieChart.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminMainActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        })
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
                                            this,
                                            "Dokter berhasil didaftarkan",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        clearInputs()
                                        setupUserStatisticsChart()
                                    }
                                    .addOnFailureListener {
                                        // Delete the created user if database save fails
                                        user.delete()
                                        Toast.makeText(
                                            this,
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
                    this,
                    "Email sudah terdaftar",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Toast.makeText(
                    this,
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
}