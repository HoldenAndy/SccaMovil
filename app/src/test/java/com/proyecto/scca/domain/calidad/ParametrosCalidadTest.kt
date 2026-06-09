package com.proyecto.scca.domain.calidad

import com.proyecto.scca.domain.model.Lectura
import kotlinx.datetime.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ParametrosCalidadTest {
    private fun lectura(
        ph: Double,
        temperatura: Double,
        turbidez: Double,
        tds: Double,
    ): Lectura =
        Lectura(
            idLectura = 1,
            idNodo = 1,
            ph = ph,
            temperatura = temperatura,
            turbidez = turbidez,
            tds = tds,
            fechaHora = LocalDateTime(2026, 5, 20, 12, 0),
        )

    // === pH tests ===
    @Test
    fun `pH normal dentro de rango 6_5 a 8_5`() {
        assertEquals(NivelCalidad.NORMAL, ParametrosCalidad.evaluarPh(7.0))
        assertEquals(NivelCalidad.NORMAL, ParametrosCalidad.evaluarPh(6.5))
        assertEquals(NivelCalidad.NORMAL, ParametrosCalidad.evaluarPh(8.5))
    }

    @Test
    fun `pH warning entre 6_0 y 6_5`() {
        assertEquals(NivelCalidad.WARNING, ParametrosCalidad.evaluarPh(6.0))
        assertEquals(NivelCalidad.WARNING, ParametrosCalidad.evaluarPh(6.2))
    }

    @Test
    fun `pH warning entre 8_5 y 9_0`() {
        assertEquals(NivelCalidad.WARNING, ParametrosCalidad.evaluarPh(8.7))
        assertEquals(NivelCalidad.WARNING, ParametrosCalidad.evaluarPh(9.0))
    }

    @Test
    fun `pH critical menor a 6_0 o mayor a 9_0`() {
        assertEquals(NivelCalidad.CRITICAL, ParametrosCalidad.evaluarPh(5.9))
        assertEquals(NivelCalidad.CRITICAL, ParametrosCalidad.evaluarPh(9.1))
        assertEquals(NivelCalidad.CRITICAL, ParametrosCalidad.evaluarPh(14.0))
    }

    // === Temperatura tests ===
    @Test
    fun `temperatura normal entre 15 y 30`() {
        assertEquals(NivelCalidad.NORMAL, ParametrosCalidad.evaluarTemperatura(20.0))
        assertEquals(NivelCalidad.NORMAL, ParametrosCalidad.evaluarTemperatura(15.0))
        assertEquals(NivelCalidad.NORMAL, ParametrosCalidad.evaluarTemperatura(30.0))
    }

    @Test
    fun `temperatura warning entre 12 y 15 o entre 30 y 33`() {
        assertEquals(NivelCalidad.WARNING, ParametrosCalidad.evaluarTemperatura(12.0))
        assertEquals(NivelCalidad.WARNING, ParametrosCalidad.evaluarTemperatura(13.0))
        assertEquals(NivelCalidad.WARNING, ParametrosCalidad.evaluarTemperatura(31.0))
        assertEquals(NivelCalidad.WARNING, ParametrosCalidad.evaluarTemperatura(33.0))
    }

    @Test
    fun `temperatura critical menor a 12 o mayor a 33`() {
        assertEquals(NivelCalidad.CRITICAL, ParametrosCalidad.evaluarTemperatura(11.9))
        assertEquals(NivelCalidad.CRITICAL, ParametrosCalidad.evaluarTemperatura(33.1))
    }

    // === Turbidez tests ===
    @Test
    fun `turbidez normal entre 0 y 4`() {
        assertEquals(NivelCalidad.NORMAL, ParametrosCalidad.evaluarTurbidez(0.0))
        assertEquals(NivelCalidad.NORMAL, ParametrosCalidad.evaluarTurbidez(4.0))
    }

    @Test
    fun `turbidez warning entre 4 y 6`() {
        assertEquals(NivelCalidad.WARNING, ParametrosCalidad.evaluarTurbidez(4.1))
        assertEquals(NivelCalidad.WARNING, ParametrosCalidad.evaluarTurbidez(6.0))
    }

    @Test
    fun `turbidez critical mayor a 6`() {
        assertEquals(NivelCalidad.CRITICAL, ParametrosCalidad.evaluarTurbidez(6.1))
        assertEquals(NivelCalidad.CRITICAL, ParametrosCalidad.evaluarTurbidez(10.0))
    }

    // === TDS tests ===
    @Test
    fun `tds normal entre 0 y 500`() {
        assertEquals(NivelCalidad.NORMAL, ParametrosCalidad.evaluarTds(0.0))
        assertEquals(NivelCalidad.NORMAL, ParametrosCalidad.evaluarTds(500.0))
    }

    @Test
    fun `tds warning entre 500 y 600`() {
        assertEquals(NivelCalidad.WARNING, ParametrosCalidad.evaluarTds(501.0))
        assertEquals(NivelCalidad.WARNING, ParametrosCalidad.evaluarTds(600.0))
    }

    @Test
    fun `tds critical mayor a 600`() {
        assertEquals(NivelCalidad.CRITICAL, ParametrosCalidad.evaluarTds(601.0))
        assertEquals(NivelCalidad.CRITICAL, ParametrosCalidad.evaluarTds(1000.0))
    }

    // === evaluarLectura: CRITICAL domina WARNING ===
    @Test
    fun `evaluarLectura critical domina cuando hay parametro critico`() {
        val l = lectura(ph = 9.4, temperatura = 31.0, turbidez = 7.1, tds = 640.0) // pH, turb, tds critical
        val evaluacion = ParametrosCalidad.evaluarLectura(l)
        assertEquals(NivelCalidad.CRITICAL, evaluacion.nivelGeneral)
    }

    @Test
    fun `evaluarLectura todos normales`() {
        val l = lectura(ph = 7.2, temperatura = 24.5, turbidez = 1.8, tds = 320.0)
        val evaluacion = ParametrosCalidad.evaluarLectura(l)
        assertEquals(NivelCalidad.NORMAL, evaluacion.nivelGeneral)
        assertEquals(NivelCalidad.NORMAL, evaluacion.ph.nivel)
        assertEquals(NivelCalidad.NORMAL, evaluacion.temperatura.nivel)
        assertEquals(NivelCalidad.NORMAL, evaluacion.turbidez.nivel)
        assertEquals(NivelCalidad.NORMAL, evaluacion.tds.nivel)
    }

    @Test
    fun `detectarAlerta critical por turbidez`() {
        val nivel = ParametrosCalidad.detectarAlerta(turbidez = 7.1, ph = 7.0)
        assertEquals(NivelCalidad.CRITICAL, nivel)
    }

    @Test
    fun `detectarAlerta warning cuando ph en rango warning`() {
        val nivel = ParametrosCalidad.detectarAlerta(turbidez = 1.0, ph = 9.0)
        assertEquals(NivelCalidad.WARNING, nivel)
    }

    @Test
    fun `detectarAlerta normal cuando todo ok`() {
        val nivel = ParametrosCalidad.detectarAlerta(turbidez = 2.0, ph = 7.0)
        assertEquals(NivelCalidad.NORMAL, nivel)
    }

    // === Porcentajes ===
    @Test
    fun `calcularPorcentaje ph 7 es 50 porciento`() {
        val pct = ParametrosCalidad.calcularPorcentaje("ph", 7.0)
        assertEquals(0.5, pct, 0.01)
    }

    @Test
    fun `calcularPorcentaje turbidez 5 es 50 porciento`() {
        val pct = ParametrosCalidad.calcularPorcentaje("turbidez", 5.0)
        assertEquals(0.5, pct, 0.01)
    }
}
