package com.jamali.eparenting.ui.customer.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.R
import com.jamali.eparenting.data.Comment
import com.jamali.eparenting.data.CommunityPost
import com.jamali.eparenting.data.PostType
import com.jamali.eparenting.data.User
import com.jamali.eparenting.databinding.ItemForumPersonalBinding
import com.jamali.eparenting.ui.customer.fragments.forum.comment.CommentFragment
import com.jamali.eparenting.utils.TimeUtils
import com.jamali.eparenting.utils.Utility

class CommunityAdapter(
    private val communityList: List<CommunityPost>,
    private val supportFragmentManager: FragmentManager
) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    private val commentList = mutableListOf<Comment>()

    private var currentCommentListener: ValueEventListener? = null

    class CommunityViewHolder(val binding: ItemForumPersonalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val binding = ItemForumPersonalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommunityViewHolder(binding)
    }

    private val userCache = mutableMapOf<String, User>()

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val community = communityList[position]

        /**
         * untuk  mengambil gambar profile dari firebase storage
         */
        val userRef = Utility.database.getReference("users").child(community.userId)

        with(holder.binding) {
            tvPostContent.text = community.description
            tvAuthorName.text = community.username
            tvPostTitle.text = getPostTitle(community.type)
            tvLikes.text = community.likeCount.toString()
            tvDislikes.text = community.dislikeCount.toString()
            tvComments.text = community.commentCount.toString()

            tvPostDate.text = TimeUtils.getTimeAgo(community.timestamp)

            setupUserProfileImg(userRef, community, holder)

            if (community.thumbnail.isEmpty()) {
                ivPostImage.visibility = View.GONE
            } else {
                ivPostImage.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(community.thumbnail)
                    .into(ivPostImage)
            }

            val currentUserId = Utility.auth.currentUser?.uid
            if (currentUserId != null) {
                // Set initial like button state
                ivLike.setImageResource(
                    if (currentUserId in community.likedBy) R.drawable.ic_like_true
                    else R.drawable.ic_like
                )

                // Set initial dislike button state
                ivDislike.setImageResource(
                    if (currentUserId in community.dislikedBy) R.drawable.ic_dislike_true
                    else R.drawable.ic_dislike
                )
            }
        }

        holder.binding.ivComment.setOnClickListener {
            /**
             * inisiasi bottom sheet comment dengan function add comment
             */
            val commentFragment = CommentFragment(community.id, commentList) { newCommentText ->
                addComment(newCommentText, community, holder)
            }
            commentFragment.show(supportFragmentManager, commentFragment.tag)
        }

        holder.binding.ivLike.setOnClickListener {
            val userId = Utility.auth.currentUser?.uid ?: return@setOnClickListener
            val post = communityList[holder.adapterPosition]

            holder.binding.ivLike.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(100)
                .withEndAction {
                    holder.binding.ivLike.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    if (userId !in post.likedBy) {
                        // Add like
                        post.likeCount++
                        post.likedBy.add(userId)

                        // Remove dislike if exists
                        if (userId in post.dislikedBy) {
                            post.dislikeCount--
                            post.dislikedBy.remove(userId)
                            holder.binding.ivDislike.setImageResource(R.drawable.ic_dislike)
                        }

                        holder.binding.ivLike.setImageResource(R.drawable.ic_like_true)
                    } else {
                        // Remove like
                        post.likeCount--
                        post.likedBy.remove(userId)
                        holder.binding.ivLike.setImageResource(R.drawable.ic_like)
                    }

                    // Update UI
                    holder.binding.tvLikes.text = post.likeCount.toString()
                    holder.binding.tvDislikes.text = post.dislikeCount.toString()

                    updatePostInRealtime(post)
                }
                .start()
        }

        holder.binding.ivDislike.setOnClickListener {
            val userId = Utility.auth.currentUser?.uid ?: return@setOnClickListener
            val post = communityList[holder.adapterPosition]

            if (userId !in post.dislikedBy) {
                // Add dislike
                post.dislikeCount++
                post.dislikedBy.add(userId)

                // Remove like if exists
                if (userId in post.likedBy) {
                    post.likeCount--
                    post.likedBy.remove(userId)
                    holder.binding.ivLike.setImageResource(R.drawable.ic_like)
                }

                holder.binding.ivDislike.setImageResource(R.drawable.ic_dislike_true)
            } else {
                // Remove dislike
                post.dislikeCount--
                post.dislikedBy.remove(userId)
                holder.binding.ivDislike.setImageResource(R.drawable.ic_dislike)
            }

            // Update UI
            holder.binding.tvLikes.text = post.likeCount.toString()
            holder.binding.tvDislikes.text = post.dislikeCount.toString()

            updatePostInRealtime(post)
        }
    }

    private fun setupUserProfileImg(userRef: DatabaseReference, community: CommunityPost, holder: CommunityViewHolder) {
        // Cek cache terlebih dahulu
        userCache[community.userId]?.let { user ->
            Glide.with(holder.itemView.context)
                .load(user.profile)
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .circleCrop()
                .into(holder.binding.ivAuthorAvatar)
            return
        }

        // Jika tidak ada di cache, ambil dari database
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    // Simpan ke cache
                    userCache[community.userId] = it

                    Glide.with(holder.itemView.context)
                        .load(it.profile)
                        .placeholder(R.drawable.ic_avatar)
                        .error(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(holder.binding.ivAuthorAvatar)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentAdapter", "Error loading user data: ${error.message}")
            }
        })
    }

    override fun getItemCount() = communityList.size

    private fun getPostTitle(type: PostType): String {
        return when (type) {
            PostType.BALITA -> "Balita"
            PostType.PRANIKAH -> "Pra Nikah"
            PostType.SD -> "Sekolah Dasar"
            PostType.SMP -> "Sekolah Menengah Pertama"
            PostType.SMA -> "Sekolah Menengah Atas"
            PostType.UMUM -> "Umum"
        }
    }

    private fun addComment(commentText: String, community: CommunityPost, holder: CommunityViewHolder) {
        val uid = Utility.auth.currentUser?.uid
        if (uid != null && commentText.isNotEmpty()) {
            val postRef = Utility.database.getReference("communityposts/${community.id}")
            val userRef = Utility.database.getReference("users/$uid/username")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.getValue(String::class.java) ?: "unknown"

                    val commentRef = postRef.child("comments").push()
                    val commentId = commentRef.key ?: return

                    val comment = Comment(
                        id = commentId,
                        idCommunity = community.id,
                        userId = uid,
                        username = username,
                        text = commentText,
                        timestamp = System.currentTimeMillis()
                    )

                    val updates = hashMapOf(
                        "/communityposts/${community.id}/comments/$commentId" to comment,
                        "/communityposts/${community.id}/commentCount" to ServerValue.increment(1)
                    )

                    Utility.database.reference.updateChildren(updates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Utility.showToast(holder.itemView.context, "Comment added successfully")
                            } else {
                                Log.e("CommunityAdapter", "Error adding comment", task.exception)
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CommunityAdapter", "Error fetching username", error.toException())
                }
            })
        } else {
            Utility.showToast(holder.itemView.context, "Please enter a comment")
        }
    }

    /**
     * untuk update perubahan pada post ke realtime database
     */
    private fun updatePostInRealtime(post: CommunityPost) {
        val database = Utility.database.getReference("communityposts")
        val updates = hashMapOf(
            "likeCount" to post.likeCount,
            "dislikeCount" to post.dislikeCount,
            "likedBy" to post.likedBy,
            "dislikedBy" to post.dislikedBy
        )
        database.child(post.id).updateChildren(updates)
    }

    fun cleanup() {
        currentCommentListener?.let { listener ->
            Utility.database.getReference("communityposts")
                .removeEventListener(listener)
        }
    }
}