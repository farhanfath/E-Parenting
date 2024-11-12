package com.jamali.eparenting.ui.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jamali.eparenting.data.entity.Comment
import com.jamali.eparenting.databinding.ItemCommentDetailPostForumBinding

class CommentAdapter(private val comments: MutableList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(val binding: ItemCommentDetailPostForumBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentDetailPostForumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val commentList = comments[position]
        holder.binding.apply {
            tvProfileUsernameDetailPostForum.text = commentList.username
            tvDescCommentUserPostForum.text = commentList.comment
        }
    }
}