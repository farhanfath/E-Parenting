package com.jamali.eparenting.ui.home.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.application.Utility
import com.jamali.eparenting.data.entity.Comment
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.data.entity.PostType
import com.jamali.eparenting.databinding.ItemForumPersonalBinding
import com.jamali.eparenting.ui.home.fragments.forum.comment.CommentFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommunityAdapter(
    private val communityList: List<CommunityPost>,
    private val supportFragmentManager: FragmentManager
) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    private val commentList = mutableListOf<Comment>()

    class CommunityViewHolder(val binding: ItemForumPersonalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val binding = ItemForumPersonalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommunityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val community = communityList[position]
        with(holder.binding) {
            tvPostContent.text = community.description
            tvAuthorName.text = community.username
            tvPostTitle.text = getPostTitle(community.type)
            tvLikes.text = community.likeCount.toString()
            tvDislikes.text = community.dislikeCount.toString()
            tvComments.text = community.commentCount.toString()

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = Date(community.timestamp)
            tvPostDate.text = dateFormat.format(date)

            if (community.thumbnail.isEmpty()) {
                ivPostImage.visibility = View.GONE
            } else {
                ivPostImage.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(community.thumbnail)
                    .into(ivPostImage)
            }
        }

        holder.binding.ivComment.setOnClickListener {
            val commentFragment = CommentFragment(community.id, commentList) { newCommentText ->
                addComment(newCommentText, community, holder)
            }
            commentFragment.show(supportFragmentManager, commentFragment.tag)
        }

        holder.binding.ivLike.setOnClickListener {
            val currentUserId = Utility.auth.currentUser?.uid ?: return@setOnClickListener
            val post = communityList[holder.adapterPosition]

            if (currentUserId !in post.likedBy) {
                // User has not liked the post yet
                post.likeCount++
                post.likedBy.add(currentUserId)
                updatePostInRealtime(post)
            } else {
                post.likeCount--
                post.likedBy.remove(currentUserId)
                updatePostInRealtime(post)
            }

            notifyItemChanged(holder.adapterPosition)
        }

        holder.binding.ivDislike.setOnClickListener {
            val currentUserId = Utility.auth.currentUser?.uid ?: return@setOnClickListener
            val post = communityList[holder.adapterPosition]

            if (currentUserId !in post.dislikedBy) {
                // User has not liked the post yet
                post.dislikeCount++
                post.dislikedBy.add(currentUserId)
                updatePostInRealtime(post)
            } else {
                post.dislikeCount--
                post.dislikedBy.remove(currentUserId)
                updatePostInRealtime(post)
            }

            notifyItemChanged(holder.adapterPosition)
        }
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

                    val comment = Comment(
                        idCommunity = community.id,
                        userId = uid,
                        username = username,
                        text = commentText,
                        timestamp = System.currentTimeMillis()
                    )

                    val currentUserId = Utility.auth.currentUser?.uid ?: return
                    val post = communityList[holder.adapterPosition]

                    if (currentUserId !in post.commentBy) {
                        post.commentCount++
                        post.commentBy.add(currentUserId)
                        updatePostInRealtime(post)
                    } else {
                        post.commentCount--
                        post.commentBy.remove(currentUserId)
                        updatePostInRealtime(post)
                    }

                    commentRef.setValue(comment).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Utility.showToast(holder.itemView.context, "Comment added successfully")
                            loadComments(community.id, holder)
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

    private fun loadComments(communityId: String, holder: CommunityViewHolder) {
        val commentsRef = Utility.database.getReference("communityposts/$communityId/comments")
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (data in snapshot.children) {
                    val comment = data.getValue(Comment::class.java)
                    if (comment != null) {
                        commentList.add(comment)
                    }
                }
                // Update the adapter or UI component for displaying comments
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Utility.showToast(holder.itemView.context, error.message)
            }
        })
    }

    private fun updatePostInRealtime(post: CommunityPost) {
        val database = Utility.database.getReference("communityposts")
        database.child(post.id).setValue(post)
    }
}