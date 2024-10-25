package com.jamali.eparenting.data.entity

enum class PostType {
    BALITA, PRANIKAH, REMAJA
}

data class CommunityPost(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val thumbnail: String = "",
    val type: PostType = PostType.BALITA
)