package com.carenote.app.ui.screens.healthrecords.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.components.CareNoteCard
import com.carenote.app.ui.screens.healthrecords.GraphDataPoint
import com.carenote.app.ui.theme.CareNoteColors

@Composable
fun BloodPressureChart(
    highPoints: List<GraphDataPoint>,
    lowPoints: List<GraphDataPoint>,
    modifier: Modifier = Modifier
) {
    val yMin = AppConfig.Graph.BLOOD_PRESSURE_Y_MIN.toDouble()
    val yMax = AppConfig.Graph.BLOOD_PRESSURE_Y_MAX.toDouble()
    val thresholdHigh = AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_UPPER.toDouble()
    val thresholdLow = AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_LOWER.toDouble()

    val colors = CareNoteColors.current
    val chartLabelColor = colors.chartLabelColor
    val chartLineColor = colors.chartLineColor
    val chartSecondaryLineColor = colors.chartSecondaryLineColor
    val accentError = colors.accentError

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember(chartLabelColor) {
        TextStyle(
            fontSize = AppConfig.Graph.AXIS_LABEL_FONT_SIZE_SP.sp,
            color = chartLabelColor
        )
    }
    val gridColor = chartLabelColor.copy(alpha = 0.2f)

    val graphDescription = bloodPressureDescription(
        highPoints, lowPoints, thresholdHigh, thresholdLow
    )

    CareNoteCard(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(R.string.health_records_graph_bp_title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            BloodPressureLegend()
            BloodPressureCanvas(
                highPoints = highPoints,
                lowPoints = lowPoints,
                yMin = yMin,
                yMax = yMax,
                thresholdHigh = thresholdHigh,
                thresholdLow = thresholdLow,
                graphDescription = graphDescription,
                textMeasurer = textMeasurer,
                labelStyle = labelStyle,
                gridColor = gridColor,
                chartLabelColor = chartLabelColor,
                chartLineColor = chartLineColor,
                chartSecondaryLineColor = chartSecondaryLineColor,
                accentError = accentError
            )
        }
    }
}

@Composable
private fun bloodPressureDescription(
    highPoints: List<GraphDataPoint>,
    lowPoints: List<GraphDataPoint>,
    thresholdHigh: Double,
    thresholdLow: Double
): String {
    val abnormalCount = remember(highPoints, lowPoints) {
        highPoints.count { it.value >= thresholdHigh } +
            lowPoints.count { it.value >= thresholdLow }
    }
    return if (abnormalCount > 0) {
        stringResource(
            R.string.a11y_graph_bp_summary,
            highPoints.size,
            highPoints.minOf { it.value }.toInt(),
            highPoints.maxOf { it.value }.toInt(),
            lowPoints.minOf { it.value }.toInt(),
            lowPoints.maxOf { it.value }.toInt(),
            abnormalCount
        )
    } else {
        stringResource(
            R.string.a11y_graph_bp_summary_no_abnormal,
            highPoints.size,
            highPoints.minOf { it.value }.toInt(),
            highPoints.maxOf { it.value }.toInt(),
            lowPoints.minOf { it.value }.toInt(),
            lowPoints.maxOf { it.value }.toInt()
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun BloodPressureCanvas(
    highPoints: List<GraphDataPoint>,
    lowPoints: List<GraphDataPoint>,
    yMin: Double,
    yMax: Double,
    thresholdHigh: Double,
    thresholdLow: Double,
    graphDescription: String,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    labelStyle: TextStyle,
    gridColor: androidx.compose.ui.graphics.Color,
    chartLabelColor: androidx.compose.ui.graphics.Color,
    chartLineColor: androidx.compose.ui.graphics.Color,
    chartSecondaryLineColor: androidx.compose.ui.graphics.Color,
    accentError: androidx.compose.ui.graphics.Color
) {
    val chartHeight = AppConfig.Graph.CHART_HEIGHT_DP
    val yAxisWidth = AppConfig.Graph.Y_AXIS_LABEL_WIDTH_DP
    val xAxisHeight = AppConfig.Graph.X_AXIS_LABEL_HEIGHT_DP

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight.dp)
            .semantics { contentDescription = graphDescription }
    ) {
        val chartLeft = yAxisWidth.dp.toPx()
        val chartRight = size.width - 8.dp.toPx()
        val chartTop = 8.dp.toPx()
        val chartBottom = size.height - xAxisHeight.dp.toPx()

        drawBpGridAndThresholds(
            yMin, yMax, thresholdHigh, thresholdLow,
            chartLeft, chartRight, chartTop, chartBottom,
            gridColor, accentError
        )
        drawBpAxisLabels(
            textMeasurer, highPoints, yMin, yMax,
            chartLeft, chartRight, chartTop, chartBottom,
            labelStyle, chartLabelColor
        )
        drawBpDataLines(
            highPoints, lowPoints, yMin, yMax,
            chartLeft, chartRight, chartTop, chartBottom,
            chartLineColor, chartSecondaryLineColor,
            accentError, thresholdHigh, thresholdLow
        )
    }
}

@Suppress("LongParameterList")
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBpGridAndThresholds(
    yMin: Double, yMax: Double,
    thresholdHigh: Double, thresholdLow: Double,
    chartLeft: Float, chartRight: Float,
    chartTop: Float, chartBottom: Float,
    gridColor: androidx.compose.ui.graphics.Color,
    accentError: androidx.compose.ui.graphics.Color
) {
    drawGridLines(
        yMin, yMax, AppConfig.Graph.BLOOD_PRESSURE_GRID_STEP,
        chartLeft, chartRight, chartTop, chartBottom, gridColor
    )
    drawThresholdLine(
        thresholdHigh, yMin, yMax,
        chartLeft, chartRight, chartTop, chartBottom,
        accentError.copy(alpha = 0.7f)
    )
    drawThresholdLine(
        thresholdLow, yMin, yMax,
        chartLeft, chartRight, chartTop, chartBottom,
        accentError.copy(alpha = 0.5f)
    )
}

@Suppress("LongParameterList")
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBpAxisLabels(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    highPoints: List<GraphDataPoint>,
    yMin: Double, yMax: Double,
    chartLeft: Float, chartRight: Float,
    chartTop: Float, chartBottom: Float,
    labelStyle: TextStyle,
    chartLabelColor: androidx.compose.ui.graphics.Color
) {
    drawYAxisLabels(
        textMeasurer, yMin, yMax,
        AppConfig.Graph.BLOOD_PRESSURE_GRID_STEP,
        chartTop, chartBottom, labelStyle, chartLabelColor
    )
    drawXAxisLabels(
        textMeasurer, highPoints,
        chartLeft, chartRight, chartBottom,
        labelStyle, chartLabelColor
    )
}

@Suppress("LongParameterList")
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBpDataLines(
    highPoints: List<GraphDataPoint>,
    lowPoints: List<GraphDataPoint>,
    yMin: Double, yMax: Double,
    chartLeft: Float, chartRight: Float,
    chartTop: Float, chartBottom: Float,
    chartLineColor: androidx.compose.ui.graphics.Color,
    chartSecondaryLineColor: androidx.compose.ui.graphics.Color,
    accentError: androidx.compose.ui.graphics.Color,
    thresholdHigh: Double, thresholdLow: Double
) {
    drawDataLine(
        highPoints, yMin, yMax,
        chartLeft, chartRight, chartTop, chartBottom,
        chartLineColor, chartLineColor,
        accentError, thresholdHigh
    )
    drawDataLine(
        lowPoints, yMin, yMax,
        chartLeft, chartRight, chartTop, chartBottom,
        chartSecondaryLineColor, chartSecondaryLineColor,
        accentError, thresholdLow
    )
}

@Composable
private fun BloodPressureLegend() {
    val colors = CareNoteColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(
            color = colors.chartLineColor,
            label = stringResource(R.string.health_records_graph_bp_systolic)
        )
        LegendItem(
            color = colors.chartSecondaryLineColor,
            label = stringResource(R.string.health_records_graph_bp_diastolic)
        )
        LegendItem(
            color = colors.accentError.copy(alpha = 0.7f),
            label = stringResource(R.string.health_records_graph_threshold),
            isDashed = true
        )
    }
}

@Composable
private fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    isDashed: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            if (isDashed) {
                val dashWidth = size.width / 3
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                    end = androidx.compose.ui.geometry.Offset(dashWidth, size.height / 2),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(
                        dashWidth * 2, size.height / 2
                    ),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx()
                )
            } else {
                drawCircle(color = color, style = Fill)
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CareNoteColors.current.chartLabelColor
        )
    }
}
