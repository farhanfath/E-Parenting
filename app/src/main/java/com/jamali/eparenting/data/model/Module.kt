package com.jamali.eparenting.data.model

data class Module(
    val id : String = "",
    val title : String = "",
    val type: PostType = PostType.UMUM, // membagi antar jenis modul
    val isi : String = "", // berupa file pdf di firebase storage
    val uploadedDate : String = "" // format dd-mm-yyyy
)
