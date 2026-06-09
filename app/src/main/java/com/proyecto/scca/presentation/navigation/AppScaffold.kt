package com.proyecto.scca.presentation.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.proyecto.scca.core.notifications.AppNotification
import com.proyecto.scca.core.notifications.NotificationKind
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.presentation.components.NavDrawerContent
import com.proyecto.scca.presentation.components.SccaStatusDot
import com.proyecto.scca.presentation.theme.StatusNormal
import kotlinx.coroutines.launch

data class NavItem(
    val ruta: String,
    val etiqueta: String,
    val roles: List<Rol>? = null,
    val icono: @Composable () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavController,
    rol: Rol?,
    nombreUsuario: String? = null,
    chromeViewModel: ChromeViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute by navController.currentBackStackEntryAsState()
    val rutaActual = currentRoute?.destination?.route
    val notifications by chromeViewModel.notifications.collectAsState()
    val unread = notifications.count { !it.read }
    var quickActionsOpen by remember { mutableStateOf(false) }
    var notificationsOpen by remember { mutableStateOf(false) }

    val navItems =
        listOf(
            NavItem(Rutas.Dashboard.ruta, "Panel") {
                Icon(Icons.Filled.Dashboard, contentDescription = "Panel")
            },
            NavItem(Rutas.Historial.ruta, "Historial") {
                Icon(Icons.Filled.History, contentDescription = "Historial")
            },
            NavItem(Rutas.Analisis.ruta, "Análisis IA") {
                Icon(Icons.Filled.Analytics, contentDescription = "Análisis")
            },
            NavItem(Rutas.Nodos.ruta, "Nodos", listOf(Rol.ADMINISTRADOR, Rol.SOPORTE, Rol.GESTIONADOR)) {
                Icon(Icons.Filled.MonitorHeart, contentDescription = "Nodos")
            },
            NavItem(Rutas.Usuarios.ruta, "Usuarios", listOf(Rol.ADMINISTRADOR)) {
                Icon(Icons.Filled.WaterDrop, contentDescription = "Usuarios")
            },
            NavItem(Rutas.Logs.ruta, "Registros", listOf(Rol.ADMINISTRADOR, Rol.SOPORTE)) {
                Icon(Icons.Filled.History, contentDescription = "Registros")
            },
            NavItem(Rutas.Preferencias.ruta, "Preferencias") {
                Icon(Icons.Filled.Settings, contentDescription = "Preferencias")
            },
        )
    val visibleNavItems = navItems.filter { item -> item.roles == null || rol in item.roles }
    val bottomItems =
        visibleNavItems.filter {
            it.ruta in
                listOf(
                    Rutas.Dashboard.ruta,
                    Rutas.Historial.ruta,
                    Rutas.Analisis.ruta,
                    Rutas.Preferencias.ruta,
                )
        }
    val title = visibleNavItems.firstOrNull { it.ruta == rutaActual }?.etiqueta ?: "Panel"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavDrawerContent(
                rol = rol,
                rutaActual = rutaActual,
                onNavigate = { ruta ->
                    scope.launch { drawerState.close() }
                    navController.navigate(ruta) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    TopAppBar(
                        colors =
                            TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Abrir navegación")
                            }
                        },
                        title = {
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "SCCA",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = "v2.0",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        actions = {
                            Row(
                                modifier = Modifier.padding(end = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(onClick = { quickActionsOpen = true }) {
                                    Icon(Icons.Filled.Search, contentDescription = "Buscar acciones")
                                }
                                IconButton(onClick = { notificationsOpen = true }) {
                                    BadgedBox(
                                        badge = {
                                            if (unread > 0) {
                                                Badge { Text(if (unread > 9) "9+" else unread.toString()) }
                                            }
                                        },
                                    ) {
                                        Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
                                    }
                                }
                                SccaStatusDot(StatusNormal)
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier =
                                        Modifier
                                            .size(28.dp)
                                            .background(
                                                MaterialTheme.colorScheme.onBackground,
                                                MaterialTheme.shapes.small,
                                            ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = nombreUsuario?.firstOrNull()?.uppercase() ?: "U",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.background,
                                    )
                                }
                            }
                        },
                    )
                }
            },
            bottomBar = {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp,
                    ) {
                        bottomItems.forEach { item ->
                            NavigationBarItem(
                                selected = rutaActual == item.ruta,
                                onClick = {
                                    navController.navigate(item.ruta) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { item.icono() },
                                label = { Text(item.etiqueta) },
                            )
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                content()
            }
        }
        if (quickActionsOpen) {
            QuickActionsDialog(
                items = visibleNavItems,
                onDismiss = { quickActionsOpen = false },
                onNavigate = { ruta ->
                    quickActionsOpen = false
                    navController.navigate(ruta) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
        if (notificationsOpen) {
            NotificationsDialog(
                items = notifications,
                onDismiss = { notificationsOpen = false },
                onMarkAllRead = chromeViewModel::markAllRead,
                onDismissNotification = chromeViewModel::dismiss,
                onOpenNotification = { notification ->
                    chromeViewModel.markRead(notification.id)
                    notificationsOpen = false
                    notification.route?.let { ruta ->
                        navController.navigate(ruta) {
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun QuickActionsDialog(
    items: List<NavItem>,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buscar / acciones") },
        text = {
            Column {
                items.forEach { item ->
                    TextButton(onClick = { onNavigate(item.ruta) }) {
                        item.icono()
                        Spacer(Modifier.width(10.dp))
                        Text(item.etiqueta)
                    }
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
private fun NotificationsDialog(
    items: List<AppNotification>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onDismissNotification: (String) -> Unit,
    onOpenNotification: (AppNotification) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Notificaciones")
                if (items.isNotEmpty()) {
                    TextButton(onClick = onMarkAllRead) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Marcar todas")
                    }
                }
            }
        },
        text = {
            if (items.isEmpty()) {
                Text(
                    "Sin notificaciones todavía. Aparecerán aquí avisos de calidad, nodos desconectados y análisis listos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column {
                    items.take(8).forEach { item ->
                        NotificationRow(
                            item = item,
                            onOpen = { onOpenNotification(item) },
                            onDismiss = { onDismissNotification(item.id) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
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
private fun NotificationRow(
    item: AppNotification,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        SccaStatusDot(notificationColor(item.kind))
        Spacer(Modifier.width(10.dp))
        TextButton(
            onClick = onOpen,
            modifier = Modifier.weight(1f),
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (item.read) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                item.body?.let {
                    Text(
                        it.take(96),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = "Descartar")
        }
    }
}

@Composable
private fun notificationColor(kind: NotificationKind) =
    when (kind) {
        NotificationKind.INFO -> MaterialTheme.colorScheme.primary
        NotificationKind.WARNING -> com.proyecto.scca.presentation.theme.StatusWarning
        NotificationKind.CRITICAL -> com.proyecto.scca.presentation.theme.StatusCritical
        NotificationKind.SUCCESS -> StatusNormal
    }
