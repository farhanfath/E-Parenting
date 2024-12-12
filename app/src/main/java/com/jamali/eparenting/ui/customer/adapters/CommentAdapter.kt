package com.jamali.eparenting.ui.customer.adapters

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.data.Comment
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ItemCommentDetailPostForumBinding
import com.jamali.eparenting.utils.TimeUtils
import com.jamali.eparenting.utils.Utility

class CommentAdapter(
    private val comments: MutableList<Comment>,
    private val communityPostId: String
) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(val binding: ItemCommentDetailPostForumBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentDetailPostForumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(
            binding
        )
    }

    override fun getItemCount(): Int = comments.size

    private val userCache = mutableMapOf<String, User>()

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val commentList = comments[position]

        /**
         * untuk  mengambil gambar profile dari firebase storage
         */
        val userRef = Utility.database.getReference("users").child(commentList.userId)

        holder.binding.apply {
            tvUsername.text = commentList.username
            tvComment.text = commentList.text
            tvTimestamp.text = TimeUtils.getTimeAgo(commentList.timestamp)

            // Cek cache terlebih dahulu
            userCache[commentList.userId]?.let { user ->
                Glide.with(holder.itemView.context)
                    .load(user.profile)
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(holder.binding.ivProfile)
                return@apply
            }

            // Jika tidak ada di cache, ambil dari database
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        // Simpan ke cache
                        userCache[commentList.userId] = it

                        Glide.with(holder.itemView.context)
                            .load(it.profile)
                            .placeholder(R.drawable.ic_avatar)
                            .error(R.drawable.ic_avatar)
                            .circleCrop()
                            .into(holder.binding.ivProfile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CommentAdapter", "Error loading user data: ${error.message}")
                }
            })

            /**
             * report comment setup
             */
            ivMoreOptions.setOnClickListener { view ->
                showCommentOptionsMenu(view, commentList, holder)
            }
        }
    }

    private fun showCommentOptionsMenu(
        view: View,
        comment: Comment,
        holder: CommentViewHolder
    ) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.option_more_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_report -> {
                    reportComment(comment, holder)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun reportComment(comment: Comment, holder: CommentViewHolder) {
        val currentUserId = Utility.auth.currentUser?.uid ?: return

        // Buat dialog untuk memilih alasan report
        val reportReasons = arrayOf(
            "Konten tidak pantas",
            "Spam",
            "Kekerasan",
            "Informasi palsu",
            "Lainnya"
        )

        MaterialAlertDialogBuilder(holder.itemView.context)
            .setTitle("Laporkan Komentar")
            .setItems(reportReasons) { _, which ->
                val selectedReason = reportReasons[which]
                submitCommentReport(comment, currentUserId, selectedReason, holder)
            }
            .show()
    }

    private fun submitCommentReport(
        comment: Comment,
        reporterId: String,
        reason: String,
        holder: CommentViewHolder
    ) {
        val commentsReportsRef = Utility.database.getReference("comment_reports")
        // Buat objek report untuk komentar
        val reportData = hashMapOf(
            "commentId" to comment.id,
            "postId" to communityPostId, // ID post induk
            "reporterId" to reporterId,
            "commentAuthorId" to comment.userId,
            "commentText" to comment.text,
            "reason" to reason,
            "timestamp" to ServerValue.TIMESTAMP,
            "username" to comment.username
        )

        // Push report ke database
        commentsReportsRef.push().setValue(reportData)
            .addOnSuccessListener {
                Utility.showToast(
                    holder.itemView.context,
                    "Komentar telah dilaporkan. Terima kasih atas laporan Anda."
                )
            }
            .addOnFailureListener { exception ->
                Utility.showToast(
                    holder.itemView.context,
                    "Gagal melaporkan komentar: ${exception.localizedMessage}"
                )
                Log.e("CommentAdapter", "Comment report submission failed", exception)
            }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            notifyDataSetChanged()
            handler.postDelayed(this, 60000)
        }
    }

    fun startUpdatingTime() {
        handler.post(updateTimeRunnable)
    }

    fun stopUpdatingTime() {
        handler.removeCallbacks(updateTimeRunnable)
    }

    fun clearCache() {
        userCache.clear()
    }
}