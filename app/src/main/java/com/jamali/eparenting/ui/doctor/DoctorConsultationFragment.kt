package com.jamali.eparenting.ui.doctor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.data.User
import com.jamali.eparenting.data.UserWithLastMessage
import com.jamali.eparenting.databinding.FragmentConsultationBinding
import com.jamali.eparenting.utils.Utility

class DoctorConsultationFragment : Fragment() {

    private var _binding: FragmentConsultationBinding? = null
    private val binding get() = _binding!!

    private val consultationCustomerList = mutableListOf<UserWithLastMessage>()
    private lateinit var consultationAdapter: CustomerAdapter

    private val databaseReference = Utility.database.getReference("users")
    private val chatsReference = Utility.database.getReference("chats")

    private val userMap = mutableMapOf<String, User>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentConsultationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvAdapterSetup()
        setupRealtimeUpdates()
        setupView()
    }

    private fun setupView() {
        binding.apply {
            tvTitle.text = getString(R.string.konsultasi_doctor_text)
            noRvConsultationData.text = getString(R.string.empty_rv_consultation_doctor)
        }
    }

    private fun rvAdapterSetup() {
        consultationAdapter = CustomerAdapter(consultationCustomerList)
        binding.rvConsultation.apply {
            layoutManager = LinearLayoutManager(requireContext())
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
                activity?.runOnUiThread {
                    Utility.showToast(requireContext(), error.message)
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
                activity?.runOnUiThread {
                    Utility.showToast(requireContext(), error.message)
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

    private fun updateUI(activeChats: List<UserWithLastMessage>) {
        activity?.runOnUiThread {
            consultationCustomerList.clear()
            consultationCustomerList.addAll(activeChats)
            consultationAdapter.notifyDataSetChanged()

            // Update visibility of empty state
            binding.noRvConsultationData.visibility =
                if (consultationCustomerList.isNotEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}