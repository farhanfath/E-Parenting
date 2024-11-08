package com.jamali.eparenting.data.api

import com.jamali.eparenting.data.entity.fromapi.ModulResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("api/moduls")
    suspend fun getModulData(): ModulResponse

    @GET("api/moduls/jenis/{jenis}")
    suspend fun getModulByType(
        @Path("jenis") jenis: String
    ): ModulResponse
}