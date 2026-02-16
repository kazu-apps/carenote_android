package com.carenote.app.ui.screens.healthrecords.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.screens.healthrecords.GraphDataPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("M/d")

fun valueToY(
    value: Double,
    yMin: Double,
    yMax: Double,
    chartTop: Float,
    chartBottom: Float
): Float {
    val ratio = (value - yMin) / (yMax - yMin)
    return chartBottom - (ratio * (chartBottom - chartTop)).toFloat()
}

fun indexToX(
    index: Int,
    totalPoints: Int,
    chartLeft: Float,
    chartRight: Float
): Float {
    if (totalPoints <= 1) return (chartLeft + chartRight) / 2f
    val step = (chartRight - chartLeft) / (totalPoints - 1)
    return chartLeft + step * index
}

@Suppress("LongParameterList")
fun DrawScope.drawGridLines(
    yMin: Double,
    yMax: Double,
    step: Double,
    chartLeft: Float,
    chartRight: Float,
    chartTop: Float,
    chartBottom: Float,
    gridColor: Color
) {
    val strokeWidth = AppConfig.Graph.GRID_STROKE_WIDTH_DP.dp.toPx()
    var value = yMin
    while (value <= yMax) {
        val y = valueToY(value, yMin, yMax, chartTop, chartBottom)
        drawLine(
            color = gridColor,
            start = Offset(chartLeft, y),
            end = Offset(chartRight, y),
            strokeWidth = strokeWidth
        )
        value += step
    }
}

@Suppress("LongParameterList")
fun DrawScope.drawThresholdLine(
    value: Double,
    yMin: Double,
    yMax: Double,
    chartLeft: Float,
    chartRight: Float,
    chartTop: Float,
    chartBottom: Float,
    color: Color
) {
    val y = valueToY(value, yMin, yMax, chartTop, chartBottom)
    val dashOn = AppConfig.Graph.THRESHOLD_DASH_ON_DP.dp.toPx()
    val dashOff = AppConfig.Graph.THRESHOLD_DASH_OFF_DP.dp.toPx()
    val strokeWidth = AppConfig.Graph.THRESHOLD_STROKE_WIDTH_DP.dp.toPx()
    drawLine(
        color = color,
        start = Offset(chartLeft, y),
        end = Offset(chartRight, y),
        strokeWidth = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashOn, dashOff))
    )
}

@Suppress("LongParameterList")
fun DrawScope.drawYAxisLabels(
    textMeasurer: TextMeasurer,
    yMin: Double,
    yMax: Double,
    step: Double,
    chartTop: Float,
    chartBottom: Float,
    labelStyle: TextStyle,
    labelColor: Color
) {
    var value = yMin
    while (value <= yMax) {
        val y = valueToY(value, yMin, yMax, chartTop, chartBottom)
        val text = if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            "%.1f".format(value)
        }
        val measured = textMeasurer.measure(text, labelStyle)
        drawText(
            textLayoutResult = measured,
            color = labelColor,
            topLeft = Offset(0f, y - measured.size.height / 2f)
        )
        value += step
    }
}

@Suppress("LongParameterList")
fun DrawScope.drawXAxisLabels(
    textMeasurer: TextMeasurer,
    points: List<GraphDataPoint>,
    chartLeft: Float,
    chartRight: Float,
    chartBottom: Float,
    labelStyle: TextStyle,
    labelColor: Color
) {
    if (points.isEmpty()) return
    val maxLabels = AppConfig.Graph.X_AXIS_MAX_LABELS
    val indices = selectLabelIndices(points.size, maxLabels)
    for (i in indices) {
        val x = indexToX(i, points.size, chartLeft, chartRight)
        val text = points[i].date.format(dateFormatter)
        val measured = textMeasurer.measure(text, labelStyle)
        drawText(
            textLayoutResult = measured,
            color = labelColor,
            topLeft = Offset(
                x - measured.size.width / 2f,
                chartBottom + 4.dp.toPx()
            )
        )
    }
}

@Suppress("LongParameterList")
fun DrawScope.drawDataLine(
    points: List<GraphDataPoint>,
    yMin: Double,
    yMax: Double,
    chartLeft: Float,
    chartRight: Float,
    chartTop: Float,
    chartBottom: Float,
    lineColor: Color,
    pointColor: Color,
    abnormalColor: Color?,
    abnormalThreshold: Double?
) {
    if (points.size < 2) {
        drawSinglePoint(
            points, yMin, yMax, chartLeft, chartRight, chartTop,
            chartBottom, pointColor, abnormalColor, abnormalThreshold
        )
        return
    }
    val strokeWidth = AppConfig.Graph.LINE_STROKE_WIDTH_DP.dp.toPx()
    for (i in 0 until points.size - 1) {
        val x1 = indexToX(i, points.size, chartLeft, chartRight)
        val y1 = valueToY(points[i].value, yMin, yMax, chartTop, chartBottom)
        val x2 = indexToX(i + 1, points.size, chartLeft, chartRight)
        val y2 = valueToY(points[i + 1].value, yMin, yMax, chartTop, chartBottom)
        drawLine(lineColor, Offset(x1, y1), Offset(x2, y2), strokeWidth)
    }
    drawDataPoints(
        points, yMin, yMax, chartLeft, chartRight, chartTop,
        chartBottom, pointColor, abnormalColor, abnormalThreshold
    )
}

@Suppress("LongParameterList")
private fun DrawScope.drawSinglePoint(
    points: List<GraphDataPoint>,
    yMin: Double,
    yMax: Double,
    chartLeft: Float,
    chartRight: Float,
    chartTop: Float,
    chartBottom: Float,
    pointColor: Color,
    abnormalColor: Color?,
    abnormalThreshold: Double?
) {
    if (points.isEmpty()) return
    drawDataPoints(
        points, yMin, yMax, chartLeft, chartRight, chartTop,
        chartBottom, pointColor, abnormalColor, abnormalThreshold
    )
}

@Suppress("LongParameterList")
private fun DrawScope.drawDataPoints(
    points: List<GraphDataPoint>,
    yMin: Double,
    yMax: Double,
    chartLeft: Float,
    chartRight: Float,
    chartTop: Float,
    chartBottom: Float,
    pointColor: Color,
    abnormalColor: Color?,
    abnormalThreshold: Double?
) {
    val radius = AppConfig.Graph.POINT_RADIUS_DP.dp.toPx()
    val abnormalRadius = AppConfig.Graph.ABNORMAL_POINT_RADIUS_DP.dp.toPx()
    for (i in points.indices) {
        val x = indexToX(i, points.size, chartLeft, chartRight)
        val y = valueToY(points[i].value, yMin, yMax, chartTop, chartBottom)
        val isAbnormal = abnormalThreshold != null &&
            abnormalColor != null &&
            points[i].value >= abnormalThreshold
        val color = if (isAbnormal) abnormalColor!! else pointColor
        val r = if (isAbnormal) abnormalRadius else radius
        drawCircle(color = color, radius = r, center = Offset(x, y))
    }
}

private fun selectLabelIndices(total: Int, maxLabels: Int): List<Int> {
    if (total <= maxLabels) return (0 until total).toList()
    val step = (total - 1).toDouble() / (maxLabels - 1)
    return (0 until maxLabels).map { (it * step).toInt().coerceAtMost(total - 1) }
}
