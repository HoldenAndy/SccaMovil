package com.proyecto.scca.data.mapper

import com.proyecto.scca.data.remote.dto.LogDto
import com.proyecto.scca.data.remote.dto.PageResponseDto
import com.proyecto.scca.domain.model.LogSistema
import com.proyecto.scca.domain.model.Pagina

fun LogDto.toDomain(): LogSistema {
    return LogSistema(
        idLog = idLog,
        nivel = nivel,
        modulo = modulo,
        mensaje = mensaje,
        fechaHora = fechaHora,
    )
}

fun PageResponseDto<LogDto>.toDomain(): Pagina<LogSistema> {
    return Pagina(
        contenido = content.map { it.toDomain() },
        numeroPagina = pageNumber,
        tamanioPagina = pageSize,
        totalElementos = totalElements,
        totalPaginas = totalPages,
        esUltima = isLast,
    )
}
