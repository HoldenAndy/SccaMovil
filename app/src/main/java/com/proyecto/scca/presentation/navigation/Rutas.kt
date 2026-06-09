package com.proyecto.scca.presentation.navigation

sealed class Rutas(val ruta: String) {
    data object Login : Rutas("login")

    data object CambiarPassword : Rutas("cambiar_password")

    data object Dashboard : Rutas("dashboard")

    data object Historial : Rutas("historial")

    data object Analisis : Rutas("analisis")

    data object Nodos : Rutas("nodos")

    data object Usuarios : Rutas("usuarios")

    data object Logs : Rutas("logs")

    data object Preferencias : Rutas("preferencias")
}
