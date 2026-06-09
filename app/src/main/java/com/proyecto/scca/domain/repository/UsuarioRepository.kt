package com.proyecto.scca.domain.repository

import com.proyecto.scca.domain.model.Pagina
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.domain.model.Usuario

data class CrearUsuarioRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val rol: Rol,
)

data class ActualizarUsuarioRequest(
    val nombre: String,
    val email: String,
)

interface UsuarioRepository {
    suspend fun listarUsuariosPaginado(
        activo: Boolean?,
        pagina: Int,
        tamanio: Int,
    ): Result<Pagina<Usuario>>

    suspend fun crearUsuario(request: CrearUsuarioRequest): Result<Usuario>

    suspend fun actualizarUsuario(
        id: Int,
        request: ActualizarUsuarioRequest,
    ): Result<Usuario>

    suspend fun cambiarRol(
        id: Int,
        rol: Rol,
    ): Result<Usuario>

    suspend fun activarUsuario(id: Int): Result<Unit>

    suspend fun desactivarUsuario(id: Int): Result<Unit>
}
