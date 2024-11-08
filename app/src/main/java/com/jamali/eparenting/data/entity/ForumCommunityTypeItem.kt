package com.jamali.eparenting.data.entity

import com.jamali.eparenting.R

data class ForumCommunityTypeItem(
    val title: String,
    val imageResId: Int,
)

object ForumCommunityData {
    val forumItems = listOf(
        ForumCommunityTypeItem("Pra Nikah", R.drawable.pranikah_community_forum),
        ForumCommunityTypeItem("Balita", R.drawable.balita_community_forum),
        ForumCommunityTypeItem("Sekolah Dasar", R.drawable.sd_community_forum),
        ForumCommunityTypeItem("SMP", R.drawable.smp_community_forum),
        ForumCommunityTypeItem("SMA", R.drawable.sma_community_forum)
    )
}