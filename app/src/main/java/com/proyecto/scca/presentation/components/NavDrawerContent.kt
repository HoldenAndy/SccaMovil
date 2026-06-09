package com.proyecto.scca.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.presentation.navigation.Rutas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawerContent(
    rol: Rol?,
    rutaActual: String?,
    onNavigate: (String) -> Unit,
) {
    val items =
        listOf(
            DrawerItem(Rutas.Dashboard.ruta, "Panel", null) {
                Icon(Icons.Filled.Dashboard, contentDescription = null)
            },
            DrawerItem(Rutas.Historial.ruta, "Historial", null) {
                Icon(Icons.Filled.History, contentDescription = null)
            },
            DrawerItem(Rutas.Analisis.ruta, "Análisis IA", null) {
                Icon(Icons.Filled.Analytics, contentDescription = null)
            },
            DrawerItem(Rutas.Nodos.ruta, "Nodos", listOf(Rol.ADMINISTRADOR, Rol.SOPORTE, Rol.GESTIONADOR)) {
                Icon(Icons.Filled.MonitorHeart, contentDescription = null)
            },
            DrawerItem(Rutas.Usuarios.ruta, "Usuarios", listOf(Rol.ADMINISTRADOR)) {
                Icon(Icons.Filled.People, contentDescription = null)
            },
            DrawerItem(Rutas.Logs.ruta, "Registros", listOf(Rol.ADMINISTRADOR, Rol.SOPORTE)) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
            },
            DrawerItem(Rutas.Preferencias.ruta, "Preferencias", null) {
                Icon(Icons.Filled.Settings, contentDescription = null)
            },
        ).filter { it.roles == null || rol in it.roles }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.background,
        drawerContentColor = MaterialTheme.colorScheme.onBackground,
        drawerShape = MaterialTheme.shapes.medium,
        drawerTonalElevation = 0.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .width(246.dp)
                    .fillMaxWidth(),
        ) {
            Spacer(Modifier.width(1.dp))
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    "SCCA",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(5.dp))
                Text("v2.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                "Sistema de Control de Calidad del Agua",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 18.dp),
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp, bottom = 10.dp),
                color = MaterialTheme.colorScheme.outline,
            )
            SccaSectionLabel(
                "Módulos",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
            )
            items.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(item.label, style = MaterialTheme.typography.labelLarge) },
                    selected = rutaActual == item.ruta,
                    onClick = { onNavigate(item.ruta) },
                    icon = item.icon,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 1.dp),
                    shape = MaterialTheme.shapes.small,
                    colors =
                        NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unselectedContainerColor = MaterialTheme.colorScheme.background,
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f),
                        ),
                )
            }
            Spacer(Modifier.weight(1f))
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text(
                    "SCCA · v2.1.0",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private data class DrawerItem(
    val ruta: String,
    val label: String,
    val roles: List<Rol>?,
    val icon: @Composable () -> Unit,
)
