package com.jamali.eparenting

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database

class Utility {
    companion object {
        val auth = FirebaseAuth.getInstance()
        val database = Firebase.database.reference
    }
}