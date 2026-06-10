package com.proyecto.scca.presentation.feature.usuarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.domain.model.Usuario
import com.proyecto.scca.presentation.components.SccaCard
import com.proyecto.scca.presentation.components.SccaPageHeader
import com.proyecto.scca.presentation.components.StateContent
import kotlin.random.Random

@Composable
fun UsuariosScreen(viewModel: UsuariosViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val filtroActivo by viewModel.filtroActivo.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val actionLoading by viewModel.actionLoading.collectAsState()
    val credenciales by viewModel.credencialesCreadas.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        SccaPageHeader(
            title = "Gestión de usuarios",
            subtitle = "Cuentas con acceso al sistema y sus roles asociados. Solo administradores pueden crear o modificar.",
        )
        Spacer(Modifier.height(12.dp))

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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CrearUsuarioCard(
                        loading = actionLoading,
                        error = actionError,
                        onCrear = viewModel::crearUsuario,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                items(data.usuarios, key = { it.idUsuario }, contentType = { "usuario" }) { usuario ->
                    UsuarioCard(
                        usuario = usuario,
                        loading = actionLoading,
                        onActivar = { viewModel.activarUsuario(usuario.idUsuario) },
                        onDesactivar = { viewModel.desactivarUsuario(usuario.idUsuario) },
                        onCambiarRol = { nuevoRol -> viewModel.cambiarRol(usuario.idUsuario, nuevoRol) },
                        onActualizar = { nombre, email ->
                            viewModel.actualizarUsuario(usuario.idUsuario, nombre, email)
                        },
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

    if (credenciales != null) {
        AlertDialog(
            onDismissRequest = viewModel::limpiarCredenciales,
            title = { Text("Usuario creado") },
            text = {
                Text(
                    "Entrega estas credenciales temporales:\n\n$credenciales",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(onClick = viewModel::limpiarCredenciales) { Text("Listo") }
            },
        )
    }
}

@Composable
private fun CrearUsuarioCard(
    loading: Boolean,
    error: String?,
    onCrear: (String, String, String, Rol) -> Unit,
    modifier: Modifier = Modifier,
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf(generarPasswordTemporal()) }
    var rol by remember { mutableStateOf(Rol.CLIENTE) }
    var showRolMenu by remember { mutableStateOf(false) }
    val roles = remember { Rol.values() }

    SccaCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Nuevo usuario", style = MaterialTheme.typography.titleSmall)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña temporal") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            trailingIcon = {
                TextButton(onClick = { password = generarPasswordTemporal() }) {
                    Text("Generar")
                }
            },
        )
        Spacer(Modifier.height(8.dp))
        Box {
            OutlinedButton(onClick = { showRolMenu = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Rol: ${rol.name}")
            }
            DropdownMenu(expanded = showRolMenu, onDismissRequest = { showRolMenu = false }) {
                roles.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name) },
                        onClick = {
                            rol = item
                            showRolMenu = false
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
                onCrear(nombre, email, password, rol)
                if (nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                    nombre = ""
                    email = ""
                    password = generarPasswordTemporal()
                    rol = Rol.CLIENTE
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        ) {
            Text(if (loading) "Guardando..." else "Crear usuario")
        }
    }
}

@Composable
fun UsuarioCard(
    usuario: Usuario,
    loading: Boolean,
    onActivar: () -> Unit,
    onDesactivar: () -> Unit,
    onCambiarRol: (Rol) -> Unit,
    onActualizar: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRolMenu by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    val roles = remember { Rol.values() }

    SccaCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.nombre, style = MaterialTheme.typography.titleSmall)
                Text(
                    usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            IconButton(onClick = { showEdit = true }) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar usuario")
            }
            AssistChip(
                onClick = { showRolMenu = true },
                label = { Text(usuario.rol.name) },
            )
            DropdownMenu(
                expanded = showRolMenu,
                onDismissRequest = { showRolMenu = false },
            ) {
                roles.forEach { rol ->
                    DropdownMenuItem(
                        text = { Text(rol.name) },
                        onClick = {
                            onCambiarRol(rol)
                            showRolMenu = false
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            if (usuario.activo) {
                OutlinedButton(onClick = onDesactivar) { Text("Desactivar") }
            } else {
                Button(onClick = onActivar) { Text("Activar") }
            }
        }
    }

    if (showEdit) {
        EditarUsuarioDialog(
            usuario = usuario,
            loading = loading,
            onDismiss = { showEdit = false },
            onGuardar = { nombre, email ->
                onActualizar(nombre, email)
                showEdit = false
            },
        )
    }
}

@Composable
private fun EditarUsuarioDialog(
    usuario: Usuario,
    loading: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (String, String) -> Unit,
) {
    var nombre by remember(usuario.idUsuario) { mutableStateOf(usuario.nombre) }
    var email by remember(usuario.idUsuario) { mutableStateOf(usuario.email) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onGuardar(nombre, email) }, enabled = !loading) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

private fun generarPasswordTemporal(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789"
    val suffix = listOf('A', 'a', '1').joinToString("")
    return buildString {
        repeat(7) {
            append(chars[Random.nextInt(chars.length)])
        }
        append(suffix)
    }
}
