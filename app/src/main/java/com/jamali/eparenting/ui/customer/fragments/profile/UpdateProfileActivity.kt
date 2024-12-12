package com.jamali.eparenting.ui.customer.fragments.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ActivityUpdateProfileBinding
import com.jamali.eparenting.utils.Utility.auth
import com.jamali.eparenting.utils.Utility.database
import com.jamali.eparenting.utils.Utility.showToast
import com.jamali.eparenting.utils.Utility.storage

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding
    private var selectedImageUri: Uri? = null
    private var currentUser: User? = null
    private var oldImageUrl: String? = null
    private var userGender: String = ""

    private var genderOptions = arrayOf("Laki-laki", "Perempuan")

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfile.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserData()

        binding.ivEditPhoto.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
        spinnerSetup()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            setLoadingState(true)
            database.getReference("users")
                .child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        setLoadingState(false)
                        currentUser = snapshot.getValue(User::class.java)
                        currentUser?.let { user ->
                            binding.apply {
                                edtName.setText(user.username)
                                oldImageUrl = user.profile
                                setupEmailAndPhoneNumber(user)
                                setupGender(user.gender)
                                edtBio.setText(user.description)

                                // Load profile picture
                                if (user.profile.isNotEmpty()) {
                                    Glide.with(this@UpdateProfileActivity)
                                        .load(user.profile)
                                        .placeholder(R.drawable.ic_avatar)
                                        .error(R.drawable.ic_avatar)
                                        .into(ivProfile)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        setLoadingState(false)
                        showToast(this@UpdateProfileActivity, "Gagal mendapatkan data pengguna")
                    }
                })
        }
    }

    private fun setupEmailAndPhoneNumber(user: User) {
        when {
            user.phoneNumber.isNotEmpty() -> {
                binding.edtPhone.setText(user.phoneNumber)
            }
            user.email.isNotEmpty() -> {
                binding.edtEmail.setText(user.email)
            }
        }
    }

    private fun updateProfile() {
        if (!validateInput()) return

        setLoadingState(true)
        val userId = auth.currentUser?.uid ?: return

        if (selectedImageUri != null) {
            // Delete old image if exists
            oldImageUrl?.let { url ->
                if (url.isNotEmpty()) {
                    storage.getReferenceFromUrl(url).delete()
                }
            }

            // Upload new image
            val imageRef = storage.reference
                .child("profile_pictures")
                .child("$userId.jpg")

            imageRef.putFile(selectedImageUri!!)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }
                .addOnSuccessListener { uri ->
                    saveUserData(userId, uri.toString())
                }
                .addOnFailureListener {
                    setLoadingState(false)
                    showToast(this@UpdateProfileActivity, "Gagal Mengunggah Gambar")
                }
        } else {
            // Update without changing image
            saveUserData(userId, oldImageUrl ?: "")
        }
    }

    private fun saveUserData(userId: String, imageUrl: String) {
        val updatedUser = User(
            uid = userId,
            username = binding.edtName.text.toString(),
            profile = imageUrl,
            email = currentUser?.email.toString(),
            phoneNumber = currentUser?.phoneNumber.toString(),
            role = currentUser?.role ?: "customer",
            gender = userGender,
            description = binding.edtBio.text.toString()
        )

        database.getReference("users")
            .child(userId)
            .setValue(updatedUser)
            .addOnSuccessListener {
                setLoadingState(false)
                showToast(this, "Berhasil Mengubah Profil")
                finish()
            }
            .addOnFailureListener {
                setLoadingState(false)
                showToast(this, "Gagal Mengubah Profil")
            }
    }

    private fun setupGender(gender: String) {
        if (gender.isNotEmpty()) {
            val position = genderOptions.indexOf(gender)
            if (position >= 0) {
                binding.genderType.setText(genderOptions[position], false)
                userGender = genderOptions[position]
            }
        }
    }

    private fun spinnerSetup() {
        val adapter = ArrayAdapter(this, R.layout.item_forum_type, genderOptions)
        binding.genderType.setAdapter(adapter)
        binding.genderType.setOnItemClickListener { _, _, position, _ ->
            val selectedGender = genderOptions[position]
            userGender = selectedGender
        }
    }

    private fun validateInput(): Boolean {
        val name = binding.edtName.text.toString()

        if (name.isEmpty()) {
            binding.edtName.error = "Harap isi bagian yang kosong"
            return false
        }
        return true
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            // Sembunyikan teks dan ikon
            binding.btnUpdateProfile.text = ""
            binding.btnUpdateProfile.icon = null

            // Tampilkan progress bar
            binding.progressFrame.visibility = View.VISIBLE

            // Nonaktifkan button
            binding.btnUpdateProfile.isEnabled = false
        } else {
            // Kembalikan teks dan ikon
            binding.btnUpdateProfile.text = getString(R.string.changeProfile)

            // Sembunyikan progress bar
            binding.progressFrame.visibility = View.GONE

            // Aktifkan kembali button
            binding.btnUpdateProfile.isEnabled = true
        }
    }
}