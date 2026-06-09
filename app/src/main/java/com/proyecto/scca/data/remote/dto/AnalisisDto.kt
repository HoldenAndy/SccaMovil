package com.proyecto.scca.data.remote.dto

import com.proyecto.scca.core.util.LocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class AnalisisDto(
    val idAnalisis: Int,
    val idLectura: Int,
    val resultadoTexto: String,
    val promptUtilizado: String? = null,
    val tiempoResMs: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val fechaHora: LocalDateTime,
)
