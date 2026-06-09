package com.proyecto.scca.data.remote.api

import com.proyecto.scca.data.remote.dto.PageResponseDto
import com.proyecto.scca.data.remote.dto.UsuarioDto
import com.proyecto.scca.data.remote.dto.UsuarioRequestDto
import com.proyecto.scca.data.remote.dto.UsuarioUpdateRequestDto
import retrofit2.http.*

interface UsuarioApi {
    @GET("api/v1/usuarios")
    suspend fun listarUsuarios(
        @Query("activo") activo: Boolean? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PageResponseDto<UsuarioDto>

    @POST("api/v1/usuarios")
    suspend fun crearUsuario(
        @Body request: UsuarioRequestDto,
    ): UsuarioDto

    @PUT("api/v1/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Int,
        @Body request: UsuarioUpdateRequestDto,
    ): UsuarioDto

    @PATCH("api/v1/usuarios/{id}/rol")
    suspend fun cambiarRol(
        @Path("id") id: Int,
        @Query("rol") rol: String,
    ): UsuarioDto

    @PATCH("api/v1/usuarios/{id}/activar")
    suspend fun activarUsuario(
        @Path("id") id: Int,
    )

    @DELETE("api/v1/usuarios/{id}")
    suspend fun desactivarUsuario(
        @Path("id") id: Int,
    )
}
