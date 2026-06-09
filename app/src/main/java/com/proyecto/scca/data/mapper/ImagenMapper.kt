package com.proyecto.scca.data.mapper

import com.proyecto.scca.data.remote.dto.ImagenDto
import com.proyecto.scca.domain.model.ImagenAgua

fun ImagenDto.toDomain(): ImagenAgua {
    return ImagenAgua(
        idImagen = idImagen,
        idLectura = idLectura,
        rutaArchivo = rutaArchivo,
        pesoKb = pesoKb,
        fechaHora = fechaHora,
    )
}
