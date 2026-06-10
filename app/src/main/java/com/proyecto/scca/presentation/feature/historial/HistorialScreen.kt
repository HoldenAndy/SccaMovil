package com.proyecto.scca.presentation.feature.historial

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyecto.scca.core.util.FechaBackend
import com.proyecto.scca.domain.calidad.NivelCalidad
import com.proyecto.scca.domain.calidad.ParametrosCalidad
import com.proyecto.scca.domain.model.Lectura
import com.proyecto.scca.presentation.components.SccaCard
import com.proyecto.scca.presentation.components.SccaPageHeader
import com.proyecto.scca.presentation.components.SparklineChart
import com.proyecto.scca.presentation.components.StateContent
import com.proyecto.scca.presentation.theme.PhColor
import com.proyecto.scca.presentation.theme.TdsColor
import com.proyecto.scca.presentation.theme.TempColor
import com.proyecto.scca.presentation.theme.TurbColor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(viewModel: HistorialViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val pagina by viewModel.pagina.collectAsState()
    val inicio by viewModel.inicio.collectAsState()
    val fin by viewModel.fin.collectAsState()
    val context = LocalContext.current

    StateContent(
        state = uiState,
        onRetry = viewModel::cargar,
        modifier = Modifier.fillMaxSize(),
    ) { data ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                SccaPageHeader(
                    title = "Historial de lecturas",
                    subtitle = "Registros históricos del nodo activo. Selecciona un rango para visualizar tendencias y métricas agregadas.",
                    actions = {
                        TextButton(
                            onClick = {
                                compartirCsv(
                                    context = context,
                                    lecturas = data.lecturas,
                                )
                            },
                        ) { Text("Exportar CSV") }
                    },
                )
            }

            item {
                RangoHistorialCard(
                    inicio = inicio,
                    fin = fin,
                    onAplicar = viewModel::setRango,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            if (data.graficos.isNotEmpty()) {
                item { GraficosPorAtributo(data.graficos, Modifier.padding(horizontal = 16.dp)) }
            }

            items(data.lecturas, key = { it.idLectura }, contentType = { "lectura" }) { lectura ->
                LecturaCard(lectura = lectura, modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Pagination controls
            if (data.totalPaginas > 1) {
                item {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.irAPagina(pagina - 1) },
                            enabled = pagina > 0,
                        ) { Text("Anterior") }

                        Text("${pagina + 1} / ${data.totalPaginas}")

                        OutlinedButton(
                            onClick = { viewModel.irAPagina(pagina + 1) },
                            enabled = pagina < data.totalPaginas - 1,
                        ) { Text("Siguiente") }
                    }
                }
            }
        }
    }
}

private enum class PresetRango { HOY, SIETE_DIAS, TREINTA_DIAS, PERSONALIZADO }

private fun isoToDisplayDate(iso: String): String =
    runCatching {
        val date = iso.substringBefore("T")
        val (a, m, d) = date.split("-")
        "$d/$m/$a"
    }.getOrDefault(iso)

private fun displayDateToIsoStart(display: String): String =
    runCatching {
        val (d, m, a) = display.trim().split("/")
        "$a-$m-${d}T00:00:00"
    }.getOrDefault(display)

private fun displayDateToIsoEnd(display: String): String =
    runCatching {
        val (d, m, a) = display.trim().split("/")
        "$a-$m-${d}T23:59:59"
    }.getOrDefault(display)

@Composable
fun RangoHistorialCard(
    inicio: String,
    fin: String,
    onAplicar: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = FechaBackend.isoHoy().substringBefore("T")
    var preset by remember(inicio, fin) {
        val iniDate = inicio.substringBefore("T")
        val finDate = fin.substringBefore("T")
        val presetValue =
            if (fin.endsWith("T23:59:59") && inicio.endsWith("T00:00:00") && finDate == today) {
                when (iniDate) {
                    FechaBackend.isoHaceNDias(0).substringBefore("T") -> PresetRango.HOY
                    FechaBackend.isoHaceNDias(7).substringBefore("T") -> PresetRango.SIETE_DIAS
                    FechaBackend.isoHaceNDias(30).substringBefore("T") -> PresetRango.TREINTA_DIAS
                    else -> PresetRango.PERSONALIZADO
                }
            } else {
                PresetRango.PERSONALIZADO
            }
        mutableStateOf(presetValue)
    }
    var inicioDisplay by remember(inicio) { mutableStateOf(isoToDisplayDate(inicio)) }
    var finDisplay by remember(fin) { mutableStateOf(isoToDisplayDate(fin)) }

    val presets =
        listOf(
            PresetRango.HOY to "Hoy",
            PresetRango.SIETE_DIAS to "7 días",
            PresetRango.TREINTA_DIAS to "30 días",
            PresetRango.PERSONALIZADO to "Personalizado",
        )

    SccaCard(modifier = modifier) {
        // Fila 1: icono + label + chips de presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "RANGO",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                presets.forEach { (p, label) ->
                    FilterChip(
                        selected = preset == p,
                        onClick = {
                            preset = p
                            if (p != PresetRango.PERSONALIZADO) {
                                val i =
                                    when (p) {
                                        PresetRango.HOY -> FechaBackend.isoHaceNDias(0)
                                        PresetRango.SIETE_DIAS -> FechaBackend.isoHaceNDias(7)
                                        else -> FechaBackend.isoHaceNDias(30)
                                    }
                                val f = FechaBackend.isoHoy()
                                inicioDisplay = isoToDisplayDate(i)
                                finDisplay = isoToDisplayDate(f)
                                onAplicar(i, f)
                            }
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        shape = MaterialTheme.shapes.small,
                        border =
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = preset == p,
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                            ),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Fila 2: campos de fecha + botón Aplicar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            OutlinedTextField(
                value = inicioDisplay,
                onValueChange = {
                    inicioDisplay = it
                    preset = PresetRango.PERSONALIZADO
                },
                label = { Text("Desde") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
            )
            Text(
                text = "→",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = finDisplay,
                onValueChange = {
                    finDisplay = it
                    preset = PresetRango.PERSONALIZADO
                },
                label = { Text("Hasta") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
            )
            Button(
                onClick = { onAplicar(displayDateToIsoStart(inicioDisplay), displayDateToIsoEnd(finDisplay)) },
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(56.dp),
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Aplicar")
            }
        }
    }
}

@Composable
fun LecturaCard(
    lectura: Lectura,
    modifier: Modifier = Modifier,
) {
    SccaCard(modifier = modifier) {
        Text(
            FechaBackend.formatFechaTabla(lectura.fechaHora),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LecturaMetric("pH", String.format(Locale.US, "%.2f", lectura.ph), PhColor)
            LecturaMetric("Temp", String.format(Locale.US, "%.1f°C", lectura.temperatura), TempColor)
            LecturaMetric("Turb", String.format(Locale.US, "%.1f NTU", lectura.turbidez), TurbColor)
            LecturaMetric("TDS", String.format(Locale.US, "%.0f ppm", lectura.tds), TdsColor)
        }
    }
}

@Composable
fun LecturaMetric(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

private data class GraficoAtributo(
    val label: String,
    val unidad: String,
    val color: Color,
    val min: Float,
    val max: Float,
    val refLine: Float?,
    val refLabel: String?,
    val valor: (Lectura) -> Float,
    val isAlert: (Float) -> Boolean,
)

@Composable
private fun GraficosPorAtributo(
    graficos: List<Lectura>,
    modifier: Modifier = Modifier,
) {
    val sorted = remember(graficos) { graficos.sortedBy { it.fechaHora } }
    val dates = remember(sorted) { sorted.map { FechaBackend.formatFechaTabla(it.fechaHora) } }

    val atributos =
        remember {
            listOf(
                GraficoAtributo(
                    "pH",
                    "",
                    PhColor,
                    6.5f,
                    8.5f,
                    7f,
                    "Neutro",
                    { it.ph.toFloat() },
                ) { v -> ParametrosCalidad.evaluarPh(v.toDouble()) != NivelCalidad.NORMAL },
                GraficoAtributo(
                    "Temperatura",
                    "°C",
                    TempColor,
                    15f,
                    35f,
                    null,
                    null,
                    { it.temperatura.toFloat() },
                ) { v -> ParametrosCalidad.evaluarTemperatura(v.toDouble()) != NivelCalidad.NORMAL },
                GraficoAtributo(
                    "Turbidez",
                    "NTU",
                    TurbColor,
                    0f,
                    5f,
                    5f,
                    "Límite máx.",
                    { it.turbidez.toFloat() },
                ) { v -> ParametrosCalidad.evaluarTurbidez(v.toDouble()) != NivelCalidad.NORMAL },
                GraficoAtributo(
                    "TDS",
                    "ppm",
                    TdsColor,
                    0f,
                    600f,
                    500f,
                    "Límite",
                    { it.tds.toFloat() },
                ) { v -> ParametrosCalidad.evaluarTds(v.toDouble()) != NivelCalidad.NORMAL },
            )
        }

    val allValues = remember(sorted) { atributos.map { attr -> sorted.map(attr.valor) } }
    val allStats =
        remember(allValues) {
            allValues.map { values ->
                if (values.isEmpty()) {
                    Triple("—", "—", "—")
                } else {
                    fun fmt(v: Float) = if (v >= 100f) String.format(Locale.US, "%.0f", v) else String.format(Locale.US, "%.2f", v)
                    Triple(
                        fmt(values.minOrNull() ?: 0f),
                        fmt(values.average().toFloat()),
                        fmt(values.maxOrNull() ?: 0f),
                    )
                }
            }
        }
    val allAlertCounts =
        remember(allValues) {
            atributos.zip(allValues).map { (attr, values) -> values.count { attr.isAlert(it) } }
        }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        atributos.forEachIndexed { i, attr ->
            val values = allValues[i]
            val stats = allStats[i]
            val alertCount = allAlertCounts[i]

            SccaCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(attr.color))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (attr.unidad.isBlank()) attr.label else "${attr.label} (${attr.unidad})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = attr.color,
                            )
                            val rangeText =
                                buildString {
                                    append(formatAxisVal(attr.min))
                                    if (attr.unidad.isNotBlank()) append(" ${attr.unidad}")
                                    append(" – ")
                                    append(formatAxisVal(attr.max))
                                    if (attr.unidad.isNotBlank()) append(" ${attr.unidad}")
                                    if (alertCount > 0) append(" · $alertCount avisos")
                                }
                            Text(
                                text = rangeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("MÍN" to stats.first, "PROM" to stats.second, "MÁX" to stats.third)
                            .forEach { (lbl, v) ->
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = lbl,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.5.sp,
                                    )
                                    Text(
                                        text = v,
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SparklineChart(
                    data = values,
                    color = attr.color,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                    config = SparklineChartConfig(
                        unidad = attr.unidad,
                        label = attr.label,
                        dates = dates,
                        minVal = attr.min,
                        maxVal = attr.max,
                        refLine = attr.refLine,
                        refLabel = attr.refLabel,
                        isAlert = attr.isAlert,
                    ),
                )
            }
        }
    }
}

private fun formatAxisVal(v: Float): String =
    if (v >= 100f) {
        String.format(Locale.US, "%.0f", v)
    } else if (v % 1f == 0f) {
        String.format(Locale.US, "%.0f", v)
    } else {
        String.format(Locale.US, "%.1f", v)
    }

private fun compartirCsv(
    context: android.content.Context,
    lecturas: List<Lectura>,
) {
    val header = "fechaHora,ph,temperatura,turbidez,tds"
    val rows =
        lecturas.joinToString("\n") { lectura ->
            listOf(
                FechaBackend.formatFechaTabla(lectura.fechaHora),
                lectura.ph.toString(),
                lectura.temperatura.toString(),
                lectura.turbidez.toString(),
                lectura.tds.toString(),
            ).joinToString(",")
        }
    val csvDir = java.io.File(context.cacheDir, "csv").also { it.mkdirs() }
    val csvFile = java.io.File(csvDir, "historial-scca.csv")
    csvFile.writeText("$header\n$rows")
    val uri =
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            csvFile,
        )
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "historial-scca.csv")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    context.startActivity(Intent.createChooser(intent, "Exportar historial"))
}
