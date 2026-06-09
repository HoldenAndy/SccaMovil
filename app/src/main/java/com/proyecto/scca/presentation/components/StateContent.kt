package com.proyecto.scca.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>

    data class Success<T>(val data: T) : UiState<T>

    data object Empty : UiState<Nothing>

    data class Error(val message: String) : UiState<Nothing>
}

@Composable
fun <T> StateContent(
    state: UiState<T>,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        UiState.Loading -> {
            Column(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                repeat(5) {
                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(if (it == 0) 76.dp else 58.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    ) {}
                }
            }
        }
        is UiState.Success -> content(state.data)
        UiState.Empty -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    )
                    Text(
                        "Sin datos disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        is UiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                    if (onRetry != null) {
                        Button(onClick = onRetry) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}
