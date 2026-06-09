package com.proyecto.scca.core.network

import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

const val RETRY_DELAY_MS = 1000L

/**
 * Envuelve una llamada a API de forma segura, mapeando excepciones a ApiResult.Error.
 * Para errores 5xx, reintenta una vez despues de RETRY_DELAY_MS.
 */
@Suppress("TooGenericExceptionCaught")
suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(call())
    } catch (e: HttpException) {
        val code = e.code()
        if (code in 500..599) {
            // Reintento unico con backoff para 5xx
            delay(RETRY_DELAY_MS)
            callWithRetry(call)
        } else {
            ApiResult.Error(
                code = code,
                message = ErrorMapper.mensajeParaCodigo(code),
            )
        }
    } catch (e: IOException) {
        val err = ErrorMapper.mapear(e)
        ApiResult.Error(message = err.mensaje)
    } catch (e: RuntimeException) {
        val err = ErrorMapper.mapear(e)
        ApiResult.Error(message = err.mensaje)
    }
}

private suspend fun <T> callWithRetry(call: suspend () -> T): ApiResult<T> =
    runCatching { ApiResult.Success(call()) }
        .getOrElse { retryEx ->
            if (retryEx is HttpException) {
                ApiResult.Error(
                    code = retryEx.code(),
                    message = ErrorMapper.mensajeParaCodigo(retryEx.code()),
                )
            } else {
                val err = ErrorMapper.mapear(retryEx)
                ApiResult.Error(message = err.mensaje)
            }
        }

/**
 * Convierte ApiResult a Result<T>, emitiendo evento de logout si es 401.
 */
fun <T> ApiResult<T>.toResult(onUnauthorized: (() -> Unit)? = null): Result<T> =
    when (this) {
        is ApiResult.Success -> Result.success(data)
        is ApiResult.Error -> {
            if (code == 401) onUnauthorized?.invoke()
            Result.failure(Exception(message))
        }
        ApiResult.Loading -> Result.failure(Exception("Cargando..."))
    }
