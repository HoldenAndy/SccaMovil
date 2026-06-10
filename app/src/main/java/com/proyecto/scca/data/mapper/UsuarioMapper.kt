package com.proyecto.scca.data.mapper

import com.proyecto.scca.data.remote.dto.PageResponseDto
import com.proyecto.scca.data.remote.dto.UsuarioDto
import com.proyecto.scca.domain.model.Pagina
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.domain.model.Usuario

fun UsuarioDto.toDomain(): Usuario {
    return Usuario(
        idUsuario = idUsuario,
        nombre = nombre,
        email = email,
        rol = runCatching { Rol.valueOf(rol) }.getOrElse { Rol.CLIENTE },
        activo = activo,
    )
}

fun PageResponseDto<UsuarioDto>.toDomain(): Pagina<Usuario> {
    return Pagina(
        contenido = content.map { it.toDomain() },
        numeroPagina = pageNumber,
        tamanioPagina = pageSize,
        totalElementos = totalElements,
        totalPaginas = totalPages,
        esUltima = isLast,
    )
}
