package com.proyecto.scca.domain.model

import kotlinx.datetime.LocalDateTime

data class LogSistema(
    val idLog: Int,
    val nivel: String,
    val modulo: String,
    val mensaje: String,
    val fechaHora: LocalDateTime,
)
