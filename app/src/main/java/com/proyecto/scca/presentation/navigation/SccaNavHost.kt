package com.proyecto.scca.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.presentation.feature.analisis.AnalisisIAScreen
import com.proyecto.scca.presentation.feature.dashboard.DashboardScreen
import com.proyecto.scca.presentation.feature.historial.HistorialScreen
import com.proyecto.scca.presentation.feature.login.CambiarPasswordScreen
import com.proyecto.scca.presentation.feature.login.LoginScreen
import com.proyecto.scca.presentation.feature.logs.LogsScreen
import com.proyecto.scca.presentation.feature.nodos.NodosScreen
import com.proyecto.scca.presentation.feature.preferencias.PreferenciasScreen
import com.proyecto.scca.presentation.feature.usuarios.UsuariosScreen

@Composable
fun SccaNavHost(
    navController: NavHostController = rememberNavController(),
    authGateViewModel: AuthGateViewModel = hiltViewModel(),
) {
    val sesion by authGateViewModel.sessionFlow.collectAsState()

    // Logout event handler
    LaunchedEffect(Unit) {
        authGateViewModel.eventoLogout.collect {
            navController.navigate(Rutas.Login.ruta) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val startDestination =
        when {
            sesion == null -> Rutas.Login.ruta
            sesion?.debeCambiarPassword == true -> Rutas.CambiarPassword.ruta
            else -> Rutas.Dashboard.ruta
        }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Rutas.Login.ruta) {
            LoginScreen(
                onLoginSuccess = { debeCambiar ->
                    if (debeCambiar) {
                        navController.navigate(Rutas.CambiarPassword.ruta) {
                            popUpTo(Rutas.Login.ruta) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Rutas.Dashboard.ruta) {
                            popUpTo(Rutas.Login.ruta) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(Rutas.CambiarPassword.ruta) {
            CambiarPasswordScreen(
                onPasswordChanged = {
                    navController.navigate(Rutas.Dashboard.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(Rutas.Dashboard.ruta) {
            AppScaffold(navController = navController, rol = sesion?.rol, nombreUsuario = sesion?.nombre) {
                DashboardScreen(
                    onOpenAnalisis = {
                        navController.navigate(Rutas.Analisis.ruta) {
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
        composable(Rutas.Historial.ruta) {
            AppScaffold(navController = navController, rol = sesion?.rol, nombreUsuario = sesion?.nombre) {
                HistorialScreen()
            }
        }
        composable(Rutas.Analisis.ruta) {
            AppScaffold(navController = navController, rol = sesion?.rol, nombreUsuario = sesion?.nombre) {
                AnalisisIAScreen()
            }
        }
        composable(Rutas.Nodos.ruta) {
            RolGuard(
                rolActual = sesion?.rol,
                rolesPermitidos = listOf(Rol.ADMINISTRADOR, Rol.SOPORTE, Rol.GESTIONADOR),
                onAccessDenied = { navController.navigateUp() },
            ) {
                AppScaffold(navController = navController, rol = sesion?.rol, nombreUsuario = sesion?.nombre) {
                    NodosScreen()
                }
            }
        }
        composable(Rutas.Usuarios.ruta) {
            RolGuard(
                rolActual = sesion?.rol,
                rolesPermitidos = listOf(Rol.ADMINISTRADOR),
                onAccessDenied = { navController.navigateUp() },
            ) {
                AppScaffold(navController = navController, rol = sesion?.rol, nombreUsuario = sesion?.nombre) {
                    UsuariosScreen()
                }
            }
        }
        composable(Rutas.Logs.ruta) {
            RolGuard(
                rolActual = sesion?.rol,
                rolesPermitidos = listOf(Rol.ADMINISTRADOR, Rol.SOPORTE),
                onAccessDenied = { navController.navigateUp() },
            ) {
                AppScaffold(navController = navController, rol = sesion?.rol, nombreUsuario = sesion?.nombre) {
                    LogsScreen()
                }
            }
        }
        composable(Rutas.Preferencias.ruta) {
            AppScaffold(navController = navController, rol = sesion?.rol, nombreUsuario = sesion?.nombre) {
                PreferenciasScreen(
                    onLogout = {
                        navController.navigate(Rutas.Login.ruta) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
