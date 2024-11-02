package com.jamali.eparenting.ui.home.fragments.consultation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.Utility
import com.jamali.eparenting.data.entity.User
import com.jamali.eparenting.databinding.FragmentConsultationBinding
import com.jamali.eparenting.ui.home.adapters.ContactAdapter

class ChatFragment : Fragment() {

    private var _binding: FragmentConsultationBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ContactAdapter
    private lateinit var userList: MutableList<User>

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

        setupDataRv()
    }

    private fun setupDataRv() {
        userList = mutableListOf()
        adapter = ContactAdapter(userList)
        binding.contactRv.layoutManager = LinearLayoutManager(requireContext())
        binding.contactRv.adapter = adapter

        fetchDataFromFirebase()
    }

    private fun fetchDataFromFirebase() {
        val currentUserId = Utility.auth.currentUser?.uid
//        showLoading(true)
        val db = Utility.database
        val userRef = db.getReference("users")

        /**
         * chat consultation only show the user with online status
         */
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                var onlineUserFound = false

                for (document in snapshot.children) {
                    val user = document.getValue(User::class.java)
                    if (user != null && user.uid != currentUserId) {
                        val statusRef = db.getReference("users").child(user.uid).child("status")
                        statusRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(statusSnapshot: DataSnapshot) {
                                val status = statusSnapshot.getValue(String::class.java)
                                // Hanya tambahkan pengguna yang berstatus online
                                if (status == "online") {
                                    userList.add(user)
                                    onlineUserFound = true
                                }

                                adapter.notifyDataSetChanged()
                                updateNoOnlineView(onlineUserFound)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.w("MainActivity", "Failed to read user status", error.toException())
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MainActivity", "Error getting documents.", error.toException())
                Toast.makeText(requireContext(), "Error getting documents: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })

        /**
         * chat recyclerview without online status
         */
//        userRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                userList.clear()
//                for (document in snapshot.children) {
//                    val user = document.getValue(User::class.java)
//                    // Filter pengguna selain pengguna yang sedang login
//                    if (user != null && user.uid != currentUserId) {
//                        userList.add(user)
//                    }
//                }
//                adapter.notifyDataSetChanged()
////                showLoading(false)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
////                showLoading(false)
//                Log.w("MainActivity", "Error getting documents.", error.toException())
//                Toast.makeText(requireContext(), "Error getting documents: ${error.message}", Toast.LENGTH_LONG).show()
//            }
//        })
    }

    private fun updateNoOnlineView(isOnline: Boolean) {
        if (isOnline) {
            binding.noUserOnline.visibility = View.GONE
        } else {
            binding.noUserOnline.visibility = View.VISIBLE
        }
    }

//    private fun showLoading(onLoading: Boolean) {
//        binding.progressBar.visibility = if (onLoading) View.VISIBLE else View.GONE
//    }

    override fun onResume() {
        super.onResume()
        setUserStatus("online")
    }

    override fun onPause() {
        super.onPause()
        setUserStatus("offline")
    }

    private fun setUserStatus(status: String) {
        val currentUserId = Utility.auth.currentUser?.uid ?: return
        val userRef = Utility.database.getReference("users").child(currentUserId).child("status")
        userRef.setValue(status)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}