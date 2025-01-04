package com.jamali.eparenting.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jamali.eparenting.data.repository.ModuleManagementRepository
import com.jamali.eparenting.viewmodel.ModuleViewModel

class ViewModelFactory(
    private val moduleRepository: ModuleManagementRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModuleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ModuleViewModel(moduleRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}