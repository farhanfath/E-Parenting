package com.jamali.eparenting.data

data class User(
    val uid : String = "",
    val email : String = "",
    val username : String = "",
    val profile : String = "",
    val phoneNumber : String = "",
    val role : String = "", // "customer" , "admin" , "doctor"
    val speciality : String = "", // keahlian untuk doctor
    val activeDay: String = "" // untuk jadwal doctor
)

data class UserWithLastMessage(
    val user: User,
    var lastMessage: String = "",
    var lastMessageTime: Long = 0
)
