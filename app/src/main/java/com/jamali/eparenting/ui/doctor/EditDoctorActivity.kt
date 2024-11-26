package com.jamali.eparenting.ui.doctor

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ActivityEditDoctorBinding
import com.jamali.eparenting.utils.Utility.auth
import com.jamali.eparenting.utils.Utility.database
import com.jamali.eparenting.utils.Utility.showLoading
import com.jamali.eparenting.utils.Utility.showToast
import com.jamali.eparenting.utils.Utility.storage

class EditDoctorActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEditDoctorBinding
    private var currentUser: User? = null
    private var oldImageUrl: String? = null
    private var selectedImageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfile.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDoctorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDaysDropdown()

        binding.btnChangePhoto.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            updateProfile()
        }

        showDoctorProfile()
    }

    private fun setupDaysDropdown() {
        val days = arrayOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

        val startAdapter = ArrayAdapter(this, R.layout.item_forum_type, days)
        val endAdapter = ArrayAdapter(this, R.layout.item_forum_type, days)

        binding.autoCompleteStartDay.setAdapter(startAdapter)
        binding.autoCompleteEndDay.setAdapter(endAdapter)
    }

    private fun showDoctorProfile() {

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
                                edtUsername.setText(user.username)
                                edtEmail.setText(user.email)
                                oldImageUrl = user.profile
                                edtPhone.setText(user.phoneNumber)
                                edtSpeciality.setText(user.speciality)
                                switchStatus.isChecked = user.status
                                edtDescription.setText(user.description)

                                // Set active days if exists
                                if (user.activeDay.isNotEmpty()) {
                                    val dayRange = user.activeDay.split(" - ")
                                    if (dayRange.size == 2) {
                                        autoCompleteStartDay.setText(dayRange[0], false)
                                        autoCompleteEndDay.setText(dayRange[1], false)
                                    }
                                }

                                // Load profile picture
                                if (user.profile.isNotEmpty()) {
                                    Glide.with(this@EditDoctorActivity)
                                        .load(user.profile)
                                        .placeholder(R.drawable.ic_avatar)
                                        .error(R.drawable.ic_avatar)
                                        .into(ivProfile)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showToast(this@EditDoctorActivity, "Failed to load user data")
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
                    showToast(this@EditDoctorActivity, "Failed to upload image")
                }
        } else {
            // Update without changing image
            saveUserData(userId, oldImageUrl ?: "")
        }
    }

    private fun saveUserData(userId: String, imageUrl: String) {
        val activeDays = "${binding.autoCompleteStartDay.text} - ${binding.autoCompleteEndDay.text}"

        val updatedUser = User(
            uid = userId,
            username = binding.edtUsername.text.toString(),
            email = binding.edtEmail.text.toString(),
            profile = imageUrl,
            phoneNumber = binding.edtPhone.text.toString(),
            speciality = binding.edtSpeciality.text.toString(),
            description = binding.edtDescription.text.toString(),
            activeDay = activeDays,
            status = binding.switchStatus.isChecked,
            role = currentUser?.role ?: "doctor" // Retain the original role
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
        val name = binding.edtUsername.text.toString()
        val email = binding.edtEmail.text.toString()
        val startDay = binding.autoCompleteStartDay.text.toString()
        val endDay = binding.autoCompleteEndDay.text.toString()

        if (name.isEmpty()) {
            binding.edtUsername.error = "Name is required"
            return false
        }
        if (email.isEmpty()) {
            binding.edtEmail.error = "Email is required"
            return false
        }
        if (startDay.isEmpty()) {
            binding.autoCompleteStartDay.error = "Start day is required"
            return false
        }
        if (endDay.isEmpty()) {
            binding.autoCompleteEndDay.error = "End day is required"
            return false
        }
        return true
    }
}