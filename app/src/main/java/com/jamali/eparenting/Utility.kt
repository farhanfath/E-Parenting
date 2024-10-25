package com.jamali.eparenting

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

class Utility {
    companion object {
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        val storage = FirebaseStorage.getInstance()
    }
}