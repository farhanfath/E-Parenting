package com.jamali.eparenting.ui.customer.fragments.forum.community

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.utils.Utility
import com.jamali.eparenting.data.CommunityPost
import com.jamali.eparenting.data.PostType
import com.jamali.eparenting.databinding.ActivityDetailCommunityBinding
import com.jamali.eparenting.ui.adapters.CommunityAdapter

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
        binding.rvPersonalPost.apply {
            layoutManager = LinearLayoutManager(this@DetailCommunityActivity)
            adapter = this@DetailCommunityActivity.adapter
            addItemDecoration(DividerItemDecoration(this@DetailCommunityActivity, LinearLayoutManager.VERTICAL))
        }


        binding.tvTitleListMaterial.text = communityPostTitle
        binding.btnBack.setOnClickListener {
            finish()
        }

        loadCommunityPosts(filterType)
    }

    private fun loadCommunityPosts(typeFilter: PostType) {
        val databaseReference = Utility.database.getReference("communityposts")
        databaseReference
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    communityList.clear()

                    // Gunakan temporary list untuk menampung data sebelum difilter dan diurutkan
                    val tempList = mutableListOf<CommunityPost>()

                    // Iterasi snapshot dan filter berdasarkan type
                    for (data in snapshot.children) {
                        val post = data.getValue(CommunityPost::class.java)
                        if (post != null && post.type == typeFilter) {
                            tempList.add(post)
                        }
                    }

                    // Urutkan dari yang terbaru (descending) dan tambahkan ke communityList
                    communityList.addAll(tempList.reversed())

                    // Update UI di main thread
                    runOnUiThread {
                        adapter.notifyDataSetChanged()

                        // Optional: Tambahkan indikator jika list kosong
                        if (communityList.isEmpty()) {
                            // Tampilkan empty state jika ada
                             binding.noPostState.visibility = View.VISIBLE
                        } else {
                            // Sembunyikan empty state jika ada
                             binding.noPostState.visibility = View.GONE

                            // Optional: Scroll ke posisi teratas untuk post terbaru
                            binding.rvPersonalPost.scrollToPosition(0)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    runOnUiThread {
                        Utility.showToast(this@DetailCommunityActivity, error.message)
                    }
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.cleanup()
    }
}