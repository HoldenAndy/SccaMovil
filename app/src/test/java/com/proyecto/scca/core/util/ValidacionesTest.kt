package com.proyecto.scca.core.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValidacionesTest {
    @Test
    fun `validarEmail con email valido`() {
        assertTrue(Validaciones.validarEmail("usuario@ejemplo.com"))
        assertTrue(Validaciones.validarEmail("user.name+tag@sub.domain.org"))
    }

    @Test
    fun `validarEmail con email invalido`() {
        assertFalse(Validaciones.validarEmail("no-at-sign"))
        assertFalse(Validaciones.validarEmail("@nodomain.com"))
        assertFalse(Validaciones.validarEmail(""))
        assertFalse(Validaciones.validarEmail("   "))
    }

    @Test
    fun `validarMac con MAC valida`() {
        assertTrue(Validaciones.validarMac("AA:BB:CC:DD:EE:01"))
        assertTrue(Validaciones.validarMac("00:1A:2B:3C:4D:5E"))
    }

    @Test
    fun `validarMac con MAC invalida`() {
        assertFalse(Validaciones.validarMac("AA-BB-CC-DD-EE-01")) // guiones
        assertFalse(Validaciones.validarMac("AA:BB:CC:DD:EE")) // incompleta
        assertFalse(Validaciones.validarMac("ZZBCC:DD:EE:01")) // char invalido
        assertFalse(Validaciones.validarMac(""))
    }

    @Test
    fun `validarNombre con nombre valido`() {
        assertTrue(Validaciones.validarNombre("Juan"))
        assertTrue(Validaciones.validarNombre("María José"))
        assertTrue(Validaciones.validarNombre("O'Brien"))
    }

    @Test
    fun `validarNombre con nombre invalido`() {
        assertFalse(Validaciones.validarNombre("J")) // muy corto
        assertFalse(Validaciones.validarNombre(""))
        assertFalse(Validaciones.validarNombre("123"))
    }

    @Test
    fun `validarPassword con password valida`() {
        assertTrue(Validaciones.validarPassword("Password1"))
        assertTrue(Validaciones.validarPassword("Abc12345"))
    }

    @Test
    fun `validarPassword con password invalida`() {
        assertFalse(Validaciones.validarPassword("short1A")) // menos de 8
        assertFalse(Validaciones.validarPassword("alllower1")) // sin mayúscula
        assertFalse(Validaciones.validarPassword("ALLUPPER1")) // sin minúscula
        assertFalse(Validaciones.validarPassword("NoNumbers!")) // sin número
    }

    @Test
    fun `validarPasswordMinima replica minimo de web`() {
        assertTrue(Validaciones.validarPasswordMinima("123456"))
        assertFalse(Validaciones.validarPasswordMinima("12345"))
    }

    @Test
    fun `sanitizarEmail convierte a minusculas y trim`() {
        assertEquals("usuario@ejemplo.com", Validaciones.sanitizarEmail("  USUARIO@EJEMPLO.COM  "))
    }

    @Test
    fun `sanitizarTexto elimina caracteres peligrosos`() {
        val resultado = Validaciones.sanitizarTexto("<script>alert('xss')</script>")
        assertFalse(resultado.contains("<"))
        assertFalse(resultado.contains(">"))
        assertFalse(resultado.contains("'"))
    }
}
