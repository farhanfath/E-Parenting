    package com.jamali.eparenting.ui.auth

    import android.content.Intent
    import android.os.Bundle
    import android.os.CountDownTimer
    import android.view.View
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import com.google.firebase.auth.PhoneAuthCredential
    import com.google.firebase.auth.PhoneAuthProvider
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.ValueEventListener
    import com.jamali.eparenting.R
    import com.jamali.eparenting.data.User
    import com.jamali.eparenting.databinding.ActivityVerifyOtpBinding
    import com.jamali.eparenting.ui.customer.CustomerMainActivity
    import com.jamali.eparenting.utils.Utility
    import com.jamali.eparenting.utils.Utility.auth
    import com.jamali.eparenting.utils.Utility.generateRandomUsername

    class VerifyOtpActivity : AppCompatActivity() {

        private lateinit var binding : ActivityVerifyOtpBinding

        private lateinit var verificationId: String
        private lateinit var phoneNumber: String
        private var resendCountDownTimer: CountDownTimer? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Retrieve verification ID and phone number from previous activity
            verificationId = intent.getStringExtra("verificationId") ?: ""
            phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

            binding.buttonVerifyOTP.setOnClickListener {
                verifyOtp()
            }

            binding.textResendOTP.setOnClickListener {
                resendVerificationCode()
            }

            // Start countdown for resend OTP
            startResendOtpCountdown()
        }

        private fun verifyOtp() {
            val otpCode = binding.otpPinView.value

            if (otpCode.length == 6) {
                setLoadingState(true)

                val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Masukkan kode OTP dengan benar", Toast.LENGTH_SHORT).show()
            }
        }

        private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        userId?.let { uid ->
                            // Cek apakah user sudah ada di database
                            Utility.database.reference.child("users").child(uid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        setLoadingState(false)
                                        if (snapshot.exists()) {
                                            // User sudah ada, ambil data yang ada
                                            val existingUser = snapshot.getValue(User::class.java)
                                            Toast.makeText(
                                                this@VerifyOtpActivity,
                                                "Selamat datang kembali ${existingUser?.username}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            // Langsung pindah ke halaman utama
                                            navigateToMainActivity()
                                        } else {
                                            // Buat user baru jika belum ada
                                            val newUser = User(
                                                uid = uid,
                                                username = generateRandomUsername(),
                                                phoneNumber = phoneNumber,
                                                role = "customer"
                                            )

                                            // Simpan user baru ke database
                                            Utility.database.reference.child("users").child(uid)
                                                .setValue(newUser)
                                                .addOnCompleteListener { userTask ->
                                                    if (userTask.isSuccessful) {
                                                        Toast.makeText(
                                                            this@VerifyOtpActivity,
                                                            "Akun berhasil dibuat",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        navigateToMainActivity()
                                                    } else {
                                                        Toast.makeText(
                                                            this@VerifyOtpActivity,
                                                            "Gagal membuat akun",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        setLoadingState(false)
                                        Toast.makeText(
                                            this@VerifyOtpActivity,
                                            "Gagal mengecek data: ${error.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                        }
                    } else {
                        setLoadingState(false)
                        Toast.makeText(
                            this,
                            "Verifikasi OTP gagal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        // Fungsi helper untuk navigasi
        private fun navigateToMainActivity() {
            val intent = Intent(this, CustomerMainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        private fun resendVerificationCode() {
            // Implement OTP resend logic using Firebase PhoneAuthProvider
            // This is a placeholder and should be implemented with actual Firebase resend method
            Toast.makeText(this, "Mengirim ulang kode OTP", Toast.LENGTH_SHORT).show()

            // Disable resend button during countdown
            binding.textResendOTP.isClickable = false
            startResendOtpCountdown()
        }

        private fun startResendOtpCountdown() {
            // Countdown for 60 seconds before allowing resend
            resendCountDownTimer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.textResendOTP.text = getString(R.string.resend_otp_countdown, millisUntilFinished / 1000)
                    binding.textResendOTP.isClickable = false
                }

                override fun onFinish() {
                    binding.textResendOTP.text = getString(R.string.resendCodeText)
                    binding.textResendOTP.isClickable = true
                }
            }.start()
        }

        private fun setLoadingState(isLoading: Boolean) {
            if (isLoading) {
                // Sembunyikan teks dan ikon
                binding.buttonVerifyOTP.text = ""
                binding.buttonVerifyOTP.icon = null

                // Tampilkan progress bar
                binding.progressBarOTP.visibility = View.VISIBLE

                // Nonaktifkan button
                binding.buttonVerifyOTP.isEnabled = false
            } else {
                // Kembalikan teks dan ikon
                binding.buttonVerifyOTP.text = getString(R.string.verifikasi_text)

                // Sembunyikan progress bar
                binding.progressBarOTP.visibility = View.GONE

                // Aktifkan kembali button
                binding.buttonVerifyOTP.isEnabled = true
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            // Cancel timer to prevent memory leaks
            resendCountDownTimer?.cancel()
        }
    }