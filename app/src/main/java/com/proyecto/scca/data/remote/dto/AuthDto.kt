package com.proyecto.scca.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val token: String,
    val nombre: String,
    val rol: String,
    val debeCambiarPassword: Boolean,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class CambiarPasswordRequestDto(
    val newPassword: String,
)
