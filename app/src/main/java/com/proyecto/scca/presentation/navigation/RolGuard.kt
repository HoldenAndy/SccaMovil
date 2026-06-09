package com.proyecto.scca.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.proyecto.scca.domain.model.Rol

@Composable
fun RolGuard(
    rolActual: Rol?,
    rolesPermitidos: List<Rol>,
    onAccessDenied: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (rolActual == null || rolActual !in rolesPermitidos) {
        LaunchedEffect(Unit) { onAccessDenied() }
    } else {
        content()
    }
}
