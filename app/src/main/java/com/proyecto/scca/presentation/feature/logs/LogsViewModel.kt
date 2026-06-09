package com.proyecto.scca.presentation.feature.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.scca.core.session.PreferenciasStore
import com.proyecto.scca.domain.model.LogSistema
import com.proyecto.scca.domain.repository.LogRepository
import com.proyecto.scca.presentation.components.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogsData(val logs: List<LogSistema>)

@HiltViewModel
class LogsViewModel
    @Inject
    constructor(
        private val logRepository: LogRepository,
        preferenciasStore: PreferenciasStore,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<LogsData>>(UiState.Loading)
        val uiState: StateFlow<UiState<LogsData>> = _uiState.asStateFlow()

        private val _filtroNivel = MutableStateFlow<String?>(null)
        val filtroNivel: StateFlow<String?> = _filtroNivel.asStateFlow()

        private val _filtroModulo = MutableStateFlow("")
        val filtroModulo: StateFlow<String> = _filtroModulo.asStateFlow()

        private val _inicio = MutableStateFlow("")
        val inicio: StateFlow<String> = _inicio.asStateFlow()

        private val _fin = MutableStateFlow("")
        val fin: StateFlow<String> = _fin.asStateFlow()

        val liveTail: StateFlow<Boolean> =
            preferenciasStore.liveTailFlow
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

        init {
            cargar()
        }

        fun cargar() {
            viewModelScope.launch {
                _uiState.value = UiState.Loading
                val hayBusqueda =
                    _filtroNivel.value != null ||
                        _filtroModulo.value.isNotBlank() ||
                        _inicio.value.isNotBlank() ||
                        _fin.value.isNotBlank()
                if (hayBusqueda) {
                    logRepository.buscarLogs(
                        nivel = _filtroNivel.value,
                        modulo = _filtroModulo.value.takeIf { it.isNotBlank() },
                        inicio = _inicio.value.takeIf { it.isNotBlank() },
                        fin = _fin.value.takeIf { it.isNotBlank() },
                        pagina = 0,
                        tamanio = 100,
                    ).fold(
                        onSuccess = { p ->
                            if (p.contenido.isEmpty()) {
                                _uiState.value = UiState.Empty
                            } else {
                                _uiState.value = UiState.Success(LogsData(p.contenido))
                            }
                        },
                        onFailure = { _uiState.value = UiState.Error(it.message ?: "Error") },
                    )
                } else {
                    logRepository.listarLogs().fold(
                        onSuccess = { lista ->
                            if (lista.isEmpty()) {
                                _uiState.value = UiState.Empty
                            } else {
                                _uiState.value = UiState.Success(LogsData(lista))
                            }
                        },
                        onFailure = { _uiState.value = UiState.Error(it.message ?: "Error") },
                    )
                }
            }
        }

        fun setFiltroNivel(nivel: String?) {
            _filtroNivel.value = nivel
            cargar()
        }

        fun setFiltrosAvanzados(
            modulo: String,
            inicio: String,
            fin: String,
        ) {
            _filtroModulo.value = modulo
            _inicio.value = inicio
            _fin.value = fin
            cargar()
        }
    }
