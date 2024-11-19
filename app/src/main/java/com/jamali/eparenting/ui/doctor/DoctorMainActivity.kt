package com.jamali.eparenting.ui.doctor

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.data.User
import com.jamali.eparenting.data.UserWithLastMessage
import com.jamali.eparenting.databinding.ActivityDoctorMainBinding
import com.jamali.eparenting.ui.customer.fragments.profile.LogOutFragment
import com.jamali.eparenting.utils.Utility

class DoctorMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorMainBinding

    private val consultationCustomerList = mutableListOf<UserWithLastMessage>()
    private lateinit var consultationAdapter: CustomerAdapter

    private val databaseReference = Utility.database.getReference("users")
    private val chatsReference = Utility.database.getReference("chats")

    private val userMap = mutableMapOf<String, User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rvAdapterSetup()
        setupRealtimeUpdates()

        binding.logoutBtn.setOnClickListener {
            val logoutFragment =
                LogOutFragment()
            logoutFragment.show(supportFragmentManager, logoutFragment.tag)
        }
    }

    private fun rvAdapterSetup() {
        consultationAdapter = CustomerAdapter(consultationCustomerList)
        binding.rvCustomerConsultation.apply {
            layoutManager = LinearLayoutManager(this@DoctorMainActivity)
            adapter = consultationAdapter
        }
    }

    private fun setupRealtimeUpdates() {
        // 1. Pertama load semua user
        loadAllUsers()

        // 2. Setup listener untuk chat
        setupChatListener()
    }

    private fun loadAllUsers() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userMap.clear()

                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java)
                    if (user != null && user.role == "customer") {
                        userMap[user.uid] = user
                    }
                }

                // Setelah load users, update list chat
                updateChatList()
            }

            override fun onCancelled(error: DatabaseError) {
                runOnUiThread {
                    Utility.showToast(this@DoctorMainActivity, error.message)
                }
            }
        })
    }

    private fun setupChatListener() {
        chatsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updateChatList()
            }

            override fun onCancelled(error: DatabaseError) {
                runOnUiThread {
                    Utility.showToast(this@DoctorMainActivity, error.message)
                }
            }
        })
    }

    private fun updateChatList() {
        val currentUserUid = Utility.auth.currentUser?.uid ?: return

        chatsReference.get().addOnSuccessListener { chatSnapshot ->
            val activeChats = mutableListOf<UserWithLastMessage>()

            for (chatRoom in chatSnapshot.children) {
                val chatRoomId = chatRoom.key ?: continue

                // Hanya ambil chat room yang dimulai dengan ID dokter saat ini
                if (!chatRoomId.startsWith(currentUserUid)) continue

                // Ekstrak UID customer dari chat room ID (ambil bagian setelah ID dokter)
                val customerUid = chatRoomId.substring(currentUserUid.length)

                // Dapatkan data user dari map
                val user = userMap[customerUid] ?: continue

                // Ambil pesan terakhir
                val lastMsgData = chatRoom.child("lastMsg").getValue(String::class.java) ?: ""
                val lastMsgTime = chatRoom.child("lastMsgTime").getValue(Long::class.java) ?: 0L

                // Buat objek UserWithLastMessage
                val userWithMessage = UserWithLastMessage(
                    user = user,
                    lastMessage = lastMsgData,
                    lastMessageTime = lastMsgTime
                )

                activeChats.add(userWithMessage)
            }

            // Sort berdasarkan waktu pesan terakhir
            activeChats.sortByDescending { it.lastMessageTime }

            // Update UI
            updateUI(activeChats)
        }
    }

// old chat update
//    private fun updateChatList() {
//        val currentUserUid = Utility.auth.currentUser?.uid ?: return
//
//        chatsReference.get().addOnSuccessListener { chatSnapshot ->
//            val activeChats = mutableListOf<UserWithLastMessage>()
//
//            for (chatRoom in chatSnapshot.children) {
//                val chatRoomId = chatRoom.key ?: continue
//
//                // Cek apakah chat room melibatkan dokter saat ini
//                if (!chatRoomId.contains(currentUserUid)) continue
//
//                // EkstrakUID customer dari chat room ID
//                val customerUid = chatRoomId.replace(currentUserUid, "")
//
//                // Dapatkan data user dari map
//                val user = userMap[customerUid] ?: continue
//
//                // Ambil pesan terakhir
//                val lastMsgData = chatRoom.child("lastMsg").getValue(String::class.java) ?: ""
//                val lastMsgTime = chatRoom.child("lastMsgTime").getValue(Long::class.java) ?: 0L
//
//                // Buat objek UserWithLastMessage
//                val userWithMessage = UserWithLastMessage(
//                    user = user,
//                    lastMessage = lastMsgData,
//                    lastMessageTime = lastMsgTime
//                )
//
//                activeChats.add(userWithMessage)
//            }
//
//            // Sort berdasarkan waktu pesan terakhir
//            activeChats.sortByDescending { it.lastMessageTime }
//
//            // Update UI
//            updateUI(activeChats)
//        }
//    }

    private fun updateUI(activeChats: List<UserWithLastMessage>) {
        runOnUiThread {
            consultationCustomerList.clear()
            consultationCustomerList.addAll(activeChats)
            consultationAdapter.notifyDataSetChanged()

            // Update visibility of empty state
            binding.noCustomerActive.visibility =
                if (consultationCustomerList.isNotEmpty()) View.GONE else View.VISIBLE
        }
    }
}