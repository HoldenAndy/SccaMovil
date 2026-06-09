package com.proyecto.scca.data.remote.dto

import com.proyecto.scca.core.util.LocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class LogDto(
    val idLog: Int,
    val nivel: String,
    val modulo: String,
    val mensaje: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val fechaHora: LocalDateTime,
)

@Serializable
data class LogRequestDto(
    val nivel: String,
    val modulo: String,
    val mensaje: String,
)
