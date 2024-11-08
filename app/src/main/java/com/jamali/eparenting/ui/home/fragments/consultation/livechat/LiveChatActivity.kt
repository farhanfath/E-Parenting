package com.jamali.eparenting.ui.home.fragments.consultation.livechat

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.application.Utility
import com.jamali.eparenting.data.entity.Message
import com.jamali.eparenting.databinding.ActivityLiveChatBinding
import com.jamali.eparenting.ui.home.adapters.MessagesAdapter
import java.util.Date

class LiveChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveChatBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var database: DatabaseReference
    private var messages: ArrayList<Message>? = null
    private var senderRoom: String? = null
    private var receiverRoom: String? = null
    private var dialog: ProgressDialog? = null
    private var senderUid: String? = null
    private var receiverUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()

        val name = intent.getStringExtra("name")
        receiverUid = intent.getStringExtra("uid")
        binding.userName.text = name

        binding.backBtn.setOnClickListener { finish() }

        senderUid = Utility.auth.uid
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        adapter = MessagesAdapter(this@LiveChatActivity, messages, senderRoom!!, receiverRoom!!)
        binding.recyclerView.layoutManager = LinearLayoutManager(this@LiveChatActivity)
        binding.recyclerView.adapter = adapter

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference

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

        Utility.database.getReference("users")
            .child(receiverUid!!)
            .child("status")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Mendapatkan nilai status dari Presence
                    val presenceStatus = snapshot.getValue(String::class.java)
                    // Mengubah text di status sesuai dengan isi dari Presence
                    binding.status.text = presenceStatus
                }

                override fun onCancelled(error: DatabaseError) {
                    // Tangani error jika diperlukan
                    Log.e("PresenceListener", "Failed to read presence status", error.toException())
                }
            })
    }
}
