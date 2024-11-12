package com.jamali.eparenting.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class PostType {
    BALITA, PRANIKAH, REMAJA,SMA, SMP, UMUM
}

@Parcelize
data class CommunityPost(
    val id: String = "",
    val username: String = "",
    val description: String = "",
    val thumbnail: String = "",
    val type: PostType = PostType.UMUM
): Parcelable