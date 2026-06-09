package com.proyecto.scca.data.remote.dto

import com.proyecto.scca.core.util.LocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class NodoDto(
    val idNodo: Int,
    val macAddress: String,
    val ubicacion: String,
    val estadoConexion: Boolean,
    @Serializable(with = LocalDateTimeSerializer::class)
    val ultimaLectura: LocalDateTime? = null,
    val activo: Boolean,
)

@Serializable
data class NodoRequestDto(
    val macAddress: String,
    val ubicacion: String,
    val idUsuario: Int,
)

@Serializable
data class NodoUpdateRequestDto(
    val ubicacion: String,
)
