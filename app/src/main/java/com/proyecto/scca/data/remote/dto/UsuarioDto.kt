package com.proyecto.scca.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val idUsuario: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val activo: Boolean,
)

@Serializable
data class UsuarioRequestDto(
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String,
)

@Serializable
data class UsuarioUpdateRequestDto(
    val nombre: String,
    val email: String,
)
