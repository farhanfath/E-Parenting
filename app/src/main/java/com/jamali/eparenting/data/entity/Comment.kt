package com.jamali.eparenting.data.entity

//data class Comment(
//    val userId: String,
//    val username: String,
//    val text: String,
//    val timestamp: Long
//)
data class Comment(
    val idCommunity: String = "",
    val text: String = "",
    val userId: String = "",
    val username: String = "",
    val timestamp: Long = 0
)