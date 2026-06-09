package com.proyecto.scca.domain.model

import kotlinx.datetime.LocalDateTime

data class ImagenAgua(
    val idImagen: Int,
    val idLectura: Int,
    val rutaArchivo: String,
    val pesoKb: Double,
    val fechaHora: LocalDateTime,
)
