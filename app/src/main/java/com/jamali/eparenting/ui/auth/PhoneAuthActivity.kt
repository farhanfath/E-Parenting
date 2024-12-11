package com.jamali.eparenting.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.jamali.eparenting.R
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ActivityPhoneAuthBinding
import com.jamali.eparenting.utils.Utility
import com.jamali.eparenting.utils.Utility.auth
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.sendCodeBtn.setOnClickListener {
            registerNumberHandler()
        }

        setInputText()
    }

    private fun registerNumberHandler() {
        var phoneNumber = binding.phoneNumberEt.text.toString().trim()

        binding.phoneNumberLayout.error = null

        // Normalisasi nomor
        phoneNumber = normalizePhoneNumber(phoneNumber)

        when {
            phoneNumber.isEmpty() -> {
                binding.phoneNumberLayout.error = getString(R.string.required)
            }
            !isValidPhoneNumber(phoneNumber) -> {
                binding.phoneNumberLayout.error = "Nomor telepon tidak valid"
                return
            }
            else -> {
                // Lanjutkan proses verifikasi nomor telepon
                startPhoneVerification(phoneNumber)
            }
        }
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        var normalizedNumber = phoneNumber.replace(" ", "").replace("-", "")

        // Konversi "08" menjadi "+62"
        if (normalizedNumber.startsWith("08")) {
            normalizedNumber = "+62${normalizedNumber.substring(1)}"
        }

        // Tambahkan "+62" jika tidak ada
        if (!normalizedNumber.startsWith("+62")) {
            normalizedNumber = "+62$normalizedNumber"
        }

        return normalizedNumber
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Pastikan format +62 dan panjang yang sesuai
        return phoneNumber.startsWith("+62") && phoneNumber.length in 10..14 && phoneNumber.matches(Regex("\\+62\\d+"))
    }

    private fun setInputText() {
        binding.phoneNumberEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.phoneNumberEt.removeTextChangedListener(this)

                val inputText = s.toString().replace(" ", "").replace("-", "")
                val cleanText = inputText.replace("+62", "")

                val formattedText = if (cleanText.isEmpty()) "+62 " else "+62 $cleanText"
                binding.phoneNumberEt.setText(formattedText)
                binding.phoneNumberEt.setSelection(formattedText.length)

                binding.phoneNumberEt.addTextChangedListener(this)
            }
        })
    }

    private fun startPhoneVerification(phoneNumber: String) {
        setLoadingState(true)

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential, phoneNumber)
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    setLoadingState(false)
                    Toast.makeText(
                        this@PhoneAuthActivity,
                        "Verifikasi gagal: ${p0.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // Kode verifikasi dikirim, arahkan ke halaman verifikasi
                    setLoadingState(false)

                    val intent =
                        Intent(this@PhoneAuthActivity, VerifyOtpActivity::class.java).apply {
                            putExtra("verificationId", verificationId)
                            putExtra("phoneNumber", phoneNumber)
                        }
                    startActivity(intent)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, phoneNumber: String) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    userId?.let { uid ->
                        val user = User(
                            uid = uid,
                            phoneNumber = phoneNumber,
                            role = "customer"
                        )

                        // Simpan data pengguna ke Realtime Database
                        Utility.database.reference.child("users").child(uid)
                            .setValue(user)
                            .addOnCompleteListener { userTask ->
                                setLoadingState(false)
                                if (userTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Registrasi berhasil",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(
                                        Intent(this, LoginActivity::class.java)
                                    )
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Gagal menyimpan data pengguna",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } else {
                    setLoadingState(false)
                    Toast.makeText(
                        this,
                        "Autentikasi gagal",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            // Sembunyikan teks dan ikon
            binding.sendCodeBtn.text = ""
            binding.sendCodeBtn.icon = null

            // Tampilkan progress bar
            binding.loadingBar.visibility = View.VISIBLE

            // Nonaktifkan button
            binding.sendCodeBtn.isEnabled = false
        } else {
            // Kembalikan teks dan ikon
            binding.sendCodeBtn.text = getString(R.string.send_verif_text)

            // Sembunyikan progress bar
            binding.loadingBar.visibility = View.GONE

            // Aktifkan kembali button
            binding.sendCodeBtn.isEnabled = true
        }
    }
}