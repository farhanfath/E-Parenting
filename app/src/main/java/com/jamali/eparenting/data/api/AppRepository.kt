package com.jamali.eparenting.data.api

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.jamali.eparenting.R
import com.jamali.eparenting.application.ResultData
import com.jamali.eparenting.application.Utility
import com.jamali.eparenting.data.entity.ErrorResponse
import com.jamali.eparenting.data.entity.fromapi.ModulResponse
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class AppRepository(
    private val apiService: ApiService,
    private val context: Context,
) {

    fun getModulData(): LiveData<ResultData<ModulResponse>> = liveData {
        emit(ResultData.Loading)
        if (!Utility.isNetworkAvailable(context)) {
            emit(ResultData.Error(context.getString(R.string.error_no_network)))
            return@liveData
        }
        try {
            val response = ApiConfig().getApiService().getModulData()
            emit(ResultData.Success(response))
        } catch (e: HttpException) {
            emit(handleHttpException(e))
        } catch (e: SocketTimeoutException) {
            emit(ResultData.Error(context.getString(R.string.error_timeout)))
        } catch (e: IOException) {
            emit(ResultData.Error(context.getString(R.string.error_network)))
        }
    }

    fun getModulDataByType(type: String): LiveData<ResultData<ModulResponse>> = liveData {
        emit(ResultData.Loading)
        if (!Utility.isNetworkAvailable(context)) {
            emit(ResultData.Error(context.getString(R.string.error_no_network)))
            return@liveData
        }
        try {
            val response = ApiConfig().getApiService().getModulByType(type)
            emit(ResultData.Success(response))
        } catch (e: HttpException) {
            emit(handleHttpException(e))
        } catch (e: SocketTimeoutException) {
            emit(ResultData.Error(context.getString(R.string.error_timeout)))
        } catch (e: IOException) {
            emit(ResultData.Error(context.getString(R.string.error_network)))
        }
    }

    private fun handleHttpException(e: HttpException): ResultData.Error {
        val jsonInString = e.response()?.errorBody()?.string()
        Log.e("DataRepository", "Error response: $jsonInString")
        val errorMessage = try {
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            errorBody.message
        } catch (ex: JsonSyntaxException) {
            jsonInString
        } catch (ex: Exception) {
            null
        }
        val errorText = context.getString(R.string.error_general)
        return ResultData.Error(errorMessage ?: errorText)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: AppRepository? = null
        fun getInstance(
            apiService: ApiService,
            context: Context
        ): AppRepository =
            instance ?: synchronized(this) {
                instance ?: AppRepository(apiService, context)
            }.also { instance = it }
    }
}