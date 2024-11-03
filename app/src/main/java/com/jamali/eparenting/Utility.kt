package com.jamali.eparenting

import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

object Utility {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val storage = FirebaseStorage.getInstance()

    fun setUserStatus(status: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(currentUserId).child("status")
        userRef.setValue(status)
    }

    fun showLoading(loadingBar: View, state: Boolean) {
        loadingBar.visibility = if (state) View.VISIBLE else View.GONE
    }
}