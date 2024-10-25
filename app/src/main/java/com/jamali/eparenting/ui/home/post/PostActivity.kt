package com.jamali.eparenting.ui.home.post

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.R
import com.jamali.eparenting.Utility
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.data.entity.PostType
import com.jamali.eparenting.databinding.ActivityPostBinding
import java.util.UUID

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding
    private var selectedImageUri: Uri? = null

    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the image picker
        setupImagePicker()
        setupSpinner()

        binding.ivPreview.setOnClickListener {
            selectImage()
        }

        binding.btnPost.setOnClickListener {
            uploadPost()
        }
    }

    private fun setupSpinner() {
        val types = PostType.entries.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = adapter
    }

    private fun setupImagePicker() {
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                /**
                 * handle selected image to be previewed
                 */
                selectedImageUri = uri
                binding.ivPreview.setImageURI(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    }

    private fun selectImage() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun uploadPost() {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        when {
            title.isEmpty() -> {
                binding.etTitle.error = "Title is required"
            }
            description.isEmpty() -> {
                binding.etDescription.error = "Description is required"
            }
            selectedImageUri == null -> {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }
            else -> {
                showLoading(true)
                uploadImageAndSaveEvent()
            }
        }
    }

    private fun uploadImageAndSaveEvent() {
        val storageReference = Utility.storage.getReference("event_images/${UUID.randomUUID()}")
        storageReference.putFile(selectedImageUri!!).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val communityPost = CommunityPost(
                    id = UUID.randomUUID().toString(),
                    title = binding.etTitle.text.toString(),
                    thumbnail = uri.toString(),
                    description = binding.etDescription.text.toString()
                )
                saveEventToDatabase(communityPost)
            }
        }.addOnFailureListener {
            showLoading(false)
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveEventToDatabase(community: CommunityPost) {
        val databaseReference = Utility.database.getReference("communityposts")
        databaseReference.child(community.id).setValue(community).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Post added successfully", Toast.LENGTH_SHORT).show()
                showLoading(false)
                finish()
                resetInputData()
            } else {
                Toast.makeText(this, "Failed to add Post", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
        }
    }

    private fun resetInputData() {
        with(binding) {
            etTitle.text?.clear()
            etDescription.text?.clear()
            selectedImageUri = null
            ivPreview.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun showLoading(state: Boolean) {
        binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE
    }
}