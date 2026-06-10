package com.proyecto.scca.data.repository

import com.proyecto.scca.core.network.safeApiCall
import com.proyecto.scca.core.network.toResult
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.data.mapper.toDomain
import com.proyecto.scca.data.remote.api.NodoApi
import com.proyecto.scca.data.remote.dto.NodoRequestDto
import com.proyecto.scca.data.remote.dto.NodoUpdateRequestDto
import com.proyecto.scca.domain.model.Nodo
import com.proyecto.scca.domain.model.Pagina
import com.proyecto.scca.domain.repository.CrearNodoRequest
import com.proyecto.scca.domain.repository.NodoRepository
import javax.inject.Inject

class NodoRepositoryImpl
    @Inject
    constructor(
        private val nodoApi: NodoApi,
        private val sessionManager: SessionManager,
    ) : NodoRepository {

        private data class MisNodosEntry(val token: String, val data: List<Nodo>, val timestamp: Long)

        private var misNodosCache: MisNodosEntry? = null

        companion object {
            private const val MIS_NODOS_TTL_MS = 30_000L  // 30 segundos
        }

        override suspend fun listarNodosPaginado(
            activo: Boolean?,
            pagina: Int,
            tamanio: Int,
        ): Result<Pagina<Nodo>> {
            return safeApiCall { nodoApi.listarNodos(activo, pagina, tamanio) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }

        override suspend fun listarMisNodos(): Result<List<Nodo>> {
            val now = System.currentTimeMillis()
            val token = sessionManager.tokenActual ?: ""
            misNodosCache?.let { entry ->
                if (entry.token == token && now - entry.timestamp < MIS_NODOS_TTL_MS) {
                    return Result.success(entry.data)
                }
            }
            return safeApiCall { nodoApi.misNodos() }
                .toResult { sessionManager.logout() }
                .map { list -> list.map { it.toDomain() } }
                .also { result -> result.getOrNull()?.let { misNodosCache = MisNodosEntry(token, it, now) } }
        }

        override suspend fun crearNodo(request: CrearNodoRequest): Result<Nodo> {
            misNodosCache = null
            return safeApiCall {
                nodoApi.crearNodo(NodoRequestDto(request.macAddress, request.ubicacion, request.idUsuario))
            }.toResult { sessionManager.logout() }.map { it.toDomain() }
        }

        override suspend fun actualizarUbicacion(
            idNodo: Int,
            ubicacion: String,
        ): Result<Nodo> {
            misNodosCache = null
            return safeApiCall { nodoApi.actualizarNodo(idNodo, NodoUpdateRequestDto(ubicacion)) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }

        override suspend fun activarNodo(idNodo: Int): Result<Unit> {
            misNodosCache = null
            return safeApiCall { nodoApi.activarNodo(idNodo) }
                .toResult { sessionManager.logout() }
        }

        override suspend fun desactivarNodo(idNodo: Int): Result<Unit> {
            misNodosCache = null
            return safeApiCall { nodoApi.desactivarNodo(idNodo) }
                .toResult { sessionManager.logout() }
        }

        override suspend fun transferirPropietario(
            idNodo: Int,
            idNuevoUsuario: Int,
        ): Result<Nodo> {
            misNodosCache = null
            return safeApiCall { nodoApi.transferirPropietario(idNodo, idNuevoUsuario) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }
    }
