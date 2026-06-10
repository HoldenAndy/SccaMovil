package com.proyecto.scca.presentation.feature.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.scca.core.util.Validaciones
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.domain.model.Usuario
import com.proyecto.scca.domain.repository.ActualizarUsuarioRequest
import com.proyecto.scca.domain.repository.CrearUsuarioRequest
import com.proyecto.scca.domain.repository.UsuarioRepository
import com.proyecto.scca.presentation.components.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UsuariosData(
    val usuarios: List<Usuario>,
    val paginaActual: Int,
    val totalPaginas: Int,
)

@HiltViewModel
class UsuariosViewModel
    @Inject
    constructor(
        private val usuarioRepository: UsuarioRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<UsuariosData>>(UiState.Loading)
        val uiState: StateFlow<UiState<UsuariosData>> = _uiState.asStateFlow()

        private val _filtroActivo = MutableStateFlow<Boolean?>(null)
        val filtroActivo: StateFlow<Boolean?> = _filtroActivo.asStateFlow()

        private val _actionError = MutableStateFlow<String?>(null)
        val actionError: StateFlow<String?> = _actionError.asStateFlow()

        private val _actionLoading = MutableStateFlow(false)
        val actionLoading: StateFlow<Boolean> = _actionLoading.asStateFlow()

        private val _credencialesCreadas = MutableStateFlow<String?>(null)
        val credencialesCreadas: StateFlow<String?> = _credencialesCreadas.asStateFlow()

        private var pagina = 0

        init {
            cargar()
        }

        fun cargar() {
            viewModelScope.launch { cargarInterno() }
        }

        private suspend fun cargarInterno() {
            _uiState.value = UiState.Loading
            usuarioRepository.listarUsuariosPaginado(_filtroActivo.value, pagina, 20).fold(
                onSuccess = { p ->
                    _uiState.value = UiState.Success(UsuariosData(p.contenido, p.numeroPagina, p.totalPaginas))
                },
                onFailure = { _uiState.value = UiState.Error(it.message ?: "Error") },
            )
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

        fun crearUsuario(
            nombre: String,
            email: String,
            password: String,
            rol: Rol,
        ) {
            val cleanNombre = Validaciones.sanitizarTexto(nombre)
            val cleanEmail = Validaciones.sanitizarEmail(email)
            val cleanPassword = Validaciones.sanitizarPassword(password)
            when {
                !Validaciones.validarNombre(cleanNombre) -> {
                    _actionError.value = "Ingresa un nombre válido."
                    return
                }
                !Validaciones.validarEmail(cleanEmail) -> {
                    _actionError.value = "Ingresa un correo válido."
                    return
                }
                !Validaciones.validarPasswordMinima(cleanPassword) -> {
                    _actionError.value = "La contraseña debe tener al menos 6 caracteres."
                    return
                }
            }

            viewModelScope.launch {
                _actionLoading.value = true
                _actionError.value = null
                val result = usuarioRepository.crearUsuario(
                    CrearUsuarioRequest(
                        nombre = cleanNombre,
                        email = cleanEmail,
                        password = cleanPassword,
                        rol = rol,
                    ),
                )
                if (result.isSuccess) {
                    _credencialesCreadas.value = "${result.getOrNull()?.nombre}\n${result.getOrNull()?.email}\n$cleanPassword"
                    cargarInterno()
                } else {
                    _actionError.value = result.exceptionOrNull()?.message ?: "No se pudo crear el usuario."
                }
                _actionLoading.value = false
            }
        }

        fun actualizarUsuario(
            id: Int,
            nombre: String,
            email: String,
        ) {
            val cleanNombre = Validaciones.sanitizarTexto(nombre)
            val cleanEmail = Validaciones.sanitizarEmail(email)
            when {
                !Validaciones.validarNombre(cleanNombre) -> {
                    _actionError.value = "Ingresa un nombre válido."
                    return
                }
                !Validaciones.validarEmail(cleanEmail) -> {
                    _actionError.value = "Ingresa un correo válido."
                    return
                }
            }

            viewModelScope.launch {
                _actionLoading.value = true
                _actionError.value = null
                val result = usuarioRepository.actualizarUsuario(
                    id,
                    ActualizarUsuarioRequest(nombre = cleanNombre, email = cleanEmail),
                )
                if (result.isSuccess) {
                    cargarInterno()
                } else {
                    _actionError.value = result.exceptionOrNull()?.message ?: "No se pudo actualizar el usuario."
                }
                _actionLoading.value = false
            }
        }

        fun limpiarCredenciales() {
            _credencialesCreadas.value = null
        }

        fun activarUsuario(id: Int) = viewModelScope.launch { usuarioRepository.activarUsuario(id).onSuccess { cargar() } }

        fun desactivarUsuario(id: Int) = viewModelScope.launch { usuarioRepository.desactivarUsuario(id).onSuccess { cargar() } }

        fun cambiarRol(
            id: Int,
            rol: Rol,
        ) = viewModelScope.launch { usuarioRepository.cambiarRol(id, rol).onSuccess { cargar() } }
    }
