package com.jamali.eparenting.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamali.eparenting.data.model.Module
import com.jamali.eparenting.data.model.PostType
import com.jamali.eparenting.data.repository.ModuleManagementRepository
import com.jamali.eparenting.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModuleViewModel (private val repository: ModuleManagementRepository) : ViewModel() {

    private val _moduleState = MutableStateFlow<Result<List<Module>>>(Result.Loading)
    val moduleState = _moduleState.asStateFlow()

    private val _operationState = MutableStateFlow<Result<Boolean>>(Result.Loading)
    val operationState = _operationState.asStateFlow()

    init {
        getALlModules()
    }

    fun getALlModules() {
        viewModelScope.launch {
            repository.getAllModule().collect { result ->
                _moduleState.value = result
            }
        }
    }

    fun createModule(title: String, moduleType: PostType, pdfUri: Uri, releaseDate: String) {
        viewModelScope.launch {
            repository.createModule(
                title = title,
                moduleType = moduleType,
                pdfUri = pdfUri,
                releaseDate = releaseDate
            ).collect { result ->
                when(result) {
                    is Result.Success -> {
                        _operationState.value = Result.Success(true)
                        getALlModules()
                    }
                    is Result.Error -> {
                        _operationState.value = Result.Error(result.error)
                    }
                    is Result.Loading -> {
                        _operationState.value = Result.Loading
                    }
                }
            }
        }
    }

    fun deleteModule(module: Module) {
        viewModelScope.launch {
            repository.deleteModule(module).collect { result ->
                when(result) {
                    is Result.Success -> {
                        _operationState.value = Result.Success(true)
                        getALlModules()
                    }
                    is Result.Error -> {
                        _operationState.value = Result.Error(result.error)
                    }
                    is Result.Loading -> {
                        _operationState.value = Result.Loading
                    }
                }
            }
        }
    }
}