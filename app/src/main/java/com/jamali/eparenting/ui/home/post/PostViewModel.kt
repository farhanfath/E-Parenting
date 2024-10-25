package com.jamali.eparenting.ui.home.post

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamali.eparenting.data.entity.PostType
import com.jamali.eparenting.data.repository.AppRepository
import kotlinx.coroutines.launch

class PostViewModel(private val repository: AppRepository) : ViewModel() {

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> get() = _uploadStatus

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun uploadPost(
        title: String,
        description: String,
        selectedImageUri: Uri,
        type: PostType
    ) {
        viewModelScope.launch {
            val result = repository.uploadPost(title, description, selectedImageUri, type)
            if (result.isSuccess) {
                _uploadStatus.postValue(true)
            } else {
                _errorMessage.postValue(result.exceptionOrNull()?.message)
                _uploadStatus.postValue(false)
            }
        }
    }
}