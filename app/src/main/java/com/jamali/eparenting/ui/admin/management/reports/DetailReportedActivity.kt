package com.jamali.eparenting.ui.admin.management.reports

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jamali.eparenting.R
import com.jamali.eparenting.data.model.Comment
import com.jamali.eparenting.data.model.CommunityPost
import com.jamali.eparenting.databinding.ActivityDetailReportedBinding
import com.jamali.eparenting.utils.Utility

class DetailReportedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailReportedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailReportedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = intent.getStringExtra("type")
        val commentId = intent.getStringExtra("commentId")
        val postCommentId = intent.getStringExtra("postId")
        val postId = intent.getStringExtra("postReportedId")
        when(type) {
            "post" -> {
                with(binding) {
                    tvHeader.text = getString(R.string.detail_post_reported)
                    postReportedLayout.visibility = View.VISIBLE
                    commentReportedLayout.visibility = View.GONE
                }
                if (postId != null) {
                    setupPostDetails(postId)
                }
            }
            "comment" -> {
                with(binding) {
                    tvHeader.text = getString(R.string.detail_comment_reported)
                    postReportedLayout.visibility = View.GONE
                    commentReportedLayout.visibility = View.VISIBLE
                }
                if (postCommentId != null && commentId != null) {
                    setupCommentDetails(commentId, postCommentId)
                }
            }
        }

        setupView()
    }

    private fun setupCommentDetails(commentId: String,postId: String) {
        val commentRef = Utility.database.getReference("communityposts/$postId/comments/$commentId")
        commentRef.get().addOnSuccessListener {
            val comment = it.getValue(Comment::class.java)
            if (comment != null) {
                with(binding) {
                    tvComment.text = comment.text
                    tvUsername.text = comment.username
                }
            }
        }
    }

    private fun setupPostDetails(postId : String) {
        val communityRef = Utility.database.getReference("communityposts/$postId")

        communityRef.get().addOnSuccessListener {
            val community = it.getValue(CommunityPost::class.java)
            if (community != null) {
                with(binding) {
                    tvPostContent.text = community.description

                    if (community.thumbnail.isEmpty()) {
                        ivPostImage.visibility = View.GONE
                    } else {
                        ivPostImage.visibility = View.VISIBLE
                        Glide.with(this@DetailReportedActivity)
                            .load(community.thumbnail)
                            .into(ivPostImage)
                    }
                }
            }
        }
    }

    private fun setupView() {

    }
}