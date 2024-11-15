package com.jamali.eparenting.ui.home.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jamali.eparenting.data.entity.Comment
import com.jamali.eparenting.databinding.ItemCommentDetailPostForumBinding
import com.jamali.eparenting.utils.TimeUtils

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
            tvUsername.text = commentList.username
            tvComment.text = commentList.text
            tvTimestamp.text = TimeUtils.getTimeAgo(commentList.timestamp)
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
}