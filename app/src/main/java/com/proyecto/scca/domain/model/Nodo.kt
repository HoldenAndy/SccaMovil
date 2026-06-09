package com.proyecto.scca.domain.model

import kotlinx.datetime.LocalDateTime

data class Nodo(
    val idNodo: Int,
    val macAddress: String,
    val ubicacion: String,
    val estadoConexion: Boolean,
    val ultimaLectura: LocalDateTime?,
    val activo: Boolean,
)
