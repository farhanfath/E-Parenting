package com.jamali.eparenting.ui.home.post

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.jamali.eparenting.R
import com.jamali.eparenting.Utility
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.data.entity.PostType
import com.jamali.eparenting.databinding.ActivityUploadPhotoBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadPhotoBinding
    private var selectedImageUri: Uri? = null

    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the image picker
        setupImagePicker()
        setupSpinner()

        binding.containerCvAddImagesGallery.setOnClickListener {
            selectImage()
        }

        binding.containerCvTakePicture.setOnClickListener {
            openCamera()
        }

        binding.btnAcceptPosting.setOnClickListener {
            uploadPost()
        }

        binding.btnCancelPosting.setOnClickListener {
            finish()
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && selectedImageUri != null) {
            binding.ivPreviewImageUpload.setImageURI(selectedImageUri)
        } else {
            // Handle jika gambar tidak berhasil diambil atau photoUri null
            Log.e("CameraIntent", "Gagal menampilkan gambar, URI tidak valid atau proses gagal.")
        }
    }

    private fun openCamera() {
        // Buat file untuk menyimpan gambar
        val photoFile = createImageFile()
        selectedImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)

        selectedImageUri?.let { uri ->
            takePictureLauncher.launch(uri)
        } ?: run {
            Log.e("CameraIntent", "Gagal membuat URI untuk gambar.")
        }
    }

    // Membuat file untuk menyimpan gambar yang diambil
    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    private fun setupSpinner() {
        val types = PostType.entries.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.spinnerType.adapter = adapter
    }

    private fun setupImagePicker() {
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                /**
                 * handle selected image to be previewed
                 */
                selectedImageUri = uri
                binding.ivPreviewImageUpload.setImageURI(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    }

    private fun selectImage() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun uploadPost() {
        val description = binding.edtAddDescStory.text.toString()
        if (description.isEmpty()) {
            binding.edtAddDescStory.error = "Description is required"
            return
        }

        showLoading(true)

        if (selectedImageUri == null) {
            // Jika tidak ada gambar, langsung simpan deskripsi saja
            val communityPost = CommunityPost(
                id = UUID.randomUUID().toString(),
                thumbnail = "",
                description = description
            )
            saveEventToDatabase(communityPost)
        } else {
            // Jika ada gambar, unggah gambar dan simpan posting
            uploadImageAndSaveEvent(description)
        }
    }

    private fun uploadImageAndSaveEvent(description: String) {
        val storageReference = Utility.storage.getReference("thumbnails/${UUID.randomUUID()}")
        storageReference.putFile(selectedImageUri!!).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val communityPost = CommunityPost(
                    id = UUID.randomUUID().toString(),
                    thumbnail = uri.toString(),
                    description = description
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
            edtAddDescStory.text?.clear()
            selectedImageUri = null
            ivPreviewImageUpload.setImageResource(R.drawable.images_upload_icon)
        }
    }

    private fun showLoading(state: Boolean) {
        binding.progressFrame.visibility = if (state) View.VISIBLE else View.GONE
    }
}