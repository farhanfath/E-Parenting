package com.jamali.eparenting.ui.customer.fragments.consultation

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import com.jamali.eparenting.data.Message
import com.jamali.eparenting.databinding.ActivityChatBinding
import com.jamali.eparenting.ui.customer.adapters.MessagesAdapter
import com.jamali.eparenting.utils.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Date

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessagesAdapter

    private val PICK_IMAGE = 100
    private val storageReference = Utility.storage.reference
    private val database = Utility.database.reference

    private var messages: ArrayList<Message>? = null
    private var senderRoom: String? = null
    private var receiverRoom: String? = null
    private var dialog: ProgressDialog? = null
    private var senderUid: String? = null
    private var receiverUid: String? = null

    private var chatEndListener: ValueEventListener? = null

    // Tambahkan flag untuk status activity
    private var isActivityActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()

        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        receiverUid = intent.getStringExtra("uid")
        binding.userName.text = name
        binding.userEmail.text = email

        binding.backBtn.setOnClickListener {
            finish()
        }

        isDoctor { isDoctor ->
            binding.endChatBtn.visibility = if (isDoctor) View.VISIBLE else View.GONE
            if (!isDoctor) {
                // Hanya pasang listener untuk mengecek status chat jika user adalah customer
                setupChatEndListener()
            }
        }

        binding.endChatBtn.setOnClickListener {
            showEndChatConfirmationDialog()
        }

        senderUid = Utility.auth.uid
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        adapter = MessagesAdapter(this@ChatActivity, messages, senderRoom!!, receiverRoom!!)
        binding.recyclerView.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding.recyclerView.adapter = adapter

        // Listen for messages in the senderRoom
        database.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (dataSnapshot in snapshot.children) {
                        val message = dataSnapshot.getValue(Message::class.java)
                        if (message != null) {
                            message.messageId = dataSnapshot.key
                            messages!!.add(message)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    binding.recyclerView.scrollToPosition(messages!!.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })

        binding.sendBtn.setOnClickListener {
            val messageTxt = binding.messageBox.text.toString()
            if (messageTxt.isNotEmpty()) {
                val message = Message(messageTxt, senderUid, Date().time)
                binding.messageBox.setText("")

                val lastMsgObj = mapOf(
                    "lastMsg" to message.message,
                    "lastMsgTime" to message.timestamp
                )

                // Push message to sender and receiver rooms
                val messageId = database.child("chats").child(senderRoom!!).child("messages").push().key
                if (messageId != null) {
                    database.child("chats").child(senderRoom!!).child("messages").child(messageId)
                        .setValue(message)
                    database.child("chats").child(receiverRoom!!).child("messages").child(messageId)
                        .setValue(message)

                    // Update last message for both rooms
                    database.child("chats").child(senderRoom!!).updateChildren(lastMsgObj)
                    database.child("chats").child(receiverRoom!!).updateChildren(lastMsgObj)
                }
            }
        }

        binding.attachBtn.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                uploadImage(imageUri)
            }
        }
    }

    private fun uploadImage(imageUri: Uri) {
        dialog?.show()

        // Create a unique filename for the image
        val timestamp = Date().time
        val imagePath = "chat_images/${senderRoom}/${timestamp}"
        val imageRef = storageReference.child(imagePath)

        // Compress image before uploading
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val compressedData = baos.toByteArray()

        imageRef.putBytes(compressedData)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    dialog?.dismiss()
                    // Create message with image
                    sendImageMessage(downloadUrl.toString(), timestamp)
                }
            }
            .addOnFailureListener { e ->
                dialog?.dismiss()
                Utility.showToast(this@ChatActivity, "Gagal mengunggah gambar: ${e.message}")
            }
            .addOnProgressListener { taskSnapshot ->
                // Update progress
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                dialog?.setMessage("Mengunggah gambar: ${progress.toInt()}%")
            }
    }

    private fun sendImageMessage(imageUrl: String, timestamp: Long) {
        // Menggunakan constructor baru untuk pesan dengan gambar
        val message = Message(
            message = "📷 Photo",
            senderId = senderUid,
            timeStamp = timestamp,
            imageUrl = imageUrl
        )

        val lastMsgObj = mapOf(
            "lastMsg" to "📷 Photo",
            "lastMsgTime" to timestamp
        )

        // Generate unique message ID
        val messageId = database.child("chats").child(senderRoom!!).child("messages").push().key

        if (messageId != null) {
            message.messageId = messageId // Set messageId

            // Send to both rooms
            database.child("chats").child(senderRoom!!).child("messages").child(messageId)
                .setValue(message)
            database.child("chats").child(receiverRoom!!).child("messages").child(messageId)
                .setValue(message)

            // Update last message
            database.child("chats").child(senderRoom!!).updateChildren(lastMsgObj)
            database.child("chats").child(receiverRoom!!).updateChildren(lastMsgObj)
        }
    }

    private fun isDoctor(callback: (Boolean) -> Unit) {
        // Mengambil current user ID dari Firebase Auth
        val currentUserId = Utility.auth.currentUser?.uid

        if (currentUserId == null) {
            callback(false)
            return
        }

        // Mengambil data role dari database
        val userRef = Utility.database.getReference("users").child(currentUserId)
        userRef.child("role").get()
            .addOnSuccessListener { snapshot ->
                // Mengecek apakah role adalah doctor
                val isDoctor = snapshot.getValue(String::class.java) == "doctor"
                callback(isDoctor)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Contoh penggunaan dalam showEndChatConfirmationDialog
    private fun showEndChatConfirmationDialog() {
        isDoctor { isDoctor ->
            if (isDoctor) {
                AlertDialog.Builder(this)
                    .setTitle("Akhiri Chat")
                    .setMessage("Apakah Anda yakin ingin mengakhiri chat ini? Semua pesan akan dihapus secara permanen.")
                    .setPositiveButton("Ya") { _, _ ->
                        endChat()
                    }
                    .setNegativeButton("Tidak", null)
                    .show()
            } else {
                Utility.showToast(this@ChatActivity, "Hanya dokter yang dapat mengakhiri chat")
            }
        }
    }

    private fun endChat() {
        val loadingDialog = ProgressDialog(this).apply {
            setMessage("Mengakhiri chat...")
            setCancelable(false)
            show()
        }

        // Tambahkan flag untuk menandai chat diakhiri oleh dokter
        val updates = hashMapOf<String, Any>()
        updates["/chats/$senderRoom/ended"] = true
        updates["/chats/$receiverRoom/ended"] = true

        // Update status ended terlebih dahulu
        database.updateChildren(updates)
            .addOnSuccessListener {
                // Lanjutkan dengan proses penghapusan gambar dan pesan
                deleteImagesAndMessages(loadingDialog)
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                Utility.showToast(this@ChatActivity, "Gagal mengakhiri chat: ${e.message}")
            }
    }

    private fun deleteImagesAndMessages(loadingDialog: ProgressDialog) {
        database.child("chats").child(senderRoom!!).child("messages")
            .get()
            .addOnSuccessListener { snapshot ->
                val imageDeletionTasks = mutableListOf<Task<Void>>()

                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    if (message?.imageUrl != null) {
                        try {
                            val imageRef = Firebase.storage.getReferenceFromUrl(message.imageUrl!!)
                            imageDeletionTasks.add(imageRef.delete())
                        } catch (e: Exception) {
                            Log.e("ChatActivity", "Error getting storage reference: ${e.message}")
                        }
                    }
                }

                if (imageDeletionTasks.isNotEmpty()) {
                    Tasks.whenAll(imageDeletionTasks)
                        .addOnSuccessListener {
                            deleteChatRooms(loadingDialog)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("ChatActivity", "Error deleting some images: ${exception.message}")
                            deleteChatRooms(loadingDialog)
                        }
                } else {
                    deleteChatRooms(loadingDialog)
                }
            }
            .addOnFailureListener { exception ->
                loadingDialog.dismiss()
                Utility.showToast(this@ChatActivity, "Gagal mengakhiri chat: ${exception.message}")
            }
    }

    private fun deleteChatRooms(loadingDialog: ProgressDialog) {
        val updates = hashMapOf<String, Any?>()

        // Hapus semua pesan di senderRoom
        updates["/chats/$senderRoom"] = null
        // Hapus semua pesan di receiverRoom
        updates["/chats/$receiverRoom"] = null

        database.updateChildren(updates)
            .addOnSuccessListener {
                loadingDialog.dismiss()
                Utility.showToast(this@ChatActivity, "Chat berhasil diakhiri")
                finish()
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                Utility.showToast(this@ChatActivity, "Gagal mengakhiri chat: ${e.message}")
            }
    }

    private fun setupChatEndListener() {
        val chatStatusRef = database.child("chats").child(senderRoom!!).child("ended")

        chatEndListener = chatStatusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isActivityActive) return // Check if activity is still active

                try {
                    if (snapshot.exists() && snapshot.getValue(Boolean::class.java) == true) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (isActivityActive) {
                                showChatEndedDialog()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatActivity", "Error processing chat end status", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatActivity", "Error listening for chat end: ${error.message}")
            }
        })
    }

    private fun showChatEndedDialog() {
        if (!isActivityActive) return

        try {
            removeChatEndListener() // Hapus listener terlebih dahulu

            if (!isFinishing && !isDestroyed) {
                AlertDialog.Builder(this)
                    .setTitle("Chat Berakhir")
                    .setMessage("Percakapan telah diakhiri oleh dokter")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        if (isActivityActive) {
                            finish()
                        }
                    }
                    .setCancelable(false)
                    .create()
                    .apply {
                        if (window != null && !isFinishing) {
                            show()
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("ChatActivity", "Error showing end chat dialog", e)
            if (isActivityActive) {
                finish()
            }
        }
    }

    private fun removeChatEndListener() {
        chatEndListener?.let { listener ->
            database.child("chats").child(senderRoom!!).removeEventListener(listener)
        }
    }

    override fun onResume() {
        super.onResume()
        isActivityActive = true
    }

    override fun onPause() {
        isActivityActive = false
        super.onPause()
    }

    override fun onDestroy() {
        removeChatEndListener()
        isActivityActive = false
        super.onDestroy()
    }

}