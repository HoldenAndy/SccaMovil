package com.proyecto.scca.presentation.feature.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.scca.core.session.PreferenciasStore
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.core.util.FechaBackend
import com.proyecto.scca.core.util.Paginacion
import com.proyecto.scca.domain.model.Lectura
import com.proyecto.scca.domain.repository.LecturaRepository
import com.proyecto.scca.domain.usecase.ObtenerNodosUseCase
import com.proyecto.scca.presentation.components.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class HistorialData(
    val lecturas: List<Lectura>,
    val graficos: List<Lectura>,
    val paginaActual: Int,
    val totalPaginas: Int,
    val totalElementos: Long,
)

@HiltViewModel
class HistorialViewModel
    @Inject
    constructor(
        private val lecturaRepository: LecturaRepository,
        private val preferenciasStore: PreferenciasStore,
        private val obtenerNodosUseCase: ObtenerNodosUseCase,
        private val sessionManager: SessionManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<HistorialData>>(UiState.Loading)
        val uiState: StateFlow<UiState<HistorialData>> = _uiState.asStateFlow()

        private val _inicio = MutableStateFlow(FechaBackend.isoHaceNDias(Paginacion.HISTORY_DAYS))
        val inicio: StateFlow<String> = _inicio.asStateFlow()

        private val _fin = MutableStateFlow(FechaBackend.isoHoy())
        val fin: StateFlow<String> = _fin.asStateFlow()

        private val _pagina = MutableStateFlow(0)
        val pagina: StateFlow<Int> = _pagina.asStateFlow()

        private var idNodoActual: Int = -1

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
            val ultimoId = withTimeoutOrNull(3_000) {
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
            inicio: String,
            fin: String,
        ) {
            _inicio.value = inicio
            _fin.value = fin
            _pagina.value = 0
            cargar()
        }

        fun irAPagina(pagina: Int) {
            _pagina.value = pagina
            cargar()
        }

        fun cargar() {
            if (idNodoActual < 0) return
            viewModelScope.launch {
                _uiState.value = UiState.Loading
                val tablaDeferred = async {
                    lecturaRepository.obtenerLecturasPaginado(
                        idNodo = idNodoActual,
                        inicio = _inicio.value,
                        fin = _fin.value,
                        pagina = _pagina.value,
                        tamanio = Paginacion.TABLE_PAGE_SIZE,
                        sortBy = "fechaHora",
                        sortDir = "desc",
                    )
                }
                val graficosDeferred = async {
                    lecturaRepository.obtenerGraficos(
                        idNodo = idNodoActual,
                        inicio = _inicio.value,
                        fin = _fin.value,
                    )
                }
                val tablaResult = tablaDeferred.await()
                val graficos = graficosDeferred.await().getOrNull().orEmpty()
                tablaResult.fold(
                    onSuccess = { pagina ->
                        if (pagina.contenido.isEmpty()) {
                            _uiState.value = UiState.Empty
                        } else {
                            _uiState.value =
                                UiState.Success(
                                    HistorialData(
                                        lecturas = pagina.contenido,
                                        graficos = graficos,
                                        paginaActual = pagina.numeroPagina,
                                        totalPaginas = pagina.totalPaginas,
                                        totalElementos = pagina.totalElementos,
                                    ),
                                )
                        }
                    },
                    onFailure = { _uiState.value = UiState.Error(it.message ?: "Error al cargar historial") },
                )
            }
        }
    }
