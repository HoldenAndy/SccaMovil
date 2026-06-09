package com.proyecto.scca.presentation.feature.preferencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.scca.core.session.PreferenciasStore
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.domain.model.SesionUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferenciasViewModel
    @Inject
    constructor(
        private val sessionManager: SessionManager,
        private val preferenciasStore: PreferenciasStore,
    ) : ViewModel() {
        val sesion: StateFlow<SesionUsuario?> = sessionManager.sessionFlow

        val tema: StateFlow<String> =
            preferenciasStore.themeFlow
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

        val densidad: StateFlow<String> =
            preferenciasStore.densityFlow
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "comfortable")

        val liveTail: StateFlow<Boolean> =
            preferenciasStore.liveTailFlow
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

        fun setTema(tema: String) {
            viewModelScope.launch { preferenciasStore.setTheme(tema) }
        }

        fun setDensidad(densidad: String) {
            viewModelScope.launch { preferenciasStore.setDensity(densidad) }
        }

        fun setLiveTail(enabled: Boolean) {
            viewModelScope.launch { preferenciasStore.setLiveTail(enabled) }
        }

        fun logout() {
            sessionManager.logout()
        }
    }
