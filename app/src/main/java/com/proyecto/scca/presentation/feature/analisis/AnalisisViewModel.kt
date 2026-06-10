package com.proyecto.scca.presentation.feature.analisis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.scca.core.session.PreferenciasStore
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.core.util.FechaBackend
import com.proyecto.scca.core.util.Paginacion
import com.proyecto.scca.domain.model.AnalisisIa
import com.proyecto.scca.domain.repository.AnalisisRepository
import com.proyecto.scca.domain.repository.LecturaRepository
import com.proyecto.scca.domain.usecase.ObtenerNodosUseCase
import com.proyecto.scca.presentation.components.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class AnalisisData(
    val analisisList: List<AnalisisIa>,
    val paginaActual: Int,
    val totalPaginas: Int,
)

@HiltViewModel
class AnalisisViewModel
    @Inject
    constructor(
        private val analisisRepository: AnalisisRepository,
        private val lecturaRepository: LecturaRepository,
        private val preferenciasStore: PreferenciasStore,
        private val obtenerNodosUseCase: ObtenerNodosUseCase,
        private val sessionManager: SessionManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<AnalisisData>>(UiState.Loading)
        val uiState: StateFlow<UiState<AnalisisData>> = _uiState.asStateFlow()

        private val _generandoAnalisis = MutableStateFlow(false)
        val generandoAnalisis: StateFlow<Boolean> = _generandoAnalisis.asStateFlow()

        private val _errorGeneracion = MutableStateFlow<String?>(null)
        val errorGeneracion: StateFlow<String?> = _errorGeneracion.asStateFlow()

        private val _inicio = MutableStateFlow(FechaBackend.isoHaceNDias(Paginacion.HISTORY_DAYS))
        val inicio: StateFlow<String> = _inicio.asStateFlow()

        private val _fin = MutableStateFlow(FechaBackend.isoHoy())
        val fin: StateFlow<String> = _fin.asStateFlow()

        private var idNodoActual: Int = -1
        private var pagina = 0

        init {
            viewModelScope.launch {
                idNodoActual = resolverNodoInicial()
                if (idNodoActual < 0) {
                    _uiState.value = UiState.Empty
                } else {
                    cargar()
                }
            }
        }

        private suspend fun resolverNodoInicial(): Int {
            val ultimoId =
                withTimeoutOrNull(3_000) {
                    preferenciasStore.ultimoNodoFlow.firstOrNull()
                }?.toIntOrNull()
            if (ultimoId != null) return ultimoId
            val rol = sessionManager.rolActual ?: return -1
            val nodos = obtenerNodosUseCase(rol).getOrNull().orEmpty()
            val nodo = nodos.find { it.estadoConexion } ?: nodos.firstOrNull()
            nodo?.let { preferenciasStore.setUltimoNodo(it.idNodo.toString()) }
            return nodo?.idNodo ?: -1
        }

        fun setRango(
            nuevoInicio: String,
            nuevoFin: String,
        ) {
            _inicio.value = nuevoInicio
            _fin.value = nuevoFin
            pagina = 0
            cargar()
        }

        fun cargar() {
            if (idNodoActual < 0) return
            viewModelScope.launch {
                _uiState.value = UiState.Loading
                analisisRepository.obtenerAnalisisNodoPaginado(
                    idNodo = idNodoActual,
                    inicio = _inicio.value,
                    fin = _fin.value,
                    pagina = pagina,
                    tamanio = Paginacion.ANALYSIS_PAGE_SIZE,
                ).fold(
                    onSuccess = { p ->
                        if (p.contenido.isEmpty()) {
                            _uiState.value = UiState.Empty
                        } else {
                            _uiState.value =
                                UiState.Success(
                                    AnalisisData(p.contenido, p.numeroPagina, p.totalPaginas),
                                )
                        }
                    },
                    onFailure = { _uiState.value = UiState.Error(it.message ?: "Error") },
                )
            }
        }

        fun generarAnalisis(idLectura: Int) {
            viewModelScope.launch {
                _generandoAnalisis.value = true
                _errorGeneracion.value = null
                analisisRepository.generarAnalisis(idLectura).fold(
                    onSuccess = { cargar() },
                    onFailure = { _errorGeneracion.value = it.message ?: "Error al generar análisis" },
                )
                _generandoAnalisis.value = false
            }
        }

        fun generarAnalisisUltimaLectura() {
            if (idNodoActual < 0) {
                _errorGeneracion.value = "Selecciona un nodo en el panel antes de generar análisis."
                return
            }
            viewModelScope.launch {
                _generandoAnalisis.value = true
                _errorGeneracion.value = null
                lecturaRepository.obtenerUltimaLectura(idNodoActual).fold(
                    onSuccess = { lectura ->
                        analisisRepository.generarAnalisis(lectura.idLectura).fold(
                            onSuccess = { cargar() },
                            onFailure = { _errorGeneracion.value = it.message ?: "Error al generar análisis" },
                        )
                    },
                    onFailure = { _errorGeneracion.value = it.message ?: "No hay lectura disponible para analizar." },
                )
                _generandoAnalisis.value = false
            }
        }

        fun irAPagina(nuevaPagina: Int) {
            pagina = nuevaPagina.coerceAtLeast(0)
            cargar()
        }
    }
