package com.proyecto.scca.presentation.feature.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyecto.scca.presentation.components.UiState

@Composable
fun CambiarPasswordScreen(
    onPasswordChanged: () -> Unit,
    viewModel: CambiarPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val nuevaPassword by viewModel.nuevaPassword.collectAsState()
    val confirmarPassword by viewModel.confirmarPassword.collectAsState()
    val errorNueva by viewModel.errorNueva.collectAsState()
    val errorConfirmar by viewModel.errorConfirmar.collectAsState()
    var showNueva by remember { mutableStateOf(false) }
    var showConfirmar by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) onPasswordChanged()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
        ) {
            Text(
                "Cambiar Contraseña",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "Por seguridad, debes establecer una nueva contraseña antes de continuar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AnimatedVisibility(uiState is UiState.Error) {
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    shape = MaterialTheme.shapes.small,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Text(
                        (uiState as? UiState.Error)?.message ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            OutlinedTextField(
                value = nuevaPassword,
                onValueChange = viewModel::onNuevaChange,
                label = { Text("Nueva contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showNueva = !showNueva }) {
                        Icon(
                            if (showNueva) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            null,
                        )
                    }
                },
                isError = errorNueva != null,
                supportingText = errorNueva?.let { { Text(it) } },
                visualTransformation = if (showNueva) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is UiState.Loading,
                shape = MaterialTheme.shapes.small,
            )

            OutlinedTextField(
                value = confirmarPassword,
                onValueChange = viewModel::onConfirmarChange,
                label = { Text("Confirmar contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showConfirmar = !showConfirmar }) {
                        Icon(
                            if (showConfirmar) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            null,
                        )
                    }
                },
                isError = errorConfirmar != null,
                supportingText = errorConfirmar?.let { { Text(it) } },
                visualTransformation = if (showConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is UiState.Loading,
                shape = MaterialTheme.shapes.small,
            )

            Button(
                onClick = viewModel::cambiarPassword,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                enabled = uiState !is UiState.Loading,
                shape = MaterialTheme.shapes.small,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background,
                    ),
            ) {
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Guardar nueva contraseña")
                }
            }
        }
    }
}
