package com.proyecto.scca.domain.repository

import com.proyecto.scca.domain.model.LogSistema
import com.proyecto.scca.domain.model.Pagina

interface LogRepository {
    suspend fun listarLogs(): Result<List<LogSistema>>

    suspend fun buscarLogs(
        nivel: String?,
        modulo: String?,
        inicio: String?,
        fin: String?,
        pagina: Int,
        tamanio: Int,
    ): Result<Pagina<LogSistema>>

    suspend fun registrarLog(
        nivel: String,
        modulo: String,
        mensaje: String,
    ): Result<LogSistema>
}
