package com.proyecto.scca.presentation.feature.historial

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyecto.scca.core.util.FechaBackend
import com.proyecto.scca.domain.model.Lectura
import com.proyecto.scca.presentation.components.SccaCard
import com.proyecto.scca.presentation.components.SccaPageHeader
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
                    title = "Historial",
                    subtitle = "Total: ${data.totalElementos} lecturas registradas.",
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

            items(data.lecturas) { lectura ->
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

@Composable
fun RangoHistorialCard(
    inicio: String,
    fin: String,
    onAplicar: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var inicioEdit by remember(inicio) { mutableStateOf(inicio) }
    var finEdit by remember(fin) { mutableStateOf(fin) }
    SccaCard(modifier = modifier) {
        Text("Rango de fechas", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = inicioEdit,
            onValueChange = { inicioEdit = it },
            label = { Text("Inicio") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = finEdit,
            onValueChange = { finEdit = it },
            label = { Text("Fin") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onAplicar(inicioEdit, finEdit) },
            shape = MaterialTheme.shapes.small,
        ) { Text("Aplicar filtros") }
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
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "historial-scca.csv")
            putExtra(Intent.EXTRA_TEXT, "$header\n$rows")
        }
    context.startActivity(Intent.createChooser(intent, "Exportar historial"))
}
