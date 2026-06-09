package com.proyecto.scca.data.remote.api

import com.proyecto.scca.data.remote.dto.AnalisisDto
import com.proyecto.scca.data.remote.dto.PageResponseDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AnalisisApi {
    @POST("api/v1/analisis/generar/{idLectura}")
    suspend fun generarAnalisis(
        @Path("idLectura") idLectura: Int,
    ): AnalisisDto

    @GET("api/v1/analisis/nodo/{id}/paginado")
    suspend fun obtenerAnalisisNodo(
        @Path("id") idNodo: Int,
        @Query("inicio") inicio: String,
        @Query("fin") fin: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): PageResponseDto<AnalisisDto>

    @GET("api/v1/analisis/lectura/{idLectura}")
    suspend fun obtenerPorLectura(
        @Path("idLectura") idLectura: Int,
    ): AnalisisDto

    @GET("api/v1/analisis/historial")
    suspend fun obtenerHistorial(): List<AnalisisDto>
}
