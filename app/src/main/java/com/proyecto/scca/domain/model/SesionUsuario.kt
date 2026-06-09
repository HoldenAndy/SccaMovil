package com.proyecto.scca.domain.model

data class SesionUsuario(
    val nombre: String,
    val rol: Rol,
    val debeCambiarPassword: Boolean,
)
