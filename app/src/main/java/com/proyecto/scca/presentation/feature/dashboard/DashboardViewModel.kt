package com.proyecto.scca.presentation.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.scca.core.notifications.NotificationCenter
import com.proyecto.scca.core.notifications.NotificationKind
import com.proyecto.scca.core.session.PreferenciasStore
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.core.util.Constantes
import com.proyecto.scca.core.util.FechaBackend
import com.proyecto.scca.domain.calidad.EvaluacionLectura
import com.proyecto.scca.domain.calidad.ParametrosCalidad
import com.proyecto.scca.domain.model.AnalisisIa
import com.proyecto.scca.domain.model.Lectura
import com.proyecto.scca.domain.model.Nodo
import com.proyecto.scca.domain.repository.AnalisisRepository
import com.proyecto.scca.domain.repository.ImagenRepository
import com.proyecto.scca.domain.repository.LecturaRepository
import com.proyecto.scca.domain.repository.RealtimeRepository
import com.proyecto.scca.domain.usecase.ObtenerNodosUseCase
import com.proyecto.scca.presentation.components.UiState
import com.proyecto.scca.presentation.navigation.Rutas
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardData(
    val nodos: List<Nodo>,
    val nodoSeleccionado: Nodo?,
    val ultimaLectura: Lectura?,
    val evaluacion: EvaluacionLectura?,
    val lecturasSerie: List<Lectura>,
    val sseConectado: Boolean,
    val lecturaConImagen: Boolean,
    val generandoAnalisis: Boolean,
    val errorGeneracion: String?,
    val ultimoAnalisis: AnalisisIa?,
)

@HiltViewModel
class DashboardViewModel
    @Inject
    constructor(
        private val obtenerNodosUseCase: ObtenerNodosUseCase,
        private val lecturaRepository: LecturaRepository,
        private val realtimeRepository: RealtimeRepository,
        private val imagenRepository: ImagenRepository,
        private val analisisRepository: AnalisisRepository,
        private val sessionManager: SessionManager,
        private val preferenciasStore: PreferenciasStore,
        private val notificationCenter: NotificationCenter,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
        val uiState: StateFlow<UiState<DashboardData>> = _uiState.asStateFlow()

        private var pollingJob: Job? = null
        private var sseJob: Job? = null
        private var nodos: List<Nodo> = emptyList()
        private var nodoSeleccionado: Nodo? = null
        private var ultimaLectura: Lectura? = null
        private var lecturasSerie: List<Lectura> = emptyList()
        private var sseConectado = false
        private var lecturaConImagen = false
        private var generandoAnalisis = false
        private var errorGeneracion: String? = null
        private var ultimoAnalisis: AnalisisIa? = null

        init {
            cargarNodos()
        }

        fun cargarNodos() {
            viewModelScope.launch {
                _uiState.value = UiState.Loading
                val rol = sessionManager.rolActual ?: return@launch
                obtenerNodosUseCase(rol).fold(
                    onSuccess = { lista ->
                        nodos = lista
                        if (lista.isEmpty()) {
                            _uiState.value = UiState.Empty
                            return@fold
                        }
                        // Restore last selected node
                        val ultimoId = preferenciasStore.ultimoNodoFlow.first()?.toIntOrNull()
                        val nodo = nodos.find { it.idNodo == ultimoId } ?: nodos.first()
                        seleccionarNodo(nodo)
                    },
                    onFailure = { _uiState.value = UiState.Error(it.message ?: "Error al cargar nodos") },
                )
            }
        }

        fun seleccionarNodo(nodo: Nodo) {
            nodoSeleccionado = nodo
            viewModelScope.launch {
                preferenciasStore.setUltimoNodo(nodo.idNodo.toString())
                cargarDatosNodo(nodo.idNodo)
            }
            iniciarSSE()
        }

        private suspend fun cargarDatosNodo(idNodo: Int) {
            val ahora = FechaBackend.isoHoy()

            lecturaRepository.obtenerUltimaLectura(idNodo).onSuccess { lectura ->
                actualizarLecturaActual(lectura)
            }

            // Chart series (2h window)
            lecturaRepository.obtenerGraficos(idNodo, FechaBackend.isoHaceNDias(1), ahora).onSuccess { serie ->
                lecturasSerie = serie.takeLast(50)
                emitirEstado()
            }
        }

        private fun iniciarSSE() {
            sseJob?.cancel()
            pollingJob?.cancel()
            sseJob =
                viewModelScope.launch {
                    var sseOk = false
                    realtimeRepository.observarLecturas()
                        .onEach { lectura ->
                            if (lectura.idNodo == nodoSeleccionado?.idNodo) {
                                sseOk = true
                                sseConectado = true
                                pollingJob?.cancel()
                                lecturasSerie =
                                    (lecturasSerie.filterNot { it.idLectura == lectura.idLectura } + lectura)
                                        .takeLast(50)
                                viewModelScope.launch {
                                    actualizarLecturaActual(lectura)
                                    emitirEstado()
                                }
                                emitirEstado()
                            }
                        }
                        .catch {
                            sseConectado = false
                            emitirEstado()
                            iniciarPolling()
                        }
                        .launchIn(this)

                    // If SSE doesn't emit in 30s, fall back to polling
                    delay(Constantes.Dashboard.POLLING_MS)
                    if (!sseOk) {
                        sseConectado = false
                        iniciarPolling()
                    }
                }
        }

        private suspend fun actualizarLecturaActual(lectura: Lectura) {
            ultimaLectura = lectura
            lecturaConImagen = imagenRepository.obtenerImagenPorLectura(lectura.idLectura).isSuccess
            ultimoAnalisis =
                analisisRepository.obtenerAnalisisPorLectura(lectura.idLectura)
                    .getOrNull()
            emitirEstado()
        }

        private fun iniciarPolling() {
            pollingJob?.cancel()
            pollingJob =
                viewModelScope.launch {
                    while (isActive) {
                        nodoSeleccionado?.let { cargarDatosNodo(it.idNodo) }
                        delay(Constantes.Dashboard.POLLING_MS)
                    }
                }
        }

        private fun emitirEstado() {
            val lectura = ultimaLectura
            _uiState.value =
                UiState.Success(
                    DashboardData(
                        nodos = nodos,
                        nodoSeleccionado = nodoSeleccionado,
                        ultimaLectura = lectura,
                        evaluacion = lectura?.let { ParametrosCalidad.evaluarLectura(it) },
                        lecturasSerie = lecturasSerie,
                        sseConectado = sseConectado,
                        lecturaConImagen = lecturaConImagen,
                        generandoAnalisis = generandoAnalisis,
                        errorGeneracion = errorGeneracion,
                        ultimoAnalisis = ultimoAnalisis,
                    ),
                )
        }

        fun generarAnalisisActual() {
            val lectura = ultimaLectura
            when {
                lectura == null -> {
                    errorGeneracion = "No hay lectura disponible para analizar."
                    emitirEstado()
                    return
                }
                !lecturaConImagen -> {
                    errorGeneracion = "La lectura actual no tiene imagen asociada."
                    emitirEstado()
                    return
                }
            }

            viewModelScope.launch {
                generandoAnalisis = true
                errorGeneracion = null
                emitirEstado()
                analisisRepository.generarAnalisis(lectura.idLectura).fold(
                    onSuccess = { analisis ->
                        ultimoAnalisis = analisis
                        errorGeneracion = null
                        notificationCenter.push(
                            kind = NotificationKind.SUCCESS,
                            title = "Análisis #${analisis.idAnalisis.toString().padStart(3, '0')} generado",
                            body = analisis.resultadoTexto,
                            route = Rutas.Analisis.ruta,
                        )
                    },
                    onFailure = {
                        errorGeneracion = it.message ?: "No se pudo generar el análisis."
                        notificationCenter.push(
                            kind = NotificationKind.CRITICAL,
                            title = "No se pudo generar el análisis",
                            body = errorGeneracion,
                            route = Rutas.Dashboard.ruta,
                        )
                    },
                )
                generandoAnalisis = false
                emitirEstado()
            }
        }

        override fun onCleared() {
            super.onCleared()
            sseJob?.cancel()
            pollingJob?.cancel()
        }
    }
