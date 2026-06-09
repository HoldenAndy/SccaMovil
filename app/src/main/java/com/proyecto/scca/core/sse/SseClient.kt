package com.proyecto.scca.core.sse

import com.proyecto.scca.BuildConfig
import com.proyecto.scca.core.session.SessionManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import javax.inject.Inject
import javax.inject.Singleton

sealed class SseEvento {
    data object Conectado : SseEvento()

    data class Lectura(val json: String) : SseEvento()

    data object Ping : SseEvento()

    data class Error(val mensaje: String) : SseEvento()
}

@Singleton
class SseClient
    @Inject
    constructor(
        @com.proyecto.scca.core.network.SseClientQualifier
        private val okHttpClient: OkHttpClient,
        private val sessionManager: SessionManager,
    ) {
        companion object {
            private const val BACKOFF_INICIAL_MS = 1_000L
            private const val BACKOFF_MAX_MS = 30_000L
            private const val SSE_PATH = "/api/v1/sse/lecturas"
        }

        fun observarEventos(): Flow<SseEvento> =
            callbackFlow {
                var backoffMs = BACKOFF_INICIAL_MS
                var eventSource: EventSource? = null
                var activo = true

                fun conectar() {
                    val token = sessionManager.tokenActual ?: return
                    val url = "${BuildConfig.API_BASE_URL}$SSE_PATH?token=$token"
                    val request = Request.Builder().url(url).build()

                    eventSource =
                        EventSources.createFactory(okHttpClient)
                            .newEventSource(
                                request,
                                object : EventSourceListener() {
                                    override fun onEvent(
                                        eventSource: EventSource,
                                        id: String?,
                                        type: String?,
                                        data: String,
                                    ) {
                                        when (type) {
                                            "conectado" -> trySend(SseEvento.Conectado)
                                            "lectura" -> trySend(SseEvento.Lectura(data))
                                            "ping" -> trySend(SseEvento.Ping)
                                            else -> trySend(SseEvento.Lectura(data))
                                        }
                                        backoffMs = BACKOFF_INICIAL_MS
                                    }

                                    override fun onFailure(
                                        eventSource: EventSource,
                                        t: Throwable?,
                                        response: Response?,
                                    ) {
                                        trySend(SseEvento.Error(t?.message ?: "Conexion perdida"))
                                        if (activo) {
                                            // Reconexion con backoff exponencial
                                            launch {
                                                delay(backoffMs)
                                                backoffMs = (backoffMs * 2).coerceAtMost(BACKOFF_MAX_MS)
                                                if (activo) conectar()
                                            }
                                        }
                                    }
                                },
                            )
                }

                conectar()

                awaitClose {
                    activo = false
                    eventSource?.cancel()
                }
            }
    }
