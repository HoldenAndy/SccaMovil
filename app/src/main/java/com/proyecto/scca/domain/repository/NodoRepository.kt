package com.proyecto.scca.domain.repository

import com.proyecto.scca.domain.model.Nodo
import com.proyecto.scca.domain.model.Pagina

data class CrearNodoRequest(
    val macAddress: String,
    val ubicacion: String,
    val idUsuario: Int,
)

interface NodoRepository {
    suspend fun listarNodosPaginado(
        activo: Boolean?,
        pagina: Int,
        tamanio: Int,
    ): Result<Pagina<Nodo>>

    suspend fun listarMisNodos(): Result<List<Nodo>>

    suspend fun crearNodo(request: CrearNodoRequest): Result<Nodo>

    suspend fun actualizarUbicacion(
        idNodo: Int,
        ubicacion: String,
    ): Result<Nodo>

    suspend fun activarNodo(idNodo: Int): Result<Unit>

    suspend fun desactivarNodo(idNodo: Int): Result<Unit>

    suspend fun transferirPropietario(
        idNodo: Int,
        idNuevoUsuario: Int,
    ): Result<Nodo>
}
