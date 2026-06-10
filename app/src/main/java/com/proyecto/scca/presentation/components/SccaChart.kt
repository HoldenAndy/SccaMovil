package com.proyecto.scca.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalTextApi::class)
@Composable
fun SparklineChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    minVal: Float? = null,
    maxVal: Float? = null,
    dates: List<String> = emptyList(),
    axisTimeOnly: Boolean = false,
    unidad: String = "",
    label: String = "",
    refLine: Float? = null,
    refLabel: String? = null,
    isAlert: ((Float) -> Boolean)? = null,
    alertColor: Color = Color(0xFFD97706),
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
        return
    }

    val chartMin = minVal ?: (data.minOrNull() ?: 0f)
    val chartMax = maxVal ?: (data.maxOrNull() ?: 0f)
    val range = if (chartMax - chartMin == 0f) 1f else chartMax - chartMin

    var touchX by remember { mutableStateOf<Float?>(null) }
    val textMeasurer = rememberTextMeasurer()
    val tooltipBgColor = MaterialTheme.colorScheme.surfaceVariant
    val tooltipTextColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
    val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    val refLineColor = Color(0xFFD97706)

    Canvas(
        modifier =
            modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            touchX = offset.x
                            tryAwaitRelease()
                            touchX = null
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> touchX = offset.x },
                        onDragEnd = { touchX = null },
                        onDragCancel = { touchX = null },
                        onDrag = { change, _ -> touchX = change.position.x },
                    )
                },
    ) {
        val lp = 38.dp.toPx() // left padding for y-axis labels
        val bp = 18.dp.toPx() // bottom padding for x-axis labels
        val cw = size.width - lp
        val ch = size.height - bp

        fun valueToY(v: Float) = (1f - (v - chartMin) / range) * ch

        fun indexToX(i: Int) = lp + if (data.size > 1) i * (cw / (data.size - 1)) else cw / 2f

        val dashSoft = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
        val dashRef = PathEffect.dashPathEffect(floatArrayOf(3f, 5f), 0f)

        fun formatY(v: Float): String =
            if (v >= 100f) {
                String.format(Locale.US, "%.0f", v)
            } else if (v % 1f == 0f) {
                String.format(Locale.US, "%.0f", v)
            } else {
                String.format(Locale.US, "%.1f", v)
            }

        val axisStyle =
            TextStyle(
                fontSize = 8.sp,
                color = axisLabelColor,
                fontFamily = FontFamily.Monospace,
            )

        // 1. Grid lines at 25%, 50%, 75%
        for (i in 1..3) {
            val y = i * ch / 4
            drawLine(
                color = gridColor,
                start = Offset(lp, y),
                end = Offset(size.width, y),
                strokeWidth = 0.5.dp.toPx(),
                pathEffect = dashSoft,
            )
        }

        // 2. Y-axis labels (max at top, min at bottom)
        val yLabels = listOf(chartMax to 0f, chartMin to ch)
        yLabels.forEach { (v, gy) ->
            val text = formatY(v)
            val layout = textMeasurer.measure(text, axisStyle)
            val textY = (gy - layout.size.height / 2f).coerceIn(0f, ch - layout.size.height.toFloat())
            drawText(
                textMeasurer = textMeasurer,
                text = text,
                topLeft =
                    Offset(
                        (lp - layout.size.width - 3.dp.toPx()).coerceAtLeast(0f),
                        textY,
                    ),
                style = axisStyle,
            )
        }

        // 3. Reference line
        refLine?.let { ref ->
            if (ref > chartMin && ref < chartMax) {
                val refY = valueToY(ref)
                drawLine(
                    color = refLineColor.copy(alpha = 0.65f),
                    start = Offset(lp, refY),
                    end = Offset(size.width, refY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashRef,
                )
                refLabel?.let { lbl ->
                    val refStyle = TextStyle(fontSize = 7.sp, color = refLineColor, fontFamily = FontFamily.Monospace)
                    val lblLayout = textMeasurer.measure(lbl, refStyle)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = lbl,
                        topLeft =
                            Offset(
                                size.width - lblLayout.size.width - 2.dp.toPx(),
                                (refY - lblLayout.size.height - 1.dp.toPx()).coerceAtLeast(0f),
                            ),
                        style = refStyle,
                    )
                }
            }
        }

        // 4. Area fill + line
        val linePath = Path()
        data.forEachIndexed { index, value ->
            val x = indexToX(index)
            val y = valueToY(value)
            if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }

        val fillPath =
            Path().apply {
                addPath(linePath)
                lineTo(indexToX(data.size - 1), ch)
                lineTo(lp, ch)
                close()
            }
        drawPath(
            path = fillPath,
            brush =
                Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
                    startY = 0f,
                    endY = ch,
                ),
            style = Fill,
        )
        drawPath(
            path = linePath,
            color = color,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // 5. Alert dots
        isAlert?.let { alertFn ->
            data.forEachIndexed { index, value ->
                if (alertFn(value)) {
                    val x = indexToX(index)
                    val y = valueToY(value)
                    drawCircle(alertColor, 3.5.dp.toPx(), Offset(x, y))
                    drawCircle(surfaceColor, 1.5.dp.toPx(), Offset(x, y))
                }
            }
        }

        // 6. X-axis labels (4 evenly spaced, "dd/mm HH:MM")
        if (dates.isNotEmpty() && data.size > 1) {
            val labelCount = 4
            val indices =
                (0 until labelCount).map { i ->
                    (i * (data.size - 1) / (labelCount - 1)).coerceIn(0, data.size - 1)
                }.distinct()
            indices.forEach { idx ->
                val x = indexToX(idx)
                val raw = dates.getOrNull(idx) ?: return@forEach
                // "dd/mm/yyyy HH:MM" → "dd/mm HH:MM"
                val shortLabel =
                    when {
                        axisTimeOnly && raw.length >= 5 -> raw.takeLast(5)
                        raw.length >= 16 -> "${raw.take(5)} ${raw.takeLast(5)}"
                        else -> raw
                    }
                val layout = textMeasurer.measure(shortLabel, axisStyle)
                val textX = (x - layout.size.width / 2f).coerceIn(lp, size.width - layout.size.width.toFloat())
                drawText(
                    textMeasurer = textMeasurer,
                    text = shortLabel,
                    topLeft = Offset(textX, ch + 3.dp.toPx()),
                    style = axisStyle,
                )
            }
        }

        // 7. Touch tooltip
        touchX?.let { tx ->
            val adjustedTx = (tx - lp).coerceAtLeast(0f)
            val stepX = if (data.size > 1) cw / (data.size - 1) else cw
            val index = (adjustedTx / stepX).roundToInt().coerceIn(0, data.size - 1)
            val pointX = indexToX(index)
            val value = data[index]
            val pointY = valueToY(value)

            drawLine(
                color = color.copy(alpha = 0.4f),
                start = Offset(pointX, 0f),
                end = Offset(pointX, ch),
                strokeWidth = 1.dp.toPx(),
            )
            drawCircle(color, 4.dp.toPx(), Offset(pointX, pointY))
            drawCircle(surfaceColor, 2.dp.toPx(), Offset(pointX, pointY))

            if (dates.isNotEmpty() && index < dates.size) {
                val valStr = String.format(Locale.US, "%.2f", value)
                val tooltipText = "${dates[index]}\n$label: $valStr $unidad"
                val ttStyle = TextStyle(fontSize = 10.sp, color = tooltipTextColor, fontWeight = FontWeight.Medium)
                val layout = textMeasurer.measure(tooltipText, ttStyle)
                val ttW = layout.size.width + 16.dp.toPx()
                val ttH = layout.size.height + 12.dp.toPx()
                var ttX = pointX - ttW / 2f
                ttX = ttX.coerceIn(0f, size.width - ttW)
                var ttY = pointY - ttH - 8.dp.toPx()
                if (ttY < 0f) ttY = pointY + 8.dp.toPx()
                ttY = if (ch > ttH) ttY.coerceIn(0f, ch - ttH) else 0f
                drawRoundRect(tooltipBgColor, Offset(ttX, ttY), Size(ttW, ttH), CornerRadius(4.dp.toPx()))
                drawText(textMeasurer, tooltipText, Offset(ttX + 8.dp.toPx(), ttY + 6.dp.toPx()), ttStyle)
            }
        }
    }
}
