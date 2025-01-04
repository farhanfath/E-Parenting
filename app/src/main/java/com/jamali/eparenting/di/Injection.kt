package com.jamali.eparenting.di

import com.jamali.eparenting.data.repository.ModuleManagementRepository
import com.jamali.eparenting.utils.ViewModelFactory

class Injection {
    companion object {

        private fun provideModuleManagementRepository(): ModuleManagementRepository {
            return ModuleManagementRepository()
        }

        fun provideVieModelFactory(): ViewModelFactory {
            return ViewModelFactory(
                moduleRepository = provideModuleManagementRepository()
            )
        }
    }
}