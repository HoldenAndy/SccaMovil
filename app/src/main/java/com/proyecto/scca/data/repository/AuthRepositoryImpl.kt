package com.proyecto.scca.data.repository

import com.proyecto.scca.core.network.safeApiCall
import com.proyecto.scca.core.network.toResult
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.data.remote.api.AuthApi
import com.proyecto.scca.data.remote.dto.CambiarPasswordRequestDto
import com.proyecto.scca.data.remote.dto.LoginRequestDto
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.domain.repository.AuthRepository
import com.proyecto.scca.domain.repository.LoginCredentials
import javax.inject.Inject

class AuthRepositoryImpl
    @Inject
    constructor(
        private val authApi: AuthApi,
        private val sessionManager: SessionManager,
    ) : AuthRepository {
        override suspend fun login(credentials: LoginCredentials): Result<Unit> {
            val result =
                safeApiCall {
                    authApi.login(LoginRequestDto(credentials.email, credentials.password))
                }
            return result.toResult(onUnauthorized = { sessionManager.logout() }).map { dto ->
                sessionManager.guardarSesion(
                    token = dto.token,
                    nombre = dto.nombre,
                    rol = Rol.valueOf(dto.rol),
                    debeCambiarPassword = dto.debeCambiarPassword,
                )
            }
        }

        override suspend fun cambiarPassword(newPassword: String): Result<Unit> {
            val result =
                safeApiCall {
                    authApi.cambiarPassword(CambiarPasswordRequestDto(newPassword))
                }
            return result.toResult(onUnauthorized = { sessionManager.logout() }).map {
                sessionManager.marcarPasswordCambiado()
            }
        }
    }
