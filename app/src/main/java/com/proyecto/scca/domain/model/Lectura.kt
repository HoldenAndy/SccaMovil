package com.proyecto.scca.domain.model

import kotlinx.datetime.LocalDateTime

data class Lectura(
    val idLectura: Int,
    val idNodo: Int,
    val ph: Double,
    val temperatura: Double,
    val turbidez: Double,
    val tds: Double,
    val fechaHora: LocalDateTime,
)
