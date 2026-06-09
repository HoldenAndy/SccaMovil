package com.proyecto.scca.presentation.feature.nodos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyecto.scca.core.util.FechaBackend
import com.proyecto.scca.domain.model.Nodo
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.domain.model.Usuario
import com.proyecto.scca.presentation.components.SccaCard
import com.proyecto.scca.presentation.components.SccaPageHeader
import com.proyecto.scca.presentation.components.StateContent
import com.proyecto.scca.presentation.theme.StatusCritical
import com.proyecto.scca.presentation.theme.StatusNormal

@Composable
fun NodosScreen(viewModel: NodosViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val filtroActivo by viewModel.filtroActivo.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val actionLoading by viewModel.actionLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        SccaPageHeader(title = "Nodos", subtitle = "Dispositivos IoT registrados y estado de conexión.")
        Spacer(Modifier.height(12.dp))

        // Filter chips
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = filtroActivo == null,
                onClick = { viewModel.setFiltroActivo(null) },
                label = { Text("Todos") },
            )
            FilterChip(
                selected = filtroActivo == true,
                onClick = { viewModel.setFiltroActivo(true) },
                label = { Text("Activos") },
            )
            FilterChip(
                selected = filtroActivo == false,
                onClick = { viewModel.setFiltroActivo(false) },
                label = { Text("Inactivos") },
            )
        }
        Spacer(Modifier.height(8.dp))

        StateContent(
            state = uiState,
            onRetry = viewModel::cargar,
            modifier = Modifier.weight(1f),
        ) { data ->
            val conectados = data.nodos.count { it.estadoConexion }
            val desconectados = data.nodos.size - conectados

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    NodeStatsRow(
                        total = data.nodos.size,
                        conectados = conectados,
                        desconectados = desconectados,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (data.rolActual in listOf(Rol.ADMINISTRADOR, Rol.SOPORTE)) {
                    item {
                        RegistrarNodoCard(
                            clientes = data.clientes,
                            loading = actionLoading,
                            error = actionError,
                            onCrear = viewModel::crearNodo,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                items(data.nodos) { nodo ->
                    NodoCard(
                        nodo = nodo,
                        rol = data.rolActual,
                        clientes = data.clientes,
                        loading = actionLoading,
                        onActivar = { viewModel.activarNodo(nodo.idNodo) },
                        onDesactivar = { viewModel.desactivarNodo(nodo.idNodo) },
                        onActualizarUbicacion = { viewModel.actualizarUbicacion(nodo.idNodo, it) },
                        onTransferir = { viewModel.transferirPropietario(nodo.idNodo, it) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (data.totalPaginas > 1) {
                    item {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
        }
    }
}

@Composable
private fun NodeStatsRow(
    total: Int,
    conectados: Int,
    desconectados: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NodeStat("Total", total.toString(), MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
        NodeStat("Conectados", conectados.toString(), StatusNormal, Modifier.weight(1f))
        NodeStat("Desconectados", desconectados.toString(), StatusCritical, Modifier.weight(1f))
    }
}

@Composable
private fun NodeStat(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    SccaCard(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
    }
}

@Composable
private fun RegistrarNodoCard(
    clientes: List<Usuario>,
    loading: Boolean,
    error: String?,
    onCrear: (String, String, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mac by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var clienteId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }

    SccaCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Registrar nodo", style = MaterialTheme.typography.titleSmall)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = mac,
            onValueChange = { mac = it.uppercase().take(17) },
            label = { Text("Dirección MAC") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = ubicacion,
            onValueChange = { ubicacion = it },
            label = { Text("Ubicación") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        )
        Spacer(Modifier.height(8.dp))
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = clientes.isNotEmpty(),
            ) {
                Text(clientes.firstOrNull { it.idUsuario == clienteId }?.nombre ?: "Asignar a cliente")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                clientes.forEach { cliente ->
                    DropdownMenuItem(
                        text = { Text("${cliente.nombre} · ${cliente.email}") },
                        onClick = {
                            clienteId = cliente.idUsuario
                            expanded = false
                        },
                    )
                }
            }
        }
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                onCrear(mac, ubicacion, clienteId)
                if (mac.isNotBlank() && ubicacion.isNotBlank() && clienteId != null) {
                    mac = ""
                    ubicacion = ""
                    clienteId = null
                }
            },
            enabled = !loading && clientes.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        ) {
            Text(if (loading) "Guardando..." else "Registrar nodo")
        }
    }
}

@Composable
fun NodoCard(
    nodo: Nodo,
    rol: Rol?,
    clientes: List<Usuario>,
    loading: Boolean,
    onActivar: () -> Unit,
    onDesactivar: () -> Unit,
    onActualizarUbicacion: (String) -> Unit,
    onTransferir: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showEdit by remember { mutableStateOf(false) }
    var showTransfer by remember { mutableStateOf(false) }

    SccaCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (nodo.estadoConexion) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = null,
                tint = if (nodo.estadoConexion) StatusNormal else StatusCritical,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(nodo.ubicacion, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "#${nodo.idNodo.toString().padStart(3, '0')}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    nodo.macAddress,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Última lectura: ${nodo.ultimaLectura?.let { FechaBackend.formatFechaTabla(it) } ?: "Sin lecturas"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    if (nodo.activo) "Activo en sistema" else "Dado de baja",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (nodo.activo) StatusNormal else StatusCritical,
                )
            }
            // Actions for admin/support
            if (rol in listOf(Rol.ADMINISTRADOR, Rol.SOPORTE)) {
                Column(horizontalAlignment = Alignment.End) {
                    Row {
                        IconButton(onClick = { showEdit = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar ubicación")
                        }
                        IconButton(onClick = { showTransfer = true }) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = "Transferir propietario")
                        }
                    }
                    if (nodo.activo) {
                        TextButton(onClick = onDesactivar) { Text("Desactivar") }
                    } else {
                        TextButton(onClick = onActivar) { Text("Activar") }
                    }
                }
            }
        }
    }

    if (showEdit) {
        EditarUbicacionDialog(
            ubicacionActual = nodo.ubicacion,
            loading = loading,
            onDismiss = { showEdit = false },
            onGuardar = {
                onActualizarUbicacion(it)
                showEdit = false
            },
        )
    }

    if (showTransfer) {
        TransferirNodoDialog(
            clientes = clientes,
            loading = loading,
            onDismiss = { showTransfer = false },
            onTransferir = {
                onTransferir(it)
                showTransfer = false
            },
        )
    }
}

@Composable
private fun EditarUbicacionDialog(
    ubicacionActual: String,
    loading: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (String) -> Unit,
) {
    var ubicacion by remember(ubicacionActual) { mutableStateOf(ubicacionActual) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar ubicación") },
        text = {
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
            )
        },
        confirmButton = {
            Button(onClick = { onGuardar(ubicacion) }, enabled = !loading) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun TransferirNodoDialog(
    clientes: List<Usuario>,
    loading: Boolean,
    onDismiss: () -> Unit,
    onTransferir: (Int?) -> Unit,
) {
    var clienteId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transferir nodo") },
        text = {
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    enabled = clientes.isNotEmpty(),
                ) {
                    Text(clientes.firstOrNull { it.idUsuario == clienteId }?.nombre ?: "Seleccionar cliente")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    clientes.forEach { cliente ->
                        DropdownMenuItem(
                            text = { Text("${cliente.nombre} · ${cliente.email}") },
                            onClick = {
                                clienteId = cliente.idUsuario
                                expanded = false
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTransferir(clienteId) },
                enabled = !loading && clienteId != null,
            ) { Text("Transferir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
