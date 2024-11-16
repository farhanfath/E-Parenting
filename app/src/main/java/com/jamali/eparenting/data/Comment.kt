package com.jamali.eparenting.data

data class Comment(
    val id: String = "",
    val idCommunity: String = "",
    val text: String = "",
    val userId: String = "",
    val username: String = "",
    val timestamp: Long = 0
)