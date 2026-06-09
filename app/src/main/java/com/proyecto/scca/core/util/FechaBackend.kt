package com.proyecto.scca.core.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlin.time.Duration.Companion.days

/**
 * Parsea LocalDateTime del backend que puede llegar como:
 * - String ISO: "2026-05-20T21:58:00"
 * - Array numérico: [2026, 5, 20, 21, 58, 0]
 */
object FechaBackend {
    @OptIn(FormatStringsInDatetimeFormats::class)
    private val isoFormat: DateTimeFormat<LocalDateTime> =
        LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd'T'HH:mm:ss") }

    fun parsear(valor: String): LocalDateTime {
        // Intentar con nanosegundos primero
        return runCatching { LocalDateTime.parse(valor) }
            .getOrElse {
                // Intentar con formato sin nanosegundos
                isoFormat.parse(valor)
            }
    }

    fun parsearArray(
        anio: Int,
        mes: Int,
        dia: Int,
        hora: Int,
        minuto: Int,
        segundo: Int = 0,
    ): LocalDateTime {
        return LocalDateTime(anio, mes, dia, hora, minuto, segundo)
    }

    fun formatHora(fecha: LocalDateTime): String {
        return "${fecha.hour.toString().padStart(2, '0')}:${fecha.minute.toString().padStart(2, '0')}"
    }

    fun formatFechaTabla(fecha: LocalDateTime): String {
        val dia = fecha.dayOfMonth.toString().padStart(2, '0')
        val mes = fecha.monthNumber.toString().padStart(2, '0')
        val anio = fecha.year
        val hora = formatHora(fecha)
        return "$dia/$mes/$anio $hora"
    }

    fun isoHoy(): String {
        val now =
            kotlinx.datetime.Clock.System.now()
                .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        return "${now.date}T23:59:59"
    }

    fun isoHaceNDias(dias: Int): String {
        val now = kotlinx.datetime.Clock.System.now()
        val hace = now.minus(dias.days)
        val local = hace.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        return "${local.date}T00:00:00"
    }
}

/**
 * Serializador JSON personalizado que acepta string ISO o array numérico para LocalDateTime.
 */
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: LocalDateTime,
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: return FechaBackend.parsear(decoder.decodeString())
        val element = jsonDecoder.decodeJsonElement()
        return when (element) {
            is JsonPrimitive -> FechaBackend.parsear(element.content)
            is JsonArray -> {
                val nums = element.map { (it as JsonPrimitive).int }
                FechaBackend.parsearArray(
                    anio = nums[0],
                    mes = nums[1],
                    dia = nums[2],
                    hora = nums[3],
                    minuto = nums[4],
                    segundo = nums.getOrElse(5) { 0 },
                )
            }
            else -> FechaBackend.parsear(element.toString())
        }
    }
}
