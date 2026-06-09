package com.proyecto.scca.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun SparklineChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    minVal: Float? = null,
    maxVal: Float? = null,
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
        return
    }

    val chartMin = minVal ?: (data.minOrNull() ?: 0f)
    val chartMax = maxVal ?: (data.maxOrNull() ?: 0f)
    val range = if (chartMax - chartMin == 0f) 1f else chartMax - chartMin

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        val stepX = if (data.size > 1) width / (data.size - 1) else width
        
        val path = Path()
        var firstX = 0f
        var firstY = 0f
        
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = 1f - ((value - chartMin) / range)
            val y = normalizedY * height
            
            if (index == 0) {
                path.moveTo(x, y)
                firstX = x
                firstY = y
            } else {
                path.lineTo(x, y)
            }
        }
        
        // Draw the line
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        
        // Draw area underneath
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
                startY = 0f,
                endY = height
            ),
            style = Fill
        )
    }
}
