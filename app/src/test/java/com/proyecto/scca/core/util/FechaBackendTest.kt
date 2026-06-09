package com.proyecto.scca.core.util

import kotlinx.datetime.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FechaBackendTest {
    @Test
    fun `parsear string ISO sin nanosegundos`() {
        val result = FechaBackend.parsear("2026-05-20T21:58:00")
        assertEquals(2026, result.year)
        assertEquals(5, result.monthNumber)
        assertEquals(20, result.dayOfMonth)
        assertEquals(21, result.hour)
        assertEquals(58, result.minute)
        assertEquals(0, result.second)
    }

    @Test
    fun `parsear string ISO con formato LocalDateTime`() {
        val result = FechaBackend.parsear("2026-05-20T22:00:05")
        assertEquals(2026, result.year)
        assertEquals(22, result.hour)
        assertEquals(0, result.minute)
        assertEquals(5, result.second)
    }

    @Test
    fun `parsearArray produce el mismo instante que string ISO`() {
        val fromString = FechaBackend.parsear("2026-05-20T22:00:05")
        val fromArray = FechaBackend.parsearArray(2026, 5, 20, 22, 0, 5)
        assertEquals(fromString, fromArray)
    }

    @Test
    fun `parsearArray sin segundos usa cero`() {
        val result = FechaBackend.parsearArray(2026, 5, 20, 21, 58)
        assertEquals(0, result.second)
    }

    @Test
    fun `formatHora produce formato HH_MM`() {
        val fecha = LocalDateTime(2026, 5, 20, 9, 5)
        assertEquals("09:05", FechaBackend.formatHora(fecha))
    }

    @Test
    fun `formatFechaTabla produce formato DD_MM_YYYY HH_MM`() {
        val fecha = LocalDateTime(2026, 5, 20, 21, 58)
        val resultado = FechaBackend.formatFechaTabla(fecha)
        assertTrue(resultado.contains("20/05/2026"), "Fecha esperada: $resultado")
        assertTrue(resultado.contains("21:58"))
    }

    @Test
    fun `isoHoy produce fecha en formato ISO`() {
        val resultado = FechaBackend.isoHoy()
        assertTrue(resultado.contains("T23:59:59"), "Se esperaba T23:59:59 en: $resultado")
    }

    @Test
    fun `isoHaceNDias produce fecha correcta`() {
        val resultado = FechaBackend.isoHaceNDias(7)
        assertTrue(resultado.contains("T00:00:00"), "Se esperaba T00:00:00 en: $resultado")
    }
}
