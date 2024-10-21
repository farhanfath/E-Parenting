package com.jamali.eparenting.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jamali.eparenting.data.repository.AppRepository
import com.jamali.eparenting.ui.auth.AuthViewModel

class ViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}