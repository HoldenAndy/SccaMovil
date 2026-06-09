package com.proyecto.scca.data.repository

import com.proyecto.scca.core.network.safeApiCall
import com.proyecto.scca.core.network.toResult
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.data.mapper.toDomain
import com.proyecto.scca.data.remote.api.AnalisisApi
import com.proyecto.scca.domain.model.AnalisisIa
import com.proyecto.scca.domain.model.Pagina
import com.proyecto.scca.domain.repository.AnalisisRepository
import javax.inject.Inject

class AnalisisRepositoryImpl
    @Inject
    constructor(
        private val analisisApi: AnalisisApi,
        private val sessionManager: SessionManager,
    ) : AnalisisRepository {
        override suspend fun generarAnalisis(idLectura: Int): Result<AnalisisIa> {
            return safeApiCall { analisisApi.generarAnalisis(idLectura) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }

        override suspend fun obtenerAnalisisNodoPaginado(
            idNodo: Int,
            inicio: String,
            fin: String,
            pagina: Int,
            tamanio: Int,
        ): Result<Pagina<AnalisisIa>> {
            return safeApiCall { analisisApi.obtenerAnalisisNodo(idNodo, inicio, fin, pagina, tamanio) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }

        override suspend fun obtenerAnalisisPorLectura(idLectura: Int): Result<AnalisisIa> {
            return safeApiCall { analisisApi.obtenerPorLectura(idLectura) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }

        override suspend fun obtenerHistorial(): Result<List<AnalisisIa>> {
            return safeApiCall { analisisApi.obtenerHistorial() }
                .toResult { sessionManager.logout() }
                .map { list -> list.map { it.toDomain() } }
        }
    }
