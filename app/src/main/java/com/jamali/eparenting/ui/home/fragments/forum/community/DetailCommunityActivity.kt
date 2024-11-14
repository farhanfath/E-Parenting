package com.jamali.eparenting.ui.home.fragments.forum.community

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.application.Utility
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.data.entity.PostType
import com.jamali.eparenting.databinding.ActivityDetailCommunityBinding
import com.jamali.eparenting.ui.home.adapters.CommunityAdapter

class DetailCommunityActivity : AppCompatActivity() {

    private lateinit var adapter: CommunityAdapter
    private val communityList = mutableListOf<CommunityPost>()

    private lateinit var binding: ActivityDetailCommunityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val communityPostType = intent.getStringExtra("forum_post_type")
        val communityPostTitle = intent.getStringExtra("forum_post_title")

        val filterType = when (communityPostType) {
            "PRANIKAH" -> PostType.PRANIKAH
            "BALITA" -> PostType.BALITA
            "SD" -> PostType.SD
            "SMP" -> PostType.SMP
            "SMA" -> PostType.SMA
            else -> PostType.UMUM
        }

        adapter = CommunityAdapter(communityList, supportFragmentManager)
        binding.rvPersonalPost.layoutManager = LinearLayoutManager(this)
        binding.rvPersonalPost.adapter = adapter

        binding.tvTitleListMaterial.text = communityPostTitle
        binding.btnBack.setOnClickListener {
            finish()
        }

        loadCommunityPosts(filterType)
    }

    private fun loadCommunityPosts(typeFilter: PostType) {
        val databaseReference = Utility.database.getReference("communityposts")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                communityList.clear()
                for (data in snapshot.children) {
                    val event = data.getValue(CommunityPost::class.java)
                    if (event != null && event.type == typeFilter) {
                        communityList.add(event)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Utility.showToast(this@DetailCommunityActivity, error.message)
            }
        })
    }
}