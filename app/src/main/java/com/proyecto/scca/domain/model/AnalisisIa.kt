package com.proyecto.scca.domain.model

import kotlinx.datetime.LocalDateTime

data class AnalisisIa(
    val idAnalisis: Int,
    val idLectura: Int,
    val resultadoTexto: String,
    val promptUtilizado: String?,
    val tiempoResMs: Int,
    val fechaHora: LocalDateTime,
)
