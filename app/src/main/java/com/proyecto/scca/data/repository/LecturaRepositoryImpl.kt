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

        private data class GraficosCacheKey(val idNodo: Int, val inicio: String, val fin: String)
        private data class CacheEntry<T>(val value: T, val timestamp: Long)

        private val graficosCache = mutableMapOf<GraficosCacheKey, CacheEntry<List<Lectura>>>()
        private val ultimaLecturaCache = mutableMapOf<Int, CacheEntry<Lectura>>()

        companion object {
            private const val GRAFICOS_TTL_MS = 5 * 60 * 1_000L  // 5 minutos
            private const val ULTIMA_LECTURA_TTL_MS = 10_000L     // 10 segundos
        }

        override suspend fun obtenerUltimaLectura(idNodo: Int): Result<Lectura> {
            val now = System.currentTimeMillis()
            ultimaLecturaCache[idNodo]?.let { entry ->
                if (now - entry.timestamp < ULTIMA_LECTURA_TTL_MS) return Result.success(entry.value)
            }
            return safeApiCall { lecturaApi.obtenerUltima(idNodo) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
                .also { result -> result.getOrNull()?.let { ultimaLecturaCache[idNodo] = CacheEntry(it, now) } }
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
            val key = GraficosCacheKey(idNodo, inicio, fin)
            val now = System.currentTimeMillis()
            graficosCache[key]?.let { entry ->
                if (now - entry.timestamp < GRAFICOS_TTL_MS) return Result.success(entry.value)
            }
            return safeApiCall { lecturaApi.obtenerGraficos(idNodo, inicio, fin) }
                .toResult { sessionManager.logout() }
                .map { list -> list.map { it.toDomain() } }
                .also { result -> result.getOrNull()?.let { graficosCache[key] = CacheEntry(it, now) } }
        }

        override suspend fun obtenerHistorialCompleto(idNodo: Int): Result<List<Lectura>> {
            return safeApiCall { lecturaApi.obtenerHistorialCompleto(idNodo) }
                .toResult { sessionManager.logout() }
                .map { list -> list.map { it.toDomain() } }
        }
    }
