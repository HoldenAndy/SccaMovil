package com.proyecto.scca.data.remote.api

import com.proyecto.scca.data.remote.dto.NodoDto
import com.proyecto.scca.data.remote.dto.NodoRequestDto
import com.proyecto.scca.data.remote.dto.NodoUpdateRequestDto
import com.proyecto.scca.data.remote.dto.PageResponseDto
import retrofit2.http.*

interface NodoApi {
    @GET("api/v1/nodos")
    suspend fun listarNodos(
        @Query("activo") activo: Boolean? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PageResponseDto<NodoDto>

    @GET("api/v1/nodos/mis-nodos")
    suspend fun misNodos(): List<NodoDto>

    @POST("api/v1/nodos")
    suspend fun crearNodo(
        @Body request: NodoRequestDto,
    ): NodoDto

    @PUT("api/v1/nodos/{id}")
    suspend fun actualizarNodo(
        @Path("id") id: Int,
        @Body request: NodoUpdateRequestDto,
    ): NodoDto

    @PATCH("api/v1/nodos/{id}/propietario")
    suspend fun transferirPropietario(
        @Path("id") id: Int,
        @Query("idNuevoUsuario") idNuevoUsuario: Int,
    ): NodoDto

    @PATCH("api/v1/nodos/{id}/activar")
    suspend fun activarNodo(
        @Path("id") id: Int,
    )

    @DELETE("api/v1/nodos/{id}")
    suspend fun desactivarNodo(
        @Path("id") id: Int,
    )
}
