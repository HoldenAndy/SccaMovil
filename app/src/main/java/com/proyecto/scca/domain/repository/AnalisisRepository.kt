package com.proyecto.scca.domain.repository

import com.proyecto.scca.domain.model.AnalisisIa
import com.proyecto.scca.domain.model.Pagina

interface AnalisisRepository {
    suspend fun generarAnalisis(idLectura: Int): Result<AnalisisIa>

    suspend fun obtenerAnalisisNodoPaginado(
        idNodo: Int,
        inicio: String,
        fin: String,
        pagina: Int,
        tamanio: Int,
    ): Result<Pagina<AnalisisIa>>

    suspend fun obtenerAnalisisPorLectura(idLectura: Int): Result<AnalisisIa>

    suspend fun obtenerHistorial(): Result<List<AnalisisIa>>
}
