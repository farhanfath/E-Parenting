package com.jamali.eparenting.ui.home.fragments.forum.detailforum.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jamali.eparenting.data.entity.Comment
import com.jamali.eparenting.databinding.LayoutCommentDialogBinding
import com.jamali.eparenting.ui.home.adapters.CommentAdapter

class CommentFragment(
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

        if (comments.isEmpty()) {
            binding.rvCommentPostForum.visibility = View.GONE
            binding.noCommentText.visibility = View.VISIBLE
        } else {
            binding.rvCommentPostForum.visibility = View.VISIBLE
            binding.noCommentText.visibility = View.GONE
        }

        binding.btnSendComment.setOnClickListener {
            val newCommentText = binding.etComment.text.toString().trim()
            if (newCommentText.isNotEmpty()) {
                onAddComment(newCommentText)
                binding.etComment.text?.clear()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        commentAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}