package com.proyecto.scca.data.remote.api

import com.proyecto.scca.data.remote.dto.LogDto
import com.proyecto.scca.data.remote.dto.LogRequestDto
import com.proyecto.scca.data.remote.dto.PageResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LogApi {
    @GET("api/v1/logs")
    suspend fun listarLogs(): List<LogDto>

    @GET("api/v1/logs/buscar")
    suspend fun buscarLogs(
        @Query("nivel") nivel: String? = null,
        @Query("modulo") modulo: String? = null,
        @Query("inicio") inicio: String? = null,
        @Query("fin") fin: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PageResponseDto<LogDto>

    @POST("api/v1/logs")
    suspend fun registrarLog(
        @Body request: LogRequestDto,
    ): LogDto
}
