package com.proyecto.scca.domain.repository

import com.proyecto.scca.domain.model.ImagenAgua

interface ImagenRepository {
    suspend fun obtenerImagenPorLectura(idLectura: Int): Result<ImagenAgua>

    suspend fun listarImagenes(): Result<List<ImagenAgua>>

    suspend fun registrarImagen(
        idLectura: Int,
        rutaArchivo: String,
        pesoKb: Double,
    ): Result<ImagenAgua>
}
