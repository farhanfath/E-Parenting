package com.jamali.eparenting.ui.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.databinding.ItemCommunityBinding

class CommunityAdapter(private val communityList: List<CommunityPost>) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    class CommunityViewHolder(val binding: ItemCommunityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val binding = ItemCommunityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommunityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val community = communityList[position]
        with(holder.binding) {
            descTv.text = community.description

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

    }

    override fun getItemCount() = communityList.size
}