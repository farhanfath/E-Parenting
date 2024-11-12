package com.jamali.eparenting.ui.home.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.databinding.ItemForumPersonalBinding
import com.jamali.eparenting.ui.home.fragments.forum.detailforum.DetailForumActivity

class CommunityAdapter(private val communityList: List<CommunityPost>) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    class CommunityViewHolder(val binding: ItemForumPersonalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val binding = ItemForumPersonalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommunityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val community = communityList[position]
        with(holder.binding) {
            descTv.text = community.description
            usernameTv.text = community.username

            if (community.thumbnail.isEmpty()) {
                // Sembunyikan thumbnail jika kosong
                thumbnailIv.visibility = View.GONE
            } else {
                // Tampilkan thumbnail jika tidak kosong
                thumbnailIv.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(community.thumbnail)
                    .into(thumbnailIv)
            }
        }
        holder.binding.root.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(holder.itemView.context, DetailForumActivity::class.java)
            intent.putExtra("community_post", community)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = communityList.size
}