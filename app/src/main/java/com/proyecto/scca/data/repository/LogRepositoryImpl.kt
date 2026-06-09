package com.proyecto.scca.data.repository

import com.proyecto.scca.core.network.safeApiCall
import com.proyecto.scca.core.network.toResult
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.data.mapper.toDomain
import com.proyecto.scca.data.remote.api.LogApi
import com.proyecto.scca.data.remote.dto.LogRequestDto
import com.proyecto.scca.domain.model.LogSistema
import com.proyecto.scca.domain.model.Pagina
import com.proyecto.scca.domain.repository.LogRepository
import javax.inject.Inject

class LogRepositoryImpl
    @Inject
    constructor(
        private val logApi: LogApi,
        private val sessionManager: SessionManager,
    ) : LogRepository {
        override suspend fun listarLogs(): Result<List<LogSistema>> {
            return safeApiCall { logApi.listarLogs() }
                .toResult { sessionManager.logout() }
                .map { list -> list.map { it.toDomain() } }
        }

        override suspend fun buscarLogs(
            nivel: String?,
            modulo: String?,
            inicio: String?,
            fin: String?,
            pagina: Int,
            tamanio: Int,
        ): Result<Pagina<LogSistema>> {
            return safeApiCall { logApi.buscarLogs(nivel, modulo, inicio, fin, pagina, tamanio) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }

        override suspend fun registrarLog(
            nivel: String,
            modulo: String,
            mensaje: String,
        ): Result<LogSistema> {
            val request =
                LogRequestDto(
                    nivel = nivel,
                    modulo = modulo,
                    mensaje = mensaje,
                )
            return safeApiCall { logApi.registrarLog(request) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }
    }
