package com.proyecto.scca.domain.repository

data class LoginCredentials(val email: String, val password: String)

interface AuthRepository {
    suspend fun login(credentials: LoginCredentials): Result<Unit>

    suspend fun cambiarPassword(newPassword: String): Result<Unit>
}
