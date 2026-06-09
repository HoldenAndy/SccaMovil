package com.proyecto.scca.presentation.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.scca.core.util.Validaciones
import com.proyecto.scca.domain.repository.AuthRepository
import com.proyecto.scca.presentation.components.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CambiarPasswordViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
        val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

        private val _nuevaPassword = MutableStateFlow("")
        val nuevaPassword: StateFlow<String> = _nuevaPassword.asStateFlow()

        private val _confirmarPassword = MutableStateFlow("")
        val confirmarPassword: StateFlow<String> = _confirmarPassword.asStateFlow()

        private val _errorNueva = MutableStateFlow<String?>(null)
        val errorNueva: StateFlow<String?> = _errorNueva.asStateFlow()

        private val _errorConfirmar = MutableStateFlow<String?>(null)
        val errorConfirmar: StateFlow<String?> = _errorConfirmar.asStateFlow()

        fun onNuevaChange(value: String) {
            _nuevaPassword.value = value
            _errorNueva.value = null
        }

        fun onConfirmarChange(value: String) {
            _confirmarPassword.value = value
            _errorConfirmar.value = null
        }

        fun cambiarPassword() {
            val nueva = Validaciones.sanitizarPassword(_nuevaPassword.value)
            val confirmar = Validaciones.sanitizarPassword(_confirmarPassword.value)

            var hasError = false
            if (!Validaciones.validarPasswordMinima(nueva)) {
                _errorNueva.value = "La contraseña debe tener al menos 6 caracteres"
                hasError = true
            }
            if (nueva != confirmar) {
                _errorConfirmar.value = "Las contraseñas no coinciden"
                hasError = true
            }
            if (hasError) return

            viewModelScope.launch {
                _uiState.value = UiState.Loading
                authRepository.cambiarPassword(nueva).fold(
                    onSuccess = { _uiState.value = UiState.Success(Unit) },
                    onFailure = { _uiState.value = UiState.Error(it.message ?: "Error al cambiar la contraseña") },
                )
            }
        }
    }
