package com.proyecto.scca.data.mapper

import com.proyecto.scca.data.remote.dto.LecturaDto
import com.proyecto.scca.data.remote.dto.PageResponseDto
import com.proyecto.scca.domain.model.Lectura
import com.proyecto.scca.domain.model.Pagina

fun LecturaDto.toDomain(): Lectura {
    return Lectura(
        idLectura = idLectura,
        idNodo = idNodo,
        ph = ph,
        temperatura = temperatura,
        turbidez = turbidez,
        tds = tds,
        fechaHora = fechaHora,
    )
}

fun PageResponseDto<LecturaDto>.toDomain(): Pagina<Lectura> {
    return Pagina(
        contenido = content.map { it.toDomain() },
        numeroPagina = pageNumber,
        tamanioPagina = pageSize,
        totalElementos = totalElements,
        totalPaginas = totalPages,
        esUltima = isLast,
    )
}
