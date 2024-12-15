package com.jamali.eparenting.ui.customer.user

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.data.CommunityPost
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ActivityUserProfileBinding
import com.jamali.eparenting.ui.adapters.CommunityAdapter
import com.jamali.eparenting.utils.ReportManager
import com.jamali.eparenting.utils.Utility

@SuppressLint("SetTextI18n")
class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"

        fun startActivity(context: Context, userId: String) {
            val intent = Intent(context, UserProfileActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        userId = intent.getStringExtra(EXTRA_USER_ID)
            ?: run {
                Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

        loadUserProfile()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_more_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_report -> {
                ReportManager.reportUser(this, userId)
                true
            }
            else -> {
                false
            }
        }
    }

    private fun loadUserProfile() {
        // Referensi ke database untuk user
        val userRef = Utility.database.getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    displayUserProfile(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserProfileActivity,
                    "Gagal memuat profil: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun displayUserProfile(user: User) {
        // Tampilkan foto profil
        Glide.with(this)
            .load(user.profile)
            .placeholder(R.drawable.ic_avatar)
            .error(R.drawable.ic_avatar)
            .circleCrop()
            .into(binding.profileImage)

        // Tampilkan informasi pengguna
        binding.tvUsername.text = user.username
        setupEmailAndPhone(user)
        binding.tvDescription.text = user.description.ifEmpty { "Tidak ada deskripsi" }

        // Tampilkan informasi tambahan berdasarkan peran
        when (user.role) {
            "doctor" -> {
                binding.layoutDoctorInfo.visibility = View.VISIBLE
                binding.tvSpeciality.text = user.speciality
                binding.tvActiveDay.text = user.activeDay
            }
            else -> {
                binding.layoutDoctorInfo.visibility = View.GONE
            }
        }

        // Muat posting pengguna
        loadUserPosts(userId)
    }

    private fun setupEmailAndPhone(user: User) {
        when {
            user.email.isNotEmpty() -> {
                binding.tvEmail.text = user.email
            }
            user.phoneNumber.isNotEmpty() -> {
                binding.tvPhoneNumber.text = user.phoneNumber
            }
        }
    }

    private fun loadUserPosts(userId: String) {
        val postsRef = Utility.database.getReference("communityposts")
        val query = postsRef.orderByChild("userId").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userPosts = mutableListOf<CommunityPost>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(CommunityPost::class.java)
                    post?.let { userPosts.add(it) }
                }

                // Update UI dengan jumlah posting
                binding.tvTotalPosts.text = getString(R.string.jumlah_posting, userPosts.size.toString())

                // Optional: Tampilkan daftar posting
                setupPostsRecyclerView(userPosts)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@UserProfileActivity,
                    "Gagal memuat posting: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupPostsRecyclerView(posts: List<CommunityPost>) {
        val adapter = CommunityAdapter(posts, supportFragmentManager)
        binding.recyclerViewUserPosts.adapter = adapter
        binding.recyclerViewUserPosts.layoutManager = LinearLayoutManager(this)
    }
}