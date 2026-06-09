package com.proyecto.scca.data.remote.api

import com.proyecto.scca.data.remote.dto.LecturaDto
import com.proyecto.scca.data.remote.dto.PageResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LecturaApi {
    @GET("api/v1/lecturas/nodo/{id}/ultima")
    suspend fun obtenerUltima(
        @Path("id") idNodo: Int,
    ): LecturaDto

    @GET("api/v1/lecturas/nodo/{id}/paginado")
    suspend fun obtenerPaginado(
        @Path("id") idNodo: Int,
        @Query("inicio") inicio: String,
        @Query("fin") fin: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 8,
        @Query("sortBy") sortBy: String = "fechaHora",
        @Query("sortDir") sortDir: String = "desc",
    ): PageResponseDto<LecturaDto>

    @GET("api/v1/lecturas/nodo/{id}/graficos")
    suspend fun obtenerGraficos(
        @Path("id") idNodo: Int,
        @Query("inicio") inicio: String,
        @Query("fin") fin: String,
    ): List<LecturaDto>

    @GET("api/v1/lecturas/nodo/{id}/historial-completo")
    suspend fun obtenerHistorialCompleto(
        @Path("id") idNodo: Int,
    ): List<LecturaDto>
}
