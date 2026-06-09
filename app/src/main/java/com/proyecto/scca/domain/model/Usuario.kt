package com.proyecto.scca.domain.model

data class Usuario(
    val idUsuario: Int,
    val nombre: String,
    val email: String,
    val rol: Rol,
    val activo: Boolean,
)
