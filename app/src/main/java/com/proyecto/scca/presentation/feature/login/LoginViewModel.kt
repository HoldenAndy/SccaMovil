package com.proyecto.scca.presentation.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.scca.core.util.Validaciones
import com.proyecto.scca.domain.repository.AuthRepository
import com.proyecto.scca.domain.repository.LoginCredentials
import com.proyecto.scca.presentation.components.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiData(
    val debeCambiarPassword: Boolean = false,
)

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<LoginUiData>>(UiState.Empty)
        val uiState: StateFlow<UiState<LoginUiData>> = _uiState.asStateFlow()

        private val _email = MutableStateFlow("")
        val email: StateFlow<String> = _email.asStateFlow()

        private val _password = MutableStateFlow("")
        val password: StateFlow<String> = _password.asStateFlow()

        private val _emailError = MutableStateFlow<String?>(null)
        val emailError: StateFlow<String?> = _emailError.asStateFlow()

        private val _passwordError = MutableStateFlow<String?>(null)
        val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

        fun onEmailChange(value: String) {
            _email.value = value
            _emailError.value = null
        }

        fun onPasswordChange(value: String) {
            _password.value = value
            _passwordError.value = null
        }

        fun login() {
            val emailVal = Validaciones.sanitizarEmail(_email.value)
            val passVal = Validaciones.sanitizarPassword(_password.value)

            var hasError = false
            if (!Validaciones.validarEmail(emailVal)) {
                _emailError.value = "Email inválido"
                hasError = true
            }
            if (passVal.isBlank()) {
                _passwordError.value = "La contraseña es requerida"
                hasError = true
            }
            if (hasError) return

            viewModelScope.launch {
                _uiState.value = UiState.Loading
                val result = authRepository.login(LoginCredentials(emailVal, passVal))
                result.fold(
                    onSuccess = { _uiState.value = UiState.Success(LoginUiData()) },
                    onFailure = { _uiState.value = UiState.Error(it.message ?: "Error al iniciar sesión") },
                )
            }
        }
    }
