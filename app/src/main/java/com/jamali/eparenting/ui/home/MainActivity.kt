package com.jamali.eparenting.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.Utility
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.databinding.ActivityMainBinding
import com.jamali.eparenting.ui.home.adapters.CommunityAdapter
import com.jamali.eparenting.ui.home.post.PostActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CommunityAdapter
    private val communityList = mutableListOf<CommunityPost>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.postFab.setOnClickListener {
            startActivity(Intent(this, PostActivity::class.java))
        }

        adapter = CommunityAdapter(communityList)
        binding.communityPostRv.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.communityPostRv.adapter = adapter

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
                Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}