package com.jamali.eparenting.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.jamali.eparenting.data.entity.User
import com.jamali.eparenting.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AppRepository): ViewModel() {

    fun registerUser(name: String, email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.registerUser(name, email, password)
            onResult(result)
        }
    }

    fun loginUser(email: String, password: String, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val result = repository.loginUser(email, password)
            onResult(result)
        }
    }

    fun getUserData() = liveData(Dispatchers.IO) {
        emit(repository.getUserData())
    }
}