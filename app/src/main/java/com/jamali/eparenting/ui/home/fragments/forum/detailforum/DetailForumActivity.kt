package com.jamali.eparenting.ui.home.fragments.forum.detailforum

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.application.Utility
import com.jamali.eparenting.data.entity.Comment
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.databinding.ActivityDetailPostForumBinding
import com.jamali.eparenting.ui.home.fragments.forum.detailforum.comment.CommentFragment

class DetailForumActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetailPostForumBinding
    private val commentList = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPostForumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val communityPost = intent.getParcelableExtra<CommunityPost>("community_post")

        setupView(communityPost)

        loadComments(communityPost?.id.toString())

        binding.btnShowCommentFragment.setOnClickListener {
            val commentFragment = CommentFragment(commentList) { newCommentText ->
                addComment(newCommentText, communityPost)
            }
            commentFragment.show(supportFragmentManager, commentFragment.tag)
        }

        binding.imbBackDetailPostForum.setOnClickListener {
            finish()
        }
    }

    private fun setupView(communityPost: CommunityPost?) {
        binding.apply {
            tvUsernameDetailPostForum.text = communityPost?.username
            tvDescDetailPostForum.text = communityPost?.description
            Glide.with(this@DetailForumActivity)
                .load(communityPost?.thumbnail)
                .into(ivPhotoDetailPostForum)
        }
    }

    private fun addComment(commentText: String, communityPost: CommunityPost?) {
        val uid = Utility.auth.currentUser?.uid
        if (uid != null && commentText.isNotEmpty() && communityPost != null) {
            val postRef = Utility.database.getReference("communityposts/${communityPost.id}")
            val userRef = Utility.database.getReference("users/$uid/username")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.getValue(String::class.java) ?: "unknown"

                        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val commentRef = postRef.child("comments").push()

                                    val comment = Comment(
                                        idCommunity = communityPost.id,
                                        userId = uid,
                                        username = username,
                                        comment = commentText
                                    )

                                    // Add the comment to the post's comments
                                    commentRef.setValue(comment).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Utility.showToast(this@DetailForumActivity, "Comment added successfully")
                                        } else {
                                            Log.e("DetailForumActivity", "Error adding comment", task.exception)
                                        }
                                    }
                                } else {
                                    Log.e("DetailForumActivity", "Community post not found")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("DetailForumActivity", "Error adding comment", error.toException())
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DetailForumActivity", "Error adding comment", error.toException())
                }

            })
        } else {
            Utility.showToast(this, "Please enter a comment")
        }
    }

    private fun loadComments(communityPostId: String) {
        val databaseReference = Utility.database.getReference("communityposts/$communityPostId/comments")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (data in snapshot.children) {
                    val comment = data.getValue(Comment::class.java)
                    if (comment != null) {
                        commentList.add(comment)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Utility.showToast(this@DetailForumActivity, error.message)
            }
        })
    }
}