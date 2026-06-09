package com.proyecto.scca.data.remote.api

import com.proyecto.scca.data.remote.dto.AuthResponseDto
import com.proyecto.scca.data.remote.dto.CambiarPasswordRequestDto
import com.proyecto.scca.data.remote.dto.LoginRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto,
    ): AuthResponseDto

    @POST("api/v1/auth/cambiar-password")
    suspend fun cambiarPassword(
        @Body request: CambiarPasswordRequestDto,
    ): String
}
