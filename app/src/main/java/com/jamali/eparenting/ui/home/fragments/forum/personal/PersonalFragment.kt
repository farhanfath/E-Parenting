package com.jamali.eparenting.ui.home.fragments.forum.personal

import android.content.Intent
import android.os.Bundle
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
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnMakePost.setOnClickListener {
            startActivity(Intent(requireContext(), PostActivity::class.java))
        }

        adapter = CommunityAdapter(communityList)
        binding.rvPersonalPost.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPersonalPost.adapter = adapter

        loadCommunityPosts()
    }

    private fun loadCommunityPosts() {
        val databaseReference = Utility.database.getReference("communityposts")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                communityList.clear()
                for (data in snapshot.children) {
                    val event = data.getValue(CommunityPost::class.java)
                    if (event != null) {
                        communityList.add(event)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}