package com.jamali.eparenting.data

data class User(
    val uid : String = "",
    val email : String = "",
    val username : String = "",
    val profile : String = "",
    val phoneNumber : String = "",
    val role : String = "", // "customer" or "admin"
    val speciality : String = "", // keahlian untuk pakar
    val activeDay: String = "" // untuk jadwal pakar
)
