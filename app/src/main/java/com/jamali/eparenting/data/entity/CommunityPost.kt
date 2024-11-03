package com.jamali.eparenting.data.entity

enum class PostType {
    BALITA, PRANIKAH, REMAJA, UMUM
}

data class CommunityPost(
    val id: String = "",
    val username: String = "",
    val description: String = "",
    val thumbnail: String = "",
    val type: PostType = PostType.UMUM
)