package com.proyecto.scca.domain.calidad

import com.proyecto.scca.domain.model.Lectura

enum class NivelCalidad {
    NORMAL,
    WARNING,
    CRITICAL,
}

data class EvaluacionParametro(
    val nivel: NivelCalidad,
    val valor: Double,
    val porcentaje: Double,
)

data class EvaluacionLectura(
    val ph: EvaluacionParametro,
    val temperatura: EvaluacionParametro,
    val turbidez: EvaluacionParametro,
    val tds: EvaluacionParametro,
    val nivelGeneral: NivelCalidad,
)

object ParametrosCalidad {
    // pH: normal 6.5–8.5 / warn 6.0–9.0 / displayMax 14
    private const val PH_NORMAL_MIN = 6.5
    private const val PH_NORMAL_MAX = 8.5
    private const val PH_WARN_MIN = 6.0
    private const val PH_WARN_MAX = 9.0
    private const val PH_DISPLAY_MAX = 14.0

    // temperatura: normal 15–30 / warn 12–33 / displayMax 50
    private const val TEMP_NORMAL_MIN = 15.0
    private const val TEMP_NORMAL_MAX = 30.0
    private const val TEMP_WARN_MIN = 12.0
    private const val TEMP_WARN_MAX = 33.0
    private const val TEMP_DISPLAY_MAX = 50.0

    // turbidez: normal 0–4 / warnMax 6 / displayMax 10
    private const val TURB_NORMAL_MAX = 4.0
    private const val TURB_WARN_MAX = 6.0
    private const val TURB_DISPLAY_MAX = 10.0

    // tds: normal 0–500 / warnMax 600 / displayMax 1000
    private const val TDS_NORMAL_MAX = 500.0
    private const val TDS_WARN_MAX = 600.0
    private const val TDS_DISPLAY_MAX = 1000.0

    fun evaluarPh(valor: Double): NivelCalidad =
        when {
            valor < PH_WARN_MIN || valor > PH_WARN_MAX -> NivelCalidad.CRITICAL
            valor < PH_NORMAL_MIN || valor > PH_NORMAL_MAX -> NivelCalidad.WARNING
            else -> NivelCalidad.NORMAL
        }

    fun evaluarTemperatura(valor: Double): NivelCalidad =
        when {
            valor < TEMP_WARN_MIN || valor > TEMP_WARN_MAX -> NivelCalidad.CRITICAL
            valor < TEMP_NORMAL_MIN || valor > TEMP_NORMAL_MAX -> NivelCalidad.WARNING
            else -> NivelCalidad.NORMAL
        }

    fun evaluarTurbidez(valor: Double): NivelCalidad =
        when {
            valor > TURB_WARN_MAX -> NivelCalidad.CRITICAL
            valor > TURB_NORMAL_MAX -> NivelCalidad.WARNING
            else -> NivelCalidad.NORMAL
        }

    fun evaluarTds(valor: Double): NivelCalidad =
        when {
            valor > TDS_WARN_MAX -> NivelCalidad.CRITICAL
            valor > TDS_NORMAL_MAX -> NivelCalidad.WARNING
            else -> NivelCalidad.NORMAL
        }

    fun evaluarParametro(
        key: String,
        valor: Double,
    ): NivelCalidad =
        when (key) {
            "ph" -> evaluarPh(valor)
            "temperatura" -> evaluarTemperatura(valor)
            "turbidez" -> evaluarTurbidez(valor)
            "tds" -> evaluarTds(valor)
            else -> NivelCalidad.NORMAL
        }

    fun calcularPorcentaje(
        key: String,
        valor: Double,
    ): Double {
        val (min, max) =
            when (key) {
                "ph" -> 0.0 to PH_DISPLAY_MAX
                "temperatura" -> 0.0 to TEMP_DISPLAY_MAX
                "turbidez" -> 0.0 to TURB_DISPLAY_MAX
                "tds" -> 0.0 to TDS_DISPLAY_MAX
                else -> 0.0 to 100.0
            }
        return ((valor - min) / (max - min)).coerceIn(0.0, 1.0)
    }

    fun evaluarLectura(lectura: Lectura): EvaluacionLectura {
        val ph =
            EvaluacionParametro(
                nivel = evaluarPh(lectura.ph),
                valor = lectura.ph,
                porcentaje = calcularPorcentaje("ph", lectura.ph),
            )
        val temperatura =
            EvaluacionParametro(
                nivel = evaluarTemperatura(lectura.temperatura),
                valor = lectura.temperatura,
                porcentaje = calcularPorcentaje("temperatura", lectura.temperatura),
            )
        val turbidez =
            EvaluacionParametro(
                nivel = evaluarTurbidez(lectura.turbidez),
                valor = lectura.turbidez,
                porcentaje = calcularPorcentaje("turbidez", lectura.turbidez),
            )
        val tds =
            EvaluacionParametro(
                nivel = evaluarTds(lectura.tds),
                valor = lectura.tds,
                porcentaje = calcularPorcentaje("tds", lectura.tds),
            )
        val nivelGeneral =
            listOf(ph.nivel, temperatura.nivel, turbidez.nivel, tds.nivel)
                .maxByOrNull { it.ordinal } ?: NivelCalidad.NORMAL

        return EvaluacionLectura(ph, temperatura, turbidez, tds, nivelGeneral)
    }

    /**
     * Detecta alertas en función de turbidez y pH.
     * CRITICAL domina sobre WARNING.
     */
    fun detectarAlerta(
        turbidez: Double,
        ph: Double,
    ): NivelCalidad {
        val nivelTurb = evaluarTurbidez(turbidez)
        val nivelPh = evaluarPh(ph)
        return listOf(nivelTurb, nivelPh).maxByOrNull { it.ordinal } ?: NivelCalidad.NORMAL
    }
}
