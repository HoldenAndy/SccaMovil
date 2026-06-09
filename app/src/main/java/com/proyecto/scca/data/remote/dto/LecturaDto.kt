package com.proyecto.scca.data.remote.dto

import com.proyecto.scca.core.util.LocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class LecturaDto(
    val idLectura: Int,
    val idNodo: Int,
    val ph: Double,
    val temperatura: Double,
    val turbidez: Double,
    val tds: Double,
    @Serializable(with = LocalDateTimeSerializer::class)
    val fechaHora: LocalDateTime,
)
