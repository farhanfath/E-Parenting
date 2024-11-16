package com.jamali.eparenting.ui.home.fragments.forum.personal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.application.Utility
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.databinding.FragmentSubForumPersonalBinding
import com.jamali.eparenting.ui.home.adapters.CommunityAdapter
import com.jamali.eparenting.ui.home.post.PostActivity

class PersonalFragment : Fragment() {

    private var _binding: FragmentSubForumPersonalBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CommunityAdapter
    private val communityList = mutableListOf<CommunityPost>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubForumPersonalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnMakePost.setOnClickListener {
            startActivity(Intent(requireContext(), PostActivity::class.java))
        }

        setupRecyclerView()
        loadCommunityPosts()
    }

    private fun setupRecyclerView() {
        adapter = CommunityAdapter(communityList, childFragmentManager)
        binding.rvPersonalPost.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PersonalFragment.adapter
            // Optional: Add item decoration if needed
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun loadCommunityPosts() {
        val databaseReference = Utility.database.getReference("communityposts")

        // Query dengan orderByChild untuk timestamp
        databaseReference
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    communityList.clear()

                    // Menggunakan temporary list untuk menampung data
                    val tempList = mutableListOf<CommunityPost>()

                    for (data in snapshot.children) {
                        val post = data.getValue(CommunityPost::class.java)
                        post?.let {
                            tempList.add(it)
                        }
                    }

                    // Mengurutkan dari yang terbaru (descending)
                    communityList.addAll(tempList.reversed())

                    // Memperbarui UI di main thread
                    binding.rvPersonalPost.post {
                        adapter.notifyDataSetChanged()

                        // Optional: Scroll ke posisi teratas untuk post terbaru
                        binding.rvPersonalPost.scrollToPosition(0)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error di main thread
                    activity?.runOnUiThread {
                        Utility.showToast(requireContext(), error.message)
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter.cleanup()
    }
}