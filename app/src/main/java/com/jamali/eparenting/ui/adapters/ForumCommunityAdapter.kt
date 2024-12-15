package com.jamali.eparenting.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jamali.eparenting.data.ForumCommunityTypeItem
import com.jamali.eparenting.databinding.ItemForumCommunityBinding
import com.jamali.eparenting.ui.customer.fragments.forum.community.DetailCommunityActivity

class ForumCommunityAdapter(private val forumCommunityTypeList: List<ForumCommunityTypeItem>) :
    RecyclerView.Adapter<ForumCommunityAdapter.ForumCommunityViewHolder>(){

    class ForumCommunityViewHolder(val binding: ItemForumCommunityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumCommunityViewHolder {
        val binding = ItemForumCommunityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForumCommunityViewHolder(binding)
    }

    override fun getItemCount(): Int = forumCommunityTypeList.size

    override fun onBindViewHolder(holder: ForumCommunityViewHolder, position: Int) {
        val communityType = forumCommunityTypeList[position]
        with(holder.binding) {
            tvCardCommunityForum.text = communityType.title
            ivCardCommunityForum.setImageResource(communityType.imageResId)
        }
        holder.binding.root.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailCommunityActivity::class.java)
            intent.putExtra("forum_post_type", communityType.type)
            intent.putExtra("forum_post_title", communityType.title)
            context.startActivity(intent)
        }
    }
}