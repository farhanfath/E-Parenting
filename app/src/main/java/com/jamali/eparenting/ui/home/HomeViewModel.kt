package com.jamali.eparenting.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.data.entity.PostType
import com.jamali.eparenting.data.repository.AppRepository

class HomeViewModel(private val repository: AppRepository) : ViewModel() {

    private val _communityList = MutableLiveData<List<CommunityPost>>()
    val communityList: LiveData<List<CommunityPost>> get() = _communityList

    // Mengambil semua data
    fun fetchAllCommunityData() {
        repository.getAllCommunityData { communities ->
            _communityList.value = communities
        }
    }
}