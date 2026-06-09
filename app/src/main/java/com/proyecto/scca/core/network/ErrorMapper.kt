package com.proyecto.scca.core.network

import java.io.IOException
import java.net.SocketTimeoutException

enum class TipoError {
    NO_CONNECTION,
    SERVER_ERROR,
    NO_NODES,
    UNAUTHORIZED,
    FORBIDDEN,
    UNKNOWN,
}

data class ErrorMapeado(
    val tipo: TipoError,
    val mensaje: String,
)

object ErrorMapper {
    fun mapear(
        throwable: Throwable,
        httpCode: Int? = null,
    ): ErrorMapeado {
        return when {
            throwable is SocketTimeoutException ->
                ErrorMapeado(TipoError.NO_CONNECTION, "El servidor no respondio (timeout 15 s)")

            throwable is IOException ->
                ErrorMapeado(TipoError.NO_CONNECTION, "Sin conexion a internet. Verifica tu red.")

            httpCode == 401 ->
                ErrorMapeado(TipoError.UNAUTHORIZED, "Sesion expirada. Inicia sesion de nuevo.")

            httpCode == 403 ->
                ErrorMapeado(TipoError.FORBIDDEN, "No tienes permiso para realizar esta accion.")

            httpCode != null && httpCode >= 500 ->
                ErrorMapeado(TipoError.SERVER_ERROR, "Error en el servidor. Intenta mas tarde.")

            httpCode == 404 ->
                ErrorMapeado(TipoError.NO_NODES, "No se encontraron datos.")

            else ->
                ErrorMapeado(TipoError.UNKNOWN, throwable.message ?: "Error desconocido.")
        }
    }

    fun mensajeParaCodigo(code: Int): String =
        when (code) {
            401 -> "Sesion expirada. Inicia sesion de nuevo."
            403 -> "No tienes permiso para realizar esta accion."
            404 -> "El recurso solicitado no existe."
            in 500..599 -> "Error en el servidor. Intenta mas tarde."
            else -> "Error desconocido (codigo $code)."
        }
}
