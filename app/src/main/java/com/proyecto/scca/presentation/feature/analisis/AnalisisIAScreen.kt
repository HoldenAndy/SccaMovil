package com.proyecto.scca.presentation.feature.analisis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyecto.scca.core.util.FechaBackend
import com.proyecto.scca.domain.model.AnalisisIa
import com.proyecto.scca.presentation.components.SccaCard
import com.proyecto.scca.presentation.components.SccaPageHeader
import com.proyecto.scca.presentation.components.StateContent
import com.proyecto.scca.presentation.feature.historial.RangoHistorialCard

@Composable
fun AnalisisIAScreen(viewModel: AnalisisViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val generando by viewModel.generandoAnalisis.collectAsState()
    val errorGeneracion by viewModel.errorGeneracion.collectAsState()
    val inicio by viewModel.inicio.collectAsState()
    val fin by viewModel.fin.collectAsState()
    var compareOpen by remember { mutableStateOf(false) }

    StateContent(
        state = uiState,
        onRetry = viewModel::cargar,
        modifier = Modifier.fillMaxSize(),
    ) { data ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                SccaPageHeader(
                    title = "Análisis de inteligencia artificial",
                    subtitle =
                        "Interpretación cualitativa de la calidad del agua mediante el modelo " +
                            "Gemini Flash Lite. Latencia típica ~47 s.",
                    actions = {
                        TextButton(
                            onClick = viewModel::generarAnalisisUltimaLectura,
                            enabled = !generando,
                        ) { Text(if (generando) "Generando..." else "Generar") }
                        TextButton(
                            onClick = { compareOpen = true },
                            enabled = data.analisisList.size >= 2,
                        ) { Text("Comparar") }
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

            // Error banner for generation
            item {
                AnimatedVisibility(errorGeneracion != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            errorGeneracion ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }

            // Loading indicator for generation
            item {
                AnimatedVisibility(generando) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Column {
                                Text(
                                    "Generando diagnóstico con IA...",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    "Esto puede tomar hasta 60 segundos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                )
                            }
                        }
                    }
                }
            }

            items(data.analisisList, key = { it.idAnalisis }, contentType = { "analisis" }) { analisis ->
                AnalisisCard(analisis = analisis, modifier = Modifier.padding(horizontal = 16.dp))
            }

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
                            onClick = { viewModel.irAPagina(data.paginaActual - 1) },
                            enabled = data.paginaActual > 0,
                        ) { Text("Anterior") }
                        Text("${data.paginaActual + 1} / ${data.totalPaginas}")
                        OutlinedButton(
                            onClick = { viewModel.irAPagina(data.paginaActual + 1) },
                            enabled = data.paginaActual < data.totalPaginas - 1,
                        ) { Text("Siguiente") }
                    }
                }
            }
        }

        if (compareOpen) {
            AnalysisCompareDialog(
                analyses = data.analisisList,
                onDismiss = { compareOpen = false },
            )
        }
    }
}

@Composable
fun AnalisisCard(
    analisis: AnalisisIa,
    modifier: Modifier = Modifier,
) {
    SccaCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text("Diagnóstico IA", style = MaterialTheme.typography.labelMedium)
            }
            Text(
                FechaBackend.formatFechaTabla(analisis.fechaHora),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        com.proyecto.scca.presentation.components.MarkdownText(markdown = analisis.resultadoTexto)
        Spacer(Modifier.height(4.dp))
        Text(
            "Tiempo de respuesta: ${analisis.tiempoResMs}ms",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AnalysisCompareDialog(
    analyses: List<AnalisisIa>,
    onDismiss: () -> Unit,
) {
    var firstId by remember(analyses) { mutableStateOf(analyses.firstOrNull()?.idAnalisis) }
    var secondId by remember(analyses) { mutableStateOf(analyses.drop(1).firstOrNull()?.idAnalisis) }
    val first = analyses.firstOrNull { it.idAnalisis == firstId }
    val second = analyses.firstOrNull { it.idAnalisis == secondId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comparar análisis") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AnalysisSelector(
                    label = "Análisis A",
                    analyses = analyses,
                    selectedId = firstId,
                    onSelected = { firstId = it },
                )
                AnalysisSelector(
                    label = "Análisis B",
                    analyses = analyses,
                    selectedId = secondId,
                    onSelected = { secondId = it },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompareColumn(first, Modifier.weight(1f))
                    CompareColumn(second, Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun AnalysisSelector(
    label: String,
    analyses: List<AnalisisIa>,
    selectedId: Int?,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = analyses.firstOrNull { it.idAnalisis == selectedId }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                selected?.let { "$label · #${it.idAnalisis.toString().padStart(3, '0')}" } ?: label,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            analyses.forEach { analysis ->
                DropdownMenuItem(
                    text = {
                        Text("#${analysis.idAnalisis.toString().padStart(3, '0')} · ${FechaBackend.formatFechaTabla(analysis.fechaHora)}")
                    },
                    onClick = {
                        onSelected(analysis.idAnalisis)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun CompareColumn(
    analysis: AnalisisIa?,
    modifier: Modifier = Modifier,
) {
    SccaCard(modifier = modifier) {
        Text(
            analysis?.let { "#${it.idAnalisis.toString().padStart(3, '0')}" } ?: "—",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            analysis?.let { FechaBackend.formatFechaTabla(it.fechaHora) } ?: "Sin selección",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        if (analysis != null) {
            com.proyecto.scca.presentation.components.MarkdownText(markdown = analysis.resultadoTexto)
        } else {
            Text("Selecciona un análisis.", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            analysis?.let { "${it.tiempoResMs}ms" } ?: "—",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
