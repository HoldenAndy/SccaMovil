package com.proyecto.scca.data.repository

import com.proyecto.scca.core.network.safeApiCall
import com.proyecto.scca.core.network.toResult
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.data.mapper.toDomain
import com.proyecto.scca.data.remote.api.LecturaApi
import com.proyecto.scca.domain.model.Lectura
import com.proyecto.scca.domain.model.Pagina
import com.proyecto.scca.domain.repository.LecturaRepository
import javax.inject.Inject

class LecturaRepositoryImpl
    @Inject
    constructor(
        private val lecturaApi: LecturaApi,
        private val sessionManager: SessionManager,
    ) : LecturaRepository {
        override suspend fun obtenerUltimaLectura(idNodo: Int): Result<Lectura> {
            return safeApiCall { lecturaApi.obtenerUltima(idNodo) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }

        override suspend fun obtenerLecturasPaginado(
            idNodo: Int,
            inicio: String,
            fin: String,
            pagina: Int,
            tamanio: Int,
            sortBy: String,
            sortDir: String,
        ): Result<Pagina<Lectura>> {
            return safeApiCall {
                lecturaApi.obtenerPaginado(idNodo, inicio, fin, pagina, tamanio, sortBy, sortDir)
            }.toResult { sessionManager.logout() }.map { it.toDomain() }
        }

        override suspend fun obtenerGraficos(
            idNodo: Int,
            inicio: String,
            fin: String,
        ): Result<List<Lectura>> {
            return safeApiCall { lecturaApi.obtenerGraficos(idNodo, inicio, fin) }
                .toResult { sessionManager.logout() }
                .map { list -> list.map { it.toDomain() } }
        }

        override suspend fun obtenerHistorialCompleto(idNodo: Int): Result<List<Lectura>> {
            return safeApiCall { lecturaApi.obtenerHistorialCompleto(idNodo) }
                .toResult { sessionManager.logout() }
                .map { list -> list.map { it.toDomain() } }
        }
    }
