package com.jamali.eparenting.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class PostType {
    BALITA, PRANIKAH, SD, SMA, SMP, UMUM
}

@Parcelize
data class CommunityPost(
    val id: String = "",
    val username: String = "",
    val userId: String = "",
    val description: String = "",
    val thumbnail: String = "",
    val thumbnailFileName : String = "",
    val type: PostType = PostType.UMUM,
    val timestamp: Long = 0L,
    var likeCount: Int = 0,
    var dislikeCount: Int = 0,
    var commentCount: Int = 0,
    var likedBy: MutableList<String> = mutableListOf(),
    var dislikedBy: MutableList<String> = mutableListOf()
): Parcelable