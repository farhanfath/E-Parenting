package com.jamali.eparenting.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jamali.eparenting.application.ResultData
import com.jamali.eparenting.data.api.AppRepository
import com.jamali.eparenting.data.entity.ModulDataType
import com.jamali.eparenting.data.entity.fromapi.ModulResponse

class ModulViewModel(private val repository: AppRepository) : ViewModel() {

    fun getModuls(): LiveData<ResultData<ModulResponse>> {
        return repository.getModulData()
    }

    fun getModulsByType(type: String): LiveData<ResultData<ModulResponse>> {
        return repository.getModulDataByType(type)
    }
}