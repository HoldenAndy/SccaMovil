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
import androidx.compose.ui.graphics.drawscope.DrawScope
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

data class SparklineChartConfig(
    val minVal: Float? = null,
    val maxVal: Float? = null,
    val dates: List<String> = emptyList(),
    val axisTimeOnly: Boolean = false,
    val unidad: String = "",
    val label: String = "",
    val refLine: Float? = null,
    val refLabel: String? = null,
    val isAlert: ((Float) -> Boolean)? = null,
    val alertColor: Color = Color(0xFFD97706),
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun SparklineChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    config: SparklineChartConfig = SparklineChartConfig(),
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
        return
    }

    val chartMin = config.minVal ?: (data.minOrNull() ?: 0f)
    val chartMax = config.maxVal ?: (data.maxOrNull() ?: 0f)
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

        val axisStyle =
            TextStyle(
                fontSize = 8.sp,
                color = axisLabelColor,
                fontFamily = FontFamily.Monospace,
            )

        drawGridLines(ch, lp, gridColor)
        drawYAxisLabels(chartMax, chartMin, ch, lp, textMeasurer, axisStyle)

        config.refLine?.let { ref ->
            if (ref in chartMin..chartMax) {
                drawReferenceLine(
                    refLine = ref,
                    refLabel = config.refLabel,
                    valueToY = ::valueToY,
                    lp = lp,
                    refLineColor = refLineColor,
                    textMeasurer = textMeasurer,
                )
            }
        }

        drawChartArea(data, color, ch, lp, ::indexToX, ::valueToY)
        drawAlertDots(data, config.isAlert, config.alertColor, surfaceColor, ::indexToX, ::valueToY)

        if (config.dates.isNotEmpty() && data.size > 1) {
            drawXAxisLabels(data, config.dates, config.axisTimeOnly, ch, lp, textMeasurer, axisStyle, ::indexToX)
        }

        touchX?.let { tx ->
            drawTooltip(
                ctx =
                    TooltipContext(
                        tx = tx,
                        data = data,
                        dates = config.dates,
                        label = config.label,
                        unidad = config.unidad,
                        color = color,
                        surfaceColor = surfaceColor,
                        tooltipBgColor = tooltipBgColor,
                        tooltipTextColor = tooltipTextColor,
                        textMeasurer = textMeasurer,
                        lp = lp,
                        cw = cw,
                        ch = ch,
                    ),
                indexToX = ::indexToX,
                valueToY = ::valueToY,
            )
        }
    }
}

private fun formatY(v: Float): String =
    if (v >= 100f) {
        String.format(Locale.US, "%.0f", v)
    } else if (v % 1f == 0f) {
        String.format(Locale.US, "%.0f", v)
    } else {
        String.format(Locale.US, "%.1f", v)
    }

private fun DrawScope.drawGridLines(
    ch: Float,
    lp: Float,
    gridColor: Color,
) {
    val dashSoft = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
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
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawYAxisLabels(
    chartMax: Float,
    chartMin: Float,
    ch: Float,
    lp: Float,
    textMeasurer: TextMeasurer,
    axisStyle: TextStyle,
) {
    val yLabels = listOf(chartMax to 0f, chartMin to ch)
    yLabels.forEach { (v, gy) ->
        val text = formatY(v)
        val layout = textMeasurer.measure(text, axisStyle)
        val textY = (gy - layout.size.height / 2f).coerceIn(0f, ch - layout.size.height.toFloat())
        drawText(
            textMeasurer = textMeasurer,
            text = text,
            topLeft = Offset((lp - layout.size.width - 3.dp.toPx()).coerceAtLeast(0f), textY),
            style = axisStyle,
        )
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawReferenceLine(
    refLine: Float,
    refLabel: String?,
    valueToY: (Float) -> Float,
    lp: Float,
    refLineColor: Color,
    textMeasurer: TextMeasurer,
) {
    val refY = valueToY(refLine)
    val dashRef = PathEffect.dashPathEffect(floatArrayOf(3f, 5f), 0f)
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

private fun DrawScope.drawChartArea(
    data: List<Float>,
    color: Color,
    ch: Float,
    lp: Float,
    indexToX: (Int) -> Float,
    valueToY: (Float) -> Float,
) {
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
}

private fun DrawScope.drawAlertDots(
    data: List<Float>,
    isAlert: ((Float) -> Boolean)?,
    alertColor: Color,
    surfaceColor: Color,
    indexToX: (Int) -> Float,
    valueToY: (Float) -> Float,
) {
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
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawXAxisLabels(
    data: List<Float>,
    dates: List<String>,
    axisTimeOnly: Boolean,
    ch: Float,
    lp: Float,
    textMeasurer: TextMeasurer,
    axisStyle: TextStyle,
    indexToX: (Int) -> Float,
) {
    val labelCount = 4
    val indices =
        (0 until labelCount).map { i ->
            (i * (data.size - 1) / (labelCount - 1)).coerceIn(0, data.size - 1)
        }.distinct()

    indices.forEach { idx ->
        val x = indexToX(idx)
        val raw = dates.getOrNull(idx) ?: return@forEach
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

@OptIn(ExperimentalTextApi::class)
private data class TooltipContext(
    val tx: Float,
    val data: List<Float>,
    val dates: List<String>,
    val label: String,
    val unidad: String,
    val color: Color,
    val surfaceColor: Color,
    val tooltipBgColor: Color,
    val tooltipTextColor: Color,
    val textMeasurer: TextMeasurer,
    val lp: Float,
    val cw: Float,
    val ch: Float,
)

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawTooltip(
    ctx: TooltipContext,
    indexToX: (Int) -> Float,
    valueToY: (Float) -> Float,
) {
    val adjustedTx = (ctx.tx - ctx.lp).coerceAtLeast(0f)
    val stepX = if (ctx.data.size > 1) ctx.cw / (ctx.data.size - 1) else ctx.cw
    val index = (adjustedTx / stepX).roundToInt().coerceIn(0, ctx.data.size - 1)
    val pointX = indexToX(index)
    val value = ctx.data[index]
    val pointY = valueToY(value)

    drawLine(
        color = ctx.color.copy(alpha = 0.4f),
        start = Offset(pointX, 0f),
        end = Offset(pointX, ctx.ch),
        strokeWidth = 1.dp.toPx(),
    )
    drawCircle(ctx.color, 4.dp.toPx(), Offset(pointX, pointY))
    drawCircle(ctx.surfaceColor, 2.dp.toPx(), Offset(pointX, pointY))

    if (ctx.dates.isNotEmpty() && index < ctx.dates.size) {
        val valStr = String.format(Locale.US, "%.2f", value)
        val tooltipText = "${ctx.dates[index]}\n${ctx.label}: $valStr ${ctx.unidad}"
        val ttStyle = TextStyle(fontSize = 10.sp, color = ctx.tooltipTextColor, fontWeight = FontWeight.Medium)
        val layout = ctx.textMeasurer.measure(tooltipText, ttStyle)
        val ttW = layout.size.width + 16.dp.toPx()
        val ttH = layout.size.height + 12.dp.toPx()
        var ttX = pointX - ttW / 2f
        ttX = ttX.coerceIn(0f, size.width - ttW)
        var ttY = pointY - ttH - 8.dp.toPx()
        if (ttY < 0f) ttY = pointY + 8.dp.toPx()
        ttY = if (ctx.ch > ttH) ttY.coerceIn(0f, ctx.ch - ttH) else 0f
        drawRoundRect(ctx.tooltipBgColor, Offset(ttX, ttY), Size(ttW, ttH), CornerRadius(4.dp.toPx()))
        drawText(ctx.textMeasurer, tooltipText, Offset(ttX + 8.dp.toPx(), ttY + 6.dp.toPx()), ttStyle)
    }
}
