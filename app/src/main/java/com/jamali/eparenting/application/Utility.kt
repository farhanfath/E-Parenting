package com.jamali.eparenting.application

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.jamali.eparenting.data.api.ApiConfig
import com.jamali.eparenting.data.api.AppRepository

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

    fun provideRepository(context: Context): AppRepository {
        val apiService = ApiConfig().getApiService()
        return AppRepository.getInstance(apiService, context)
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private var toast: Toast? = null
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        toast?.cancel() // Batalkan toast sebelumnya jika ada
        toast = Toast.makeText(context, message, duration)
        toast?.show()
    }
}