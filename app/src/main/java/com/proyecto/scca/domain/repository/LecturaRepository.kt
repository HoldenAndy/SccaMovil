package com.proyecto.scca.domain.repository

import com.proyecto.scca.domain.model.Lectura
import com.proyecto.scca.domain.model.Pagina

interface LecturaRepository {
    suspend fun obtenerUltimaLectura(idNodo: Int): Result<Lectura>

    suspend fun obtenerLecturasPaginado(
        idNodo: Int,
        inicio: String,
        fin: String,
        pagina: Int,
        tamanio: Int,
        sortBy: String,
        sortDir: String,
    ): Result<Pagina<Lectura>>

    suspend fun obtenerGraficos(
        idNodo: Int,
        inicio: String,
        fin: String,
    ): Result<List<Lectura>>

    suspend fun obtenerHistorialCompleto(idNodo: Int): Result<List<Lectura>>
}
