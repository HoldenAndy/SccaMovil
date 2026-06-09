package com.proyecto.scca.data.repository

import com.proyecto.scca.core.network.safeApiCall
import com.proyecto.scca.core.network.toResult
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.data.mapper.toDomain
import com.proyecto.scca.data.remote.api.UsuarioApi
import com.proyecto.scca.data.remote.dto.UsuarioRequestDto
import com.proyecto.scca.data.remote.dto.UsuarioUpdateRequestDto
import com.proyecto.scca.domain.model.Pagina
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.domain.model.Usuario
import com.proyecto.scca.domain.repository.ActualizarUsuarioRequest
import com.proyecto.scca.domain.repository.CrearUsuarioRequest
import com.proyecto.scca.domain.repository.UsuarioRepository
import javax.inject.Inject

class UsuarioRepositoryImpl
    @Inject
    constructor(
        private val usuarioApi: UsuarioApi,
        private val sessionManager: SessionManager,
    ) : UsuarioRepository {
        override suspend fun listarUsuariosPaginado(
            activo: Boolean?,
            pagina: Int,
            tamanio: Int,
        ): Result<Pagina<Usuario>> {
            return safeApiCall { usuarioApi.listarUsuarios(activo, pagina, tamanio) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }

        override suspend fun crearUsuario(request: CrearUsuarioRequest): Result<Usuario> {
            return safeApiCall {
                usuarioApi.crearUsuario(
                    UsuarioRequestDto(request.nombre, request.email, request.password, request.rol.name),
                )
            }.toResult { sessionManager.logout() }.map { it.toDomain() }
        }

        override suspend fun actualizarUsuario(
            id: Int,
            request: ActualizarUsuarioRequest,
        ): Result<Usuario> {
            return safeApiCall {
                usuarioApi.actualizarUsuario(id, UsuarioUpdateRequestDto(request.nombre, request.email))
            }.toResult { sessionManager.logout() }.map { it.toDomain() }
        }

        override suspend fun cambiarRol(
            id: Int,
            rol: Rol,
        ): Result<Usuario> {
            return safeApiCall { usuarioApi.cambiarRol(id, rol.name) }
                .toResult { sessionManager.logout() }
                .map { it.toDomain() }
        }

        override suspend fun activarUsuario(id: Int): Result<Unit> {
            return safeApiCall { usuarioApi.activarUsuario(id) }
                .toResult { sessionManager.logout() }
        }

        override suspend fun desactivarUsuario(id: Int): Result<Unit> {
            return safeApiCall { usuarioApi.desactivarUsuario(id) }
                .toResult { sessionManager.logout() }
        }
    }
