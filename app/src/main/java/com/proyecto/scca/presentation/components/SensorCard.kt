package com.proyecto.scca.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.scca.domain.calidad.EvaluacionParametro
import com.proyecto.scca.domain.calidad.NivelCalidad
import com.proyecto.scca.presentation.theme.StatusCritical
import com.proyecto.scca.presentation.theme.StatusNormal
import com.proyecto.scca.presentation.theme.StatusWarning
import java.util.Locale

@Composable
fun SensorCard(
    titulo: String,
    evaluacion: EvaluacionParametro,
    unidad: String,
    colorSensor: Color,
    history: List<Float> = emptyList(),
    historyDates: List<String> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = evaluacion.porcentaje.toFloat(),
        animationSpec = tween(800),
        label = "sensor_progress",
    )

    val nivelColor =
        when (evaluacion.nivel) {
            NivelCalidad.NORMAL -> StatusNormal
            NivelCalidad.WARNING -> StatusWarning
            NivelCalidad.CRITICAL -> StatusCritical
        }

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(com.proyecto.scca.presentation.theme.LocalSccaDensity.current.cardPadding),
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(28.dp)
                                .height(28.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(colorSensor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = titulo.take(1),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorSensor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    StatusChip(nivel = evaluacion.nivel)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text =
                            if (unidad == "ppm") {
                                String.format(Locale.US, "%.0f", evaluacion.valor)
                            } else {
                                String.format(Locale.US, "%.2f", evaluacion.valor)
                            },
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = unidad,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                SparklineChart(
                    data = history,
                    color = colorSensor,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    config = SparklineChartConfig(
                        dates = historyDates,
                        axisTimeOnly = true,
                        unidad = unidad,
                        label = titulo,
                    ),
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(MaterialTheme.shapes.small),
                    color = nivelColor,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                )
            }
        }
    }
}
