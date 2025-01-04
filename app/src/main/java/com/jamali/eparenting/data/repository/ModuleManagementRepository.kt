package com.jamali.eparenting.data.repository

import android.net.Uri
import android.util.Log
import com.jamali.eparenting.data.model.Module
import com.jamali.eparenting.data.model.PostType
import com.jamali.eparenting.utils.Result
import com.jamali.eparenting.utils.Utility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ModuleManagementRepository {

    private val moduleRef = Utility.database.getReference("module")
    private val storageRef = Utility.storage.getReference("module_pdf")

    fun createModule(title: String, moduleType: PostType, pdfUri: Uri, releaseDate: String) :Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            val  newRef = moduleRef.push()
            val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.pdf"
            val pdfRef = storageRef.child(fileName)

            pdfRef.putFile(pdfUri).await()
            val downloadUrl = pdfRef.downloadUrl.await().toString()

            val module = Module(
                id = newRef.key ?: "",
                title = title,
                type = moduleType,
                isi = downloadUrl,
                uploadedDate = releaseDate
            )
            newRef.setValue(module).await()
            emit(Result.Success(module.id))
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Failed to create Module"))
        }
    }

    fun deleteModule(module: Module) : Flow<Result<Boolean>> = flow {
        emit(Result.Loading)
        try {
            // Delete from storage
            try {

                // Gunakan reference langsung ke file
                Utility.storage.getReferenceFromUrl(module.isi).delete().await()

            } catch (e: Exception) {
                Log.e("BuletinRepository", "Error deleting file: ${e.message}")
            }

            moduleRef.child(module.id).removeValue().await()
            emit(Result.Success(true))

        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Failed to delete Module"))
        }
    }

    fun getAllModule(): Flow<Result<List<Module>>> = flow {
        emit(Result.Loading)
        try {
            val snapshot = moduleRef.get().await()
            val modules = snapshot.children.mapNotNull {
                it.getValue(Module::class.java)
            }
            emit(Result.Success(modules))
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Failed to fetch Modules"))
        }
    }
}