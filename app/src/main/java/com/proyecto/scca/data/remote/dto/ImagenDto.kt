package com.proyecto.scca.data.remote.dto

import com.proyecto.scca.core.util.LocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ImagenDto(
    val idImagen: Int,
    val idLectura: Int,
    val rutaArchivo: String,
    val pesoKb: Double,
    @Serializable(with = LocalDateTimeSerializer::class)
    val fechaHora: LocalDateTime,
)

@Serializable
data class ImagenRequestDto(
    val idLectura: Int,
    val rutaArchivo: String,
    val pesoKb: Double,
)
