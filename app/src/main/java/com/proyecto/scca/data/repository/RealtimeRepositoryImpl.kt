package com.proyecto.scca.data.repository

import com.proyecto.scca.core.sse.SseClient
import com.proyecto.scca.core.sse.SseEvento
import com.proyecto.scca.data.mapper.toDomain
import com.proyecto.scca.data.remote.dto.LecturaDto
import com.proyecto.scca.domain.model.Lectura
import com.proyecto.scca.domain.repository.RealtimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RealtimeRepositoryImpl
    @Inject
    constructor(
        private val sseClient: SseClient,
    ) : RealtimeRepository {
        private val json = Json { ignoreUnknownKeys = true }

        override fun observarLecturas(): Flow<Lectura> {
            return sseClient.observarEventos().mapNotNull { evento ->
                when (evento) {
                    is SseEvento.Lectura ->
                        runCatching { json.decodeFromString<LecturaDto>(evento.json).toDomain() }
                            .getOrNull()
                    else -> null
                }
            }
        }
    }
