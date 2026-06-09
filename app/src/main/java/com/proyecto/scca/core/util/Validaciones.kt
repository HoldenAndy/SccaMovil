package com.proyecto.scca.core.util

object Validaciones {
    private val EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
    private val MAC_REGEX = Regex("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$")
    private val NOMBRE_REGEX = Regex("^[a-zA-ZÀ-ɏ\\s'-]{2,100}$")

    fun validarEmail(email: String): Boolean = email.isNotBlank() && EMAIL_REGEX.matches(email.trim())

    fun validarMac(mac: String): Boolean = mac.isNotBlank() && MAC_REGEX.matches(mac.trim().uppercase())

    fun validarNombre(nombre: String): Boolean = nombre.isNotBlank() && NOMBRE_REGEX.matches(nombre.trim())

    fun sanitizarTexto(texto: String): String = texto.trim().replace(Regex("[<>\"']"), "")

    fun sanitizarEmail(email: String): String = email.trim().lowercase()

    fun sanitizarPassword(password: String): String = password.trim()

    fun validarPasswordMinima(password: String): Boolean = password.length >= 6

    /**
     * Valida la complejidad de una contrasena:
     * - Minimo 8 caracteres
     * - Al menos una mayuscula, una minuscula y un numero
     */
    fun validarPassword(password: String): Boolean {
        if (password.length < 8) return false
        val tieneUpper = password.any { it.isUpperCase() }
        val tieneLower = password.any { it.isLowerCase() }
        val tieneDigito = password.any { it.isDigit() }
        return tieneUpper && tieneLower && tieneDigito
    }
}
