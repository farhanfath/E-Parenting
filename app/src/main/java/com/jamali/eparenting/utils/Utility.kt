package com.jamali.eparenting.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

object Utility {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val storage = FirebaseStorage.getInstance()

    fun showLoading(loadingBar: View, state: Boolean) {
        loadingBar.visibility = if (state) View.VISIBLE else View.GONE
    }

    fun showSnackBar(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(view, message, duration).show()
    }

    private var toast: Toast? = null
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        toast?.cancel() // Batalkan toast sebelumnya jika ada
        toast = Toast.makeText(context, message, duration)
        toast?.show()
    }
}