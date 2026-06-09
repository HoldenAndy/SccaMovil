package com.proyecto.scca.presentation.feature.nodos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.core.util.Validaciones
import com.proyecto.scca.domain.model.Nodo
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.domain.model.Usuario
import com.proyecto.scca.domain.repository.CrearNodoRequest
import com.proyecto.scca.domain.repository.NodoRepository
import com.proyecto.scca.domain.repository.UsuarioRepository
import com.proyecto.scca.presentation.components.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NodosData(
    val nodos: List<Nodo>,
    val clientes: List<Usuario>,
    val paginaActual: Int,
    val totalPaginas: Int,
    val rolActual: Rol?,
)

@HiltViewModel
class NodosViewModel
    @Inject
    constructor(
        private val nodoRepository: NodoRepository,
        private val usuarioRepository: UsuarioRepository,
        private val sessionManager: SessionManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<NodosData>>(UiState.Loading)
        val uiState: StateFlow<UiState<NodosData>> = _uiState.asStateFlow()

        private val _filtroActivo = MutableStateFlow<Boolean?>(null)
        val filtroActivo: StateFlow<Boolean?> = _filtroActivo.asStateFlow()

        private val _actionError = MutableStateFlow<String?>(null)
        val actionError: StateFlow<String?> = _actionError.asStateFlow()

        private val _actionLoading = MutableStateFlow(false)
        val actionLoading: StateFlow<Boolean> = _actionLoading.asStateFlow()

        private var pagina = 0

        init {
            cargar()
        }

        fun cargar() {
            viewModelScope.launch {
                _uiState.value = UiState.Loading
                nodoRepository.listarNodosPaginado(
                    activo = _filtroActivo.value,
                    pagina = pagina,
                    tamanio = 20,
                ).fold(
                    onSuccess = { p ->
                        val clientes =
                            usuarioRepository.listarUsuariosPaginado(null, 0, 1000)
                                .getOrNull()
                                ?.contenido
                                ?.filter { it.rol == Rol.CLIENTE && it.activo }
                                .orEmpty()
                        _uiState.value =
                            UiState.Success(
                                NodosData(
                                    nodos = p.contenido,
                                    clientes = clientes,
                                    paginaActual = p.numeroPagina,
                                    totalPaginas = p.totalPaginas,
                                    rolActual = sessionManager.rolActual,
                                ),
                            )
                    },
                    onFailure = { _uiState.value = UiState.Error(it.message ?: "Error") },
                )
            }
        }

        fun setFiltroActivo(activo: Boolean?) {
            _filtroActivo.value = activo
            pagina = 0
            cargar()
        }

        fun irAPagina(nuevaPagina: Int) {
            pagina = nuevaPagina.coerceAtLeast(0)
            cargar()
        }

        fun crearNodo(
            macAddress: String,
            ubicacion: String,
            idUsuario: Int?,
        ) {
            val cleanMac = macAddress.trim().uppercase()
            val cleanUbicacion = Validaciones.sanitizarTexto(ubicacion)
            when {
                !Validaciones.validarMac(cleanMac) -> {
                    _actionError.value = "Formato MAC inválido. Usa XX:XX:XX:XX:XX:XX."
                    return
                }
                cleanUbicacion.isBlank() -> {
                    _actionError.value = "La ubicación es obligatoria."
                    return
                }
                idUsuario == null -> {
                    _actionError.value = "Selecciona un cliente para asignar el nodo."
                    return
                }
            }

            viewModelScope.launch {
                _actionLoading.value = true
                _actionError.value = null
                nodoRepository.crearNodo(
                    CrearNodoRequest(
                        macAddress = cleanMac,
                        ubicacion = cleanUbicacion,
                        idUsuario = idUsuario,
                    ),
                ).fold(
                    onSuccess = { cargar() },
                    onFailure = { _actionError.value = it.message ?: "No se pudo registrar el nodo." },
                )
                _actionLoading.value = false
            }
        }

        fun actualizarUbicacion(
            idNodo: Int,
            ubicacion: String,
        ) {
            val cleanUbicacion = Validaciones.sanitizarTexto(ubicacion)
            if (cleanUbicacion.isBlank()) {
                _actionError.value = "La ubicación es obligatoria."
                return
            }
            viewModelScope.launch {
                _actionLoading.value = true
                _actionError.value = null
                nodoRepository.actualizarUbicacion(idNodo, cleanUbicacion).fold(
                    onSuccess = { cargar() },
                    onFailure = { _actionError.value = it.message ?: "No se pudo actualizar el nodo." },
                )
                _actionLoading.value = false
            }
        }

        fun transferirPropietario(
            idNodo: Int,
            idNuevoUsuario: Int?,
        ) {
            if (idNuevoUsuario == null) {
                _actionError.value = "Selecciona el nuevo cliente propietario."
                return
            }
            viewModelScope.launch {
                _actionLoading.value = true
                _actionError.value = null
                nodoRepository.transferirPropietario(idNodo, idNuevoUsuario).fold(
                    onSuccess = { cargar() },
                    onFailure = { _actionError.value = it.message ?: "No se pudo transferir el nodo." },
                )
                _actionLoading.value = false
            }
        }

        fun activarNodo(idNodo: Int) {
            viewModelScope.launch {
                nodoRepository.activarNodo(idNodo).onSuccess { cargar() }
            }
        }

        fun desactivarNodo(idNodo: Int) {
            viewModelScope.launch {
                nodoRepository.desactivarNodo(idNodo).onSuccess { cargar() }
            }
        }
    }
