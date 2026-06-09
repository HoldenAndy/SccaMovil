package com.proyecto.scca.data.mapper

import com.proyecto.scca.data.remote.dto.AnalisisDto
import com.proyecto.scca.data.remote.dto.PageResponseDto
import com.proyecto.scca.domain.model.AnalisisIa
import com.proyecto.scca.domain.model.Pagina

fun AnalisisDto.toDomain(): AnalisisIa {
    return AnalisisIa(
        idAnalisis = idAnalisis,
        idLectura = idLectura,
        resultadoTexto = resultadoTexto,
        promptUtilizado = promptUtilizado,
        tiempoResMs = tiempoResMs,
        fechaHora = fechaHora,
    )
}

fun PageResponseDto<AnalisisDto>.toDomain(): Pagina<AnalisisIa> {
    return Pagina(
        contenido = content.map { it.toDomain() },
        numeroPagina = pageNumber,
        tamanioPagina = pageSize,
        totalElementos = totalElements,
        totalPaginas = totalPages,
        esUltima = isLast,
    )
}
