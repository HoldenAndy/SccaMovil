package com.proyecto.scca.data.mapper

import com.proyecto.scca.data.remote.dto.NodoDto
import com.proyecto.scca.data.remote.dto.PageResponseDto
import com.proyecto.scca.domain.model.Nodo
import com.proyecto.scca.domain.model.Pagina

fun NodoDto.toDomain(): Nodo {
    return Nodo(
        idNodo = idNodo,
        macAddress = macAddress,
        ubicacion = ubicacion,
        estadoConexion = estadoConexion,
        ultimaLectura = ultimaLectura,
        activo = activo,
    )
}

fun PageResponseDto<NodoDto>.toDomain(): Pagina<Nodo> {
    return Pagina(
        contenido = content.map { it.toDomain() },
        numeroPagina = pageNumber,
        tamanioPagina = pageSize,
        totalElementos = totalElements,
        totalPaginas = totalPages,
        esUltima = isLast,
    )
}
