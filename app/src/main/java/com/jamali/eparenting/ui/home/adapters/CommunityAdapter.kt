package com.jamali.eparenting.ui.home.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.databinding.ItemCommunityBinding

class CommunityAdapter(private var communityList: List<CommunityPost>) :
    RecyclerView.Adapter<CommunityAdapter.ViewHolder>(){

    class ViewHolder(private val binding: ItemCommunityBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(communityPost: CommunityPost) {
            binding.descTv.text = communityPost.description
            Glide.with(itemView.context)
                .load(communityPost.thumbnail)
                .into(binding.thumbnailIv)
            Log.e("adapter", "$communityPost.thumbnail")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommunityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = communityList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(communityList[position])
    }

    fun updateData(newCommunityList: List<CommunityPost>) {
        communityList = newCommunityList
        notifyDataSetChanged()
    }
}