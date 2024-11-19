package com.jamali.eparenting.data

class Message {
    var messageId: String? = null
    var message: String? = null
    var senderId: String? = null
    var imageUrl: String? = null
    var timestamp: Long = 0

    constructor(){}

    constructor(
        message: String?,
        senderId: String?,
        timeStamp: Long
    ) {
        this.message = message
        this.senderId = senderId
        this.timestamp = timeStamp
    }

    // Tambahkan constructor baru untuk pesan dengan gambar
    constructor(
        message: String?,
        senderId: String?,
        timeStamp: Long,
        imageUrl: String?
    ) {
        this.message = message
        this.senderId = senderId
        this.timestamp = timeStamp
        this.imageUrl = imageUrl
    }
}