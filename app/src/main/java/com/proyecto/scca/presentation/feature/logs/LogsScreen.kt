package com.proyecto.scca.presentation.feature.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyecto.scca.core.util.FechaBackend
import com.proyecto.scca.domain.model.LogSistema
import com.proyecto.scca.presentation.components.SccaCard
import com.proyecto.scca.presentation.components.SccaPageHeader
import com.proyecto.scca.presentation.components.StateContent

@Composable
fun LogsScreen(viewModel: LogsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val filtroNivel by viewModel.filtroNivel.collectAsState()
    val modulo by viewModel.filtroModulo.collectAsState()
    val inicio by viewModel.inicio.collectAsState()
    val fin by viewModel.fin.collectAsState()
    val liveTail by viewModel.liveTail.collectAsState()
    val niveles = listOf(null, "INFO", "WARN", "ERROR")

    LaunchedEffect(liveTail) {
        if (!liveTail) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(5_000)
            viewModel.cargar()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SccaPageHeader(
            title = "Registros del sistema",
            subtitle =
                if (liveTail) {
                    "Eventos del sistema · auto cada 5 s."
                } else {
                    "Eventos del sistema filtrados por nivel."
                },
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            niveles.forEach { nivel ->
                FilterChip(
                    selected = filtroNivel == nivel,
                    onClick = { viewModel.setFiltroNivel(nivel) },
                    label = { Text(nivel ?: "Todos") },
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        FiltrosLogsCard(
            modulo = modulo,
            inicio = inicio,
            fin = fin,
            onAplicar = viewModel::setFiltrosAvanzados,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))

        StateContent(
            state = uiState,
            onRetry = viewModel::cargar,
            modifier = Modifier.weight(1f),
        ) { data ->
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(data.logs, key = { it.idLog }, contentType = { "log" }) { log ->
                    LogEntry(log = log, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun FiltrosLogsCard(
    modulo: String,
    inicio: String,
    fin: String,
    onAplicar: (String, String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var moduloEdit by remember(modulo) { mutableStateOf(modulo) }
    var inicioEdit by remember(inicio) { mutableStateOf(inicio) }
    var finEdit by remember(fin) { mutableStateOf(fin) }

    SccaCard(modifier = modifier) {
        Text("Búsqueda avanzada", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = moduloEdit,
            onValueChange = { moduloEdit = it },
            label = { Text("Módulo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = inicioEdit,
                onValueChange = { inicioEdit = it },
                label = { Text("Inicio") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
            )
            OutlinedTextField(
                value = finEdit,
                onValueChange = { finEdit = it },
                label = { Text("Fin") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onAplicar(moduloEdit, inicioEdit, finEdit) },
            shape = MaterialTheme.shapes.small,
        ) { Text("Aplicar búsqueda") }
    }
}

@Composable
fun LogEntry(
    log: LogSistema,
    modifier: Modifier = Modifier,
) {
    val color =
        when (log.nivel) {
            "ERROR" -> Color(0xFFD32F2F)
            "WARN" -> Color(0xFFF9A825)
            "INFO" -> Color(0xFF1565C0)
            else -> MaterialTheme.colorScheme.onSurface
        }
    SccaCard(modifier = modifier, containerColor = color.copy(alpha = 0.08f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                log.nivel,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                modifier = Modifier.width(40.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(log.mensaje, style = MaterialTheme.typography.bodySmall)
                Text(
                    "${log.modulo} • ${FechaBackend.formatFechaTabla(log.fechaHora)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }
        }
    }
}
