package com.proyecto.scca.data.repository

import com.proyecto.scca.core.network.safeApiCall
import com.proyecto.scca.core.network.toResult
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.data.mapper.toDomain
import com.proyecto.scca.data.remote.api.ImagenApi
import com.proyecto.scca.data.remote.dto.ImagenRequestDto
import com.proyecto.scca.domain.model.ImagenAgua
import com.proyecto.scca.domain.repository.ImagenRepository
import javax.inject.Inject

class ImagenRepositoryImpl
    @Inject
    constructor(
        private val imagenApi: ImagenApi,
        private val sessionManager: SessionManager,
    ) : ImagenRepository {
        override suspend fun obtenerImagenPorLectura(idLectura: Int): Result<ImagenAgua> {
            return safeApiCall { imagenApi.obtenerPorLectura(idLectura) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }

        override suspend fun listarImagenes(): Result<List<ImagenAgua>> {
            return safeApiCall { imagenApi.listarImagenes() }
                .toResult { sessionManager.logout() }
                .map { list -> list.map { it.toDomain() } }
        }

        override suspend fun registrarImagen(
            idLectura: Int,
            rutaArchivo: String,
            pesoKb: Double,
        ): Result<ImagenAgua> {
            val request =
                ImagenRequestDto(
                    idLectura = idLectura,
                    rutaArchivo = rutaArchivo,
                    pesoKb = pesoKb,
                )
            return safeApiCall { imagenApi.registrarImagen(request) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }
    }
