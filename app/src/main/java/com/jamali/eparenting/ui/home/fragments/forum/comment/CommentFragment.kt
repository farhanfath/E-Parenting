package com.jamali.eparenting.ui.home.fragments.forum.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.application.Utility
import com.jamali.eparenting.data.entity.Comment
import com.jamali.eparenting.databinding.LayoutCommentDialogBinding
import com.jamali.eparenting.ui.home.adapters.CommentAdapter

class CommentFragment(
    private val communityId: String,
    private val comments: MutableList<Comment>,
    private val onAddComment: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: LayoutCommentDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutCommentDialogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val layoutParams = it.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.layoutParams = layoutParams
        }

        commentAdapter = CommentAdapter(comments)
        binding.rvCommentPostForum.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentAdapter
        }

        loadComments()
        emptyCommentSetup()

        binding.btnSendComment.setOnClickListener {
            val newCommentText = binding.etComment.text.toString().trim()
            if (newCommentText.isNotEmpty()) {
                onAddComment(newCommentText)
                binding.etComment.text?.clear()
            }
        }
    }

    private fun loadComments() {
        val commentsRef = Utility.database.getReference("communityposts/$communityId/comments")
        /**
         * ditampilkan berdasarkan timestamp
         */
        commentsRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                comments.clear()
                for (data in snapshot.children) {
                    val comment = data.getValue(Comment::class.java)
                    if (comment != null) {
                        comments.add(comment)
                    }
                }
                /**
                 * ditampilkan dengan urutan terbaru
                 */
                comments.sortByDescending { it.timestamp }
                commentAdapter.notifyDataSetChanged()
                emptyCommentSetup()
            }

            override fun onCancelled(error: DatabaseError) {
                Utility.showToast(requireContext(), error.message)
            }
        })
    }

    private fun emptyCommentSetup() {
        if (comments.isEmpty()) {
            binding.rvCommentPostForum.visibility = View.GONE
            binding.noCommentText.visibility = View.VISIBLE
        } else {
            binding.rvCommentPostForum.visibility = View.VISIBLE
            binding.noCommentText.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        commentAdapter.startUpdatingTime()
        commentAdapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        commentAdapter.stopUpdatingTime()
    }
}