package com.proyecto.scca.presentation.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.proyecto.scca.core.util.FechaBackend
import com.proyecto.scca.domain.calidad.NivelCalidad
import com.proyecto.scca.domain.model.AnalisisIa
import com.proyecto.scca.domain.model.Lectura
import com.proyecto.scca.domain.model.Nodo
import com.proyecto.scca.presentation.components.SccaCard
import com.proyecto.scca.presentation.components.SccaPageHeader
import com.proyecto.scca.presentation.components.SccaSectionLabel
import com.proyecto.scca.presentation.components.SccaStatusDot
import com.proyecto.scca.presentation.components.SensorCard
import com.proyecto.scca.presentation.components.StateContent
import com.proyecto.scca.presentation.theme.PhColor
import com.proyecto.scca.presentation.theme.StatusCritical
import com.proyecto.scca.presentation.theme.StatusNormal
import com.proyecto.scca.presentation.theme.StatusWarning
import com.proyecto.scca.presentation.theme.TdsColor
import com.proyecto.scca.presentation.theme.TempColor
import com.proyecto.scca.presentation.theme.TurbColor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenAnalisis: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    StateContent(
        state = uiState,
        onRetry = viewModel::cargarNodos,
        modifier = Modifier.fillMaxSize(),
    ) { data ->
        if (data.generandoAnalisis) {
            GeneratingAnalysisDialog(lectura = data.ultimaLectura)
        }

        val density = com.proyecto.scca.presentation.theme.LocalSccaDensity.current
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(density.sectionGap),
            contentPadding = PaddingValues(bottom = density.cardPadding),
        ) {
            item {
                SccaPageHeader(
                    title = "Panel de control",
                    subtitle =
                        data.ultimaLectura?.let {
                            "Monitoreo en tiempo real del nodo activo · última lectura a las ${FechaBackend.formatHora(it.fechaHora)}."
                        } ?: "Monitoreo en tiempo real del nodo activo.",
                    actions = {
                        IconButton(
                            onClick = viewModel::generarAnalisisActual,
                            enabled = data.lecturaConImagen && !data.generandoAnalisis,
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = "Generar análisis")
                        }
                        IconButton(onClick = viewModel::cargarNodos) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refrescar")
                        }
                    },
                )
            }

            if (data.nodos.size > 1) {
                item {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(data.nodos, key = { it.idNodo }, contentType = { "nodo_chip" }) { nodo ->
                            FilterChip(
                                selected = nodo.idNodo == data.nodoSeleccionado?.idNodo,
                                onClick = { viewModel.seleccionarNodo(nodo) },
                                label = { Text(nodo.ubicacion) },
                                leadingIcon = {
                                    SccaStatusDot(
                                        color =
                                            if (nodo.estadoConexion) {
                                                StatusNormal
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.28f,
                                                )
                                            },
                                    )
                                },
                                shape = MaterialTheme.shapes.small,
                                border =
                                    FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = nodo.idNodo == data.nodoSeleccionado?.idNodo,
                                        borderColor = MaterialTheme.colorScheme.outline,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val online = data.sseConectado || data.nodoSeleccionado?.estadoConexion == true
                    Icon(
                        imageVector = if (online) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        contentDescription = null,
                        tint = if (online) StatusNormal else StatusCritical,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text =
                            if (online) {
                                "ESP32 en línea${data.nodoSeleccionado?.ubicacion?.let { " · $it" } ?: ""}"
                            } else {
                                "ESP32 desconectado"
                            },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (online) StatusNormal else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (data.evaluacion?.nivelGeneral == NivelCalidad.WARNING || data.evaluacion?.nivelGeneral == NivelCalidad.CRITICAL) {
                item {
                    val critical = data.evaluacion.nivelGeneral == NivelCalidad.CRITICAL
                    Row(
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .background(
                                    if (critical) {
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                                    } else {
                                        StatusWarning.copy(alpha = 0.10f)
                                    },
                                    MaterialTheme.shapes.small,
                                )
                                .border(
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    MaterialTheme.shapes.small,
                                )
                                .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (critical) StatusCritical else StatusWarning,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (critical) "Alerta crítica de calidad" else "Aviso de calidad — turbidez fuera de banda",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (critical) StatusCritical else StatusWarning,
                            )
                            Text(
                                text = data.ultimaLectura?.let {
                                    "La turbidez (${it.turbidez} NTU) supera el límite recomendado de 5.0 NTU. Revisa el sistema de filtración."
                                } ?: "Uno o más parámetros están fuera de la banda recomendada. Revisa el historial y genera un análisis IA si hay imagen asociada.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (!data.lecturaConImagen || data.errorGeneracion != null || data.generandoAnalisis) {
                item {
                    SccaCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text =
                                when {
                                    data.generandoAnalisis -> "Generando análisis IA..."
                                    data.errorGeneracion != null -> data.errorGeneracion
                                    else -> "La última lectura no tiene imagen asociada. El análisis IA requiere una imagen del agua capturada por la cámara ESP32."
                                } ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                if (data.errorGeneracion != null) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SccaSectionLabel("Cámara")
                    CameraPanel(
                        nodo = data.nodoSeleccionado,
                        ultimaLectura = data.ultimaLectura,
                        imagenAgua = data.imagenAgua,
                    )
                }
            }

            if (data.evaluacion != null) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SccaSectionLabel("Parámetros actuales")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SensorCard(
                                titulo = "pH",
                                evaluacion = data.evaluacion.ph,
                                unidad = "",
                                colorSensor = PhColor,
                                history = data.lecturasSerie.map { it.ph.toFloat() },
                                historyDates = data.lecturasSerie.map { FechaBackend.formatFechaTabla(it.fechaHora) },
                                modifier = Modifier.weight(1f),
                            )
                            SensorCard(
                                titulo = "Temperatura",
                                evaluacion = data.evaluacion.temperatura,
                                unidad = "°C",
                                colorSensor = TempColor,
                                history = data.lecturasSerie.map { it.temperatura.toFloat() },
                                historyDates = data.lecturasSerie.map { FechaBackend.formatFechaTabla(it.fechaHora) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SensorCard(
                                titulo = "Turbidez",
                                evaluacion = data.evaluacion.turbidez,
                                unidad = "NTU",
                                colorSensor = TurbColor,
                                history = data.lecturasSerie.map { it.turbidez.toFloat() },
                                historyDates = data.lecturasSerie.map { FechaBackend.formatFechaTabla(it.fechaHora) },
                                modifier = Modifier.weight(1f),
                            )
                            SensorCard(
                                titulo = "TDS",
                                evaluacion = data.evaluacion.tds,
                                unidad = "ppm",
                                colorSensor = TdsColor,
                                history = data.lecturasSerie.map { it.tds.toFloat() },
                                historyDates = data.lecturasSerie.map { FechaBackend.formatFechaTabla(it.fechaHora) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            } else {
                item {
                    SccaCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("Sin lecturas disponibles", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Cuando el nodo activo reporte datos, aparecerán aquí los cuatro parámetros principales.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (data.lecturasSerie.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SccaSectionLabel("Últimas lecturas")
                        val ultimasTres = remember(data.lecturasSerie) { data.lecturasSerie.takeLast(3).reversed() }
                        ultimasTres.forEach { lectura ->
                            SccaCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        FechaBackend.formatFechaTabla(lectura.fechaHora),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        "pH ${String.format(Locale.US, "%.2f", lectura.ph)} · " +
                                            "${String.format(Locale.US, "%.1f", lectura.temperatura)}°C",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            data.ultimoAnalisis?.let { analisis ->
                item {
                    AnalysisSummaryCard(
                        analisis = analisis,
                        lectura = data.ultimaLectura,
                        lecturaConImagen = data.lecturaConImagen,
                        generando = data.generandoAnalisis,
                        onGenerate = viewModel::generarAnalisisActual,
                        onOpenAnalisis = onOpenAnalisis,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPanel(
    nodo: com.proyecto.scca.domain.model.Nodo?,
    ultimaLectura: com.proyecto.scca.domain.model.Lectura?,
    imagenAgua: com.proyecto.scca.domain.model.ImagenAgua?,
    modifier: Modifier = Modifier,
) {
    SccaCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text("Cámara ESP32-CAM", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = nodo?.ubicacion ?: "Sin nodo seleccionado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                SccaStatusDot(if (nodo?.estadoConexion == true) StatusNormal else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (nodo?.estadoConexion == true) "Live" else "Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (nodo?.estadoConexion == true) StatusNormal else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(178.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center,
        ) {
            if (imagenAgua != null) {
                val fullUrl = remember(imagenAgua.rutaArchivo) {
                    if (imagenAgua.rutaArchivo.startsWith("http")) {
                        imagenAgua.rutaArchivo
                    } else {
                        val baseUrl = com.proyecto.scca.BuildConfig.API_BASE_URL.removeSuffix("/")
                        val path = if (imagenAgua.rutaArchivo.startsWith("/")) imagenAgua.rutaArchivo else "/${imagenAgua.rutaArchivo}"
                        "$baseUrl$path"
                    }
                }
                val context = LocalContext.current
                coil.compose.AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(fullUrl)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = "Imagen del agua",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "640 x 480 px",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Actualización cada 10 s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            Text(
                text = "REC",
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = nodo?.macAddress ?: "—",
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Última captura",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                ultimaLectura?.let { FechaBackend.formatHora(it.fechaHora) } ?: "—",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AnalysisSummaryCard(
    analisis: AnalisisIa,
    lectura: Lectura?,
    lecturaConImagen: Boolean,
    generando: Boolean,
    onGenerate: () -> Unit,
    onOpenAnalisis: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SccaCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SccaSectionLabel("Análisis IA · más reciente")
                Text(
                    "${FechaBackend.formatFechaTabla(analisis.fechaHora)} · latencia ${analisis.tiempoResMs} ms · gemini-flash-lite",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onOpenAnalisis) {
                Text("Ver")
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        com.proyecto.scca.presentation.components.MarkdownText(
            markdown = analisis.resultadoTexto,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onGenerate,
            enabled = !generando && lectura != null && lecturaConImagen,
            shape = MaterialTheme.shapes.small,
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (generando) "Generando..." else "Generar nuevo análisis")
        }
        if (!lecturaConImagen) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Requiere imagen de la cámara.",
                style = MaterialTheme.typography.labelSmall,
                color = StatusWarning,
            )
        }
    }
}

@Composable
private fun GeneratingAnalysisDialog(lectura: Lectura?) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Column {
                SccaSectionLabel("Procesando · Gemini Flash Lite")
                Text("Generando análisis IA", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Tomando los valores actuales y la imagen del agua...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (lectura != null) {
                    SccaCard {
                        DialogMetric("pH", String.format(Locale.US, "%.2f", lectura.ph))
                        DialogMetric("Temp", String.format(Locale.US, "%.1f °C", lectura.temperatura))
                        DialogMetric("Turb", String.format(Locale.US, "%.1f NTU", lectura.turbidez))
                        DialogMetric("TDS", String.format(Locale.US, "%.0f ppm", lectura.tds))
                    }
                }
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {},
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun DialogMetric(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
