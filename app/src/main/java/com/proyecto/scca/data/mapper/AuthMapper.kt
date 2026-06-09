package com.proyecto.scca.data.mapper

import com.proyecto.scca.data.remote.dto.AuthResponseDto
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.domain.model.SesionUsuario

fun AuthResponseDto.toSesionUsuario(): SesionUsuario {
    return SesionUsuario(
        nombre = nombre,
        rol = Rol.valueOf(rol),
        debeCambiarPassword = debeCambiarPassword,
    )
}
