package com.jamali.eparenting.ui.home.post

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.utils.Utility
import com.jamali.eparenting.data.CommunityPost
import com.jamali.eparenting.data.PostType
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ActivityUploadPhotoBinding
import com.yalantis.ucrop.UCrop
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadPhotoBinding
    private var selectedImageUri: Uri? = null
    private var selectedPostType: PostType = PostType.UMUM
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    // Tambahkan launcher untuk UCrop
    private val cropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                selectedImageUri = resultUri
                binding.ivPreviewImageUpload.setImageURI(resultUri)
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            Toast.makeText(this, "Error cropping image: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the image picker
        setupImagePicker()
        setupSpinner()

        binding.btnGallery.setOnClickListener {
            selectImage()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnCamera.setOnClickListener {
            openCamera()
        }

        binding.btnAcceptPosting.setOnClickListener {
            uploadPost()
        }

        binding.btnCancelPosting.setOnClickListener {
            finish()
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f) // Untuk membuat crop square
            .withMaxResultSize(1080, 1080) // Max resolution

        uCrop.withOptions(UCrop.Options().apply {
            setCompressionQuality(80) // Compression quality
            setHideBottomControls(false)
            setFreeStyleCropEnabled(true)
            setToolbarColor(ContextCompat.getColor(this@PostActivity, R.color.green_500))
            setStatusBarColor(ContextCompat.getColor(this@PostActivity, R.color.green_500))
            setToolbarWidgetColor(ContextCompat.getColor(this@PostActivity, R.color.white))
        })

        try {
            cropLauncher.launch(uCrop.getIntent(this))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error starting crop: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to take pictures.", Toast.LENGTH_SHORT).show()
            }
        }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && selectedImageUri != null) {
            startCrop(selectedImageUri!!)
        } else {
            Log.e("CameraIntent", "Gagal menampilkan gambar, URI tidak valid atau proses gagal.")
        }
    }

    private fun openCamera() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val photoFile = createImageFile()
            selectedImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
            selectedImageUri?.let { uri ->
                takePictureLauncher.launch(uri)
            } ?: run {
                Log.e("CameraIntent", "Failed to create URI for image.")
            }
        } else {
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

//    // Membuat file untuk menyimpan gambar yang diambil
    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun setupSpinner() {
        val types = PostType.entries.map { it.name }.toTypedArray()
        val adapter = ArrayAdapter(this, R.layout.item_forum_type, types)
        binding.insertTypeItem.setAdapter(adapter)
        binding.insertTypeItem.setOnItemClickListener { _, _, position, _ ->
            val selectedTypes = types[position]
            selectedPostType = PostType.valueOf(selectedTypes)
        }
    }

    private fun setupImagePicker() {
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                startCrop(uri)
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

        Utility.showLoading(binding.progressFrame, true)

        val currentUid = Utility.auth.currentUser?.uid
        if (currentUid == null) {
            Utility.showLoading(binding.progressFrame, false)
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Mengambil data user (username dan userId) dari database
        Utility.database.getReference("users").child(currentUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        if (selectedImageUri == null) {
                            // Post tanpa gambar
                            val communityPost = CommunityPost(
                                id = UUID.randomUUID().toString(),
                                username = user.username,
                                userId = user.uid,
                                thumbnail = "",
                                description = description,
                                type = selectedPostType,
                                timestamp = System.currentTimeMillis()
                            )
                            saveEventToDatabase(communityPost)
                        } else {
                            // Post dengan gambar
                            uploadImageAndSaveEvent(description, user.username, user.uid)
                        }
                    } else {
                        Utility.showLoading(binding.progressFrame, false)
                        Toast.makeText(baseContext, "Failed to get user data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utility.showLoading(binding.progressFrame, false)
                    Toast.makeText(baseContext, "Failed to get user data: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UserDataError", "Failed to fetch user data", error.toException())
                }
            })
    }

    private fun uploadImageAndSaveEvent(description: String, username: String, userId: String) {
        val storageReference = Utility.storage.getReference("thumbnails/${UUID.randomUUID()}")
        storageReference.putFile(selectedImageUri!!).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val communityPost = CommunityPost(
                    id = UUID.randomUUID().toString(),
                    username = username,
                    userId = userId,
                    thumbnail = uri.toString(),
                    description = description,
                    type = selectedPostType,
                    timestamp = System.currentTimeMillis()
                )
                saveEventToDatabase(communityPost)
            }
        }.addOnFailureListener {
            Utility.showLoading(binding.progressFrame,false)
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveEventToDatabase(community: CommunityPost) {
        val databaseReference = Utility.database.getReference("communityposts")
        databaseReference.child(community.id).setValue(community).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Post added successfully", Toast.LENGTH_SHORT).show()
                Utility.showLoading(binding.progressFrame,false)
                setResult(Activity.RESULT_OK)
                finish()
                resetInputData()
            } else {
                Toast.makeText(this, "Failed to add Post", Toast.LENGTH_SHORT).show()
                Utility.showLoading(binding.progressFrame,false)
            }
        }
    }

    private fun resetInputData() {
        with(binding) {
            edtAddDescStory.text?.clear()
            selectedImageUri = null
            ivPreviewImageUpload.setImageResource(R.drawable.img_placeholder)
        }
    }
}