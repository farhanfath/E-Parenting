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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.jamali.eparenting.Utility
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.data.entity.PostType
import com.jamali.eparenting.data.repository.AppRepository
import com.jamali.eparenting.databinding.ActivityPostBinding
import com.jamali.eparenting.ui.auth.AuthViewModel
import com.jamali.eparenting.utils.ViewModelFactory

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding
    private var selectedImageUri: Uri? = null

    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    private val viewModel: PostViewModel by viewModels {
        ViewModelFactory(AppRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the image picker
        setupImagePicker()
        setupSpinner()

        binding.btnSelectImage.setOnClickListener {
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
        val type = PostType.valueOf(binding.spinnerType.selectedItem.toString())
        if (selectedImageUri != null) {
            showLoading(true)
            viewModel.uploadPost(title, description, selectedImageUri!!, type)
            viewModel.uploadStatus.observe(this) { success ->
                if (success) {
                    showLoading(false)
                    Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    showLoading(false)
                    Log.d("PostActivity", "Post upload failed")
                }
            }
            viewModel.errorMessage.observe(this) { errorMessage ->
                showLoading(false)
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(state: Boolean) {
        binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE
    }
}