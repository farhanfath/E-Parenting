package com.jamali.eparenting

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object Utility {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val storage = FirebaseStorage.getInstance()
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
}