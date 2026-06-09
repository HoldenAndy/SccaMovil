package com.proyecto.scca.domain.model

data class Pagina<T>(
    val contenido: List<T>,
    val numeroPagina: Int,
    val tamanioPagina: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val esUltima: Boolean,
)
