package com.proyecto.scca.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.proyecto.scca.domain.calidad.NivelCalidad
import com.proyecto.scca.presentation.theme.StatusCritical
import com.proyecto.scca.presentation.theme.StatusCriticalLight
import com.proyecto.scca.presentation.theme.StatusNormal
import com.proyecto.scca.presentation.theme.StatusNormalLight
import com.proyecto.scca.presentation.theme.StatusWarning
import com.proyecto.scca.presentation.theme.StatusWarningLight

@Composable
fun StatusChip(
    nivel: NivelCalidad,
    modifier: Modifier = Modifier,
) {
    val (color, background, label) =
        when (nivel) {
            NivelCalidad.NORMAL -> Triple(StatusNormal, StatusNormalLight, "Normal")
            NivelCalidad.WARNING -> Triple(StatusWarning, StatusWarningLight, "Alerta")
            NivelCalidad.CRITICAL -> Triple(StatusCritical, StatusCriticalLight, "Crítico")
        }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier =
            modifier
                .clip(RoundedCornerShape(3.dp))
                .background(background)
                .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}
