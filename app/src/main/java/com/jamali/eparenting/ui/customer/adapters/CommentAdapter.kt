package com.jamali.eparenting.ui.customer.adapters

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.data.Comment
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ItemCommentDetailPostForumBinding
import com.jamali.eparenting.utils.TimeUtils
import com.jamali.eparenting.utils.Utility

class CommentAdapter(private val comments: MutableList<Comment>) :
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