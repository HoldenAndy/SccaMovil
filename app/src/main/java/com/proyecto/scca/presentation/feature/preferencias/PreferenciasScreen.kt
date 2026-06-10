package com.proyecto.scca.presentation.feature.preferencias

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyecto.scca.presentation.components.SccaCard
import com.proyecto.scca.presentation.components.SccaPageHeader

@Composable
fun PreferenciasScreen(
    onLogout: () -> Unit,
    viewModel: PreferenciasViewModel = hiltViewModel(),
) {
    val sesion by viewModel.sesion.collectAsState()
    val tema by viewModel.tema.collectAsState()
    val densidad by viewModel.densidad.collectAsState()
    val liveTail by viewModel.liveTail.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SccaPageHeader(
            title = "Preferencias",
            subtitle = "Configura la apariencia, densidad y comportamiento del cliente.",
        )

        // Account info
        SccaCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Cuenta",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            sesion?.let {
                Text("Nombre: ${it.nombre}", style = MaterialTheme.typography.bodyMedium)
                Text("Rol: ${it.rol.name}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Theme selector
        SccaCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Tema",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "system" to ("Sistema" to Icons.Filled.SettingsBrightness),
                    "light" to ("Claro" to Icons.Filled.LightMode),
                    "dark" to ("Oscuro" to Icons.Filled.DarkMode),
                ).forEach { (key, pair) ->
                    val (label, icon) = pair
                    FilterChip(
                        selected = tema == key,
                        onClick = { viewModel.setTema(key) },
                        label = { Text(label) },
                        leadingIcon = { Icon(icon, null, Modifier.size(16.dp)) },
                    )
                }
            }
        }

        SccaCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Interfaz",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = densidad == "comfortable",
                    onClick = { viewModel.setDensidad("comfortable") },
                    label = { Text("Cómoda") },
                )
                FilterChip(
                    selected = densidad == "compact",
                    onClick = { viewModel.setDensidad("compact") },
                    label = { Text("Compacta") },
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Auto-actualización de registros", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (liveTail) "Activa cada 5 s" else "Desactivada",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = liveTail,
                    onCheckedChange = viewModel::setLiveTail,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Logout button
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro que deseas salir de tu cuenta?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logout()
                    onLogout()
                }) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            },
        )
    }
}
