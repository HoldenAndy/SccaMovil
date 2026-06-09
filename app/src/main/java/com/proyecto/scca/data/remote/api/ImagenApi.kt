package com.proyecto.scca.data.remote.api

import com.proyecto.scca.data.remote.dto.ImagenDto
import com.proyecto.scca.data.remote.dto.ImagenRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ImagenApi {
    @GET("api/v1/imagenes/lectura/{idLectura}")
    suspend fun obtenerPorLectura(
        @Path("idLectura") idLectura: Int,
    ): ImagenDto

    @GET("api/v1/imagenes")
    suspend fun listarImagenes(): List<ImagenDto>

    @POST("api/v1/imagenes")
    suspend fun registrarImagen(
        @Body request: ImagenRequestDto,
    ): ImagenDto
}
