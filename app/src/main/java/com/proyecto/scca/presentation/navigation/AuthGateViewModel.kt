package com.proyecto.scca.presentation.navigation

import androidx.lifecycle.ViewModel
import com.proyecto.scca.core.session.SessionManager
import com.proyecto.scca.domain.model.SesionUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AuthGateViewModel
    @Inject
    constructor(
        private val sessionManager: SessionManager,
    ) : ViewModel() {
        val sessionFlow: StateFlow<SesionUsuario?> = sessionManager.sessionFlow
        val eventoLogout: SharedFlow<Unit> = sessionManager.eventoLogout
    }
