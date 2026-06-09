package com.proyecto.scca.domain.repository

import com.proyecto.scca.domain.model.Lectura
import kotlinx.coroutines.flow.Flow

interface RealtimeRepository {
    fun observarLecturas(): Flow<Lectura>
}
