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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.data.CommunityPost
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ActivityUserProfileBinding
import com.jamali.eparenting.ui.customer.adapters.CommunityAdapter
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
                showReportUserDialog()
                true
            }
            else -> {
                false
            }
        }
    }

    /**
     * report user function
     */
    private fun showReportUserDialog() {
        val reasons = arrayOf(
            "Konten tidak pantas",
            "Spam",
            "Pelecehan",
            "Informasi palsu",
            "Lainnya"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Laporkan Pengguna")
            .setItems(reasons) { _, which ->
                val selectedReason = reasons[which]
                reportUser(userId, selectedReason)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun reportUser(reportedUserId: String, reason: String) {
        // Dapatkan ID pengguna saat ini (misalnya dari Firebase Authentication)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(this, "Anda harus login untuk melaporkan pengguna", Toast.LENGTH_SHORT).show()
            return
        }

        // Referensi ke node reports di database
        val reportsRef = Utility.database.getReference("user_reports")

        // Buat objek laporan
        val reportData = hashMapOf(
            "reportedUserId" to reportedUserId,
            "reportingUserId" to currentUserId,
            "reason" to reason,
            "timestamp" to ServerValue.TIMESTAMP
        )

        // Generate unique key untuk setiap laporan
        val reportKey = reportsRef.push().key

        if (reportKey != null) {
            reportsRef.child(reportKey).setValue(reportData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Pengguna berhasil dilaporkan", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Gagal melaporkan pengguna: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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