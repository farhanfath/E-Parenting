package com.jamali.eparenting.ui.customer.fragments.profile

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.utils.Utility.auth
import com.jamali.eparenting.utils.Utility.database
import com.jamali.eparenting.utils.Utility.showLoading
import com.jamali.eparenting.utils.Utility.showToast
import com.jamali.eparenting.utils.Utility.storage
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ActivityUpdateProfileBinding

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding
    private var selectedImageUri: Uri? = null
    private var currentUser: User? = null
    private var oldImageUrl: String? = null

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
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            showLoading(binding.progressFrame,true)
            database.getReference("users")
                .child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        showLoading(binding.progressFrame, false)
                        currentUser = snapshot.getValue(User::class.java)
                        currentUser?.let { user ->
                            binding.apply {
                                edtName.setText(user.username)
                                edtEmail.setText(user.email)
                                oldImageUrl = user.profile
                                edtPhone.setText(user.phoneNumber)

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
                        showLoading(binding.progressFrame,false)
                        showToast(this@UpdateProfileActivity, "Failed to load user data")
                    }
                })
        }
    }

    private fun updateProfile() {
        if (!validateInput()) return

        showLoading(binding.progressFrame, true)
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
                    showLoading(binding.progressFrame,false)
                    showToast(this@UpdateProfileActivity, "Failed to upload image")
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
            email = binding.edtEmail.text.toString(),
            profile = imageUrl,
            phoneNumber = binding.edtPhone.text.toString()
        )

        database.getReference("users")
            .child(userId)
            .setValue(updatedUser)
            .addOnSuccessListener {
                showLoading(binding.progressFrame,false)
                showToast(this, "Profile updated successfully")
                finish()
            }
            .addOnFailureListener {
                showLoading(binding.progressFrame,false)
                showToast(this, "Failed to update profile")
            }
    }

    private fun validateInput(): Boolean {
        val name = binding.edtName.text.toString()
        val email = binding.edtEmail.text.toString()

        if (name.isEmpty()) {
            binding.edtName.error = "Name is required"
            return false
        }
        if (email.isEmpty()) {
            binding.edtEmail.error = "Email is required"
            return false
        }
        return true
    }
}