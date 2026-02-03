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
    val chartHeight = AppConfig.Graph.CHART_HEIGHT_DP
    val yAxisWidth = AppConfig.Graph.Y_AXIS_LABEL_WIDTH_DP
    val xAxisHeight = AppConfig.Graph.X_AXIS_LABEL_HEIGHT_DP

    val colors = CareNoteColors.current
    val chartLabelColor = colors.chartLabelColor
    val chartLineColor = colors.chartLineColor
    val chartSecondaryLineColor = colors.chartSecondaryLineColor
    val accentError = colors.accentError

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember(chartLabelColor) {
        TextStyle(fontSize = AppConfig.Graph.AXIS_LABEL_FONT_SIZE_SP.sp, color = chartLabelColor)
    }
    val gridColor = chartLabelColor.copy(alpha = 0.2f)

    CareNoteCard(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(R.string.health_records_graph_bp_title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            BloodPressureLegend()

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight.dp)
            ) {
                val chartLeft = yAxisWidth.dp.toPx()
                val chartRight = size.width - 8.dp.toPx()
                val chartTop = 8.dp.toPx()
                val chartBottom = size.height - xAxisHeight.dp.toPx()

                drawGridLines(
                    yMin = yMin,
                    yMax = yMax,
                    step = AppConfig.Graph.BLOOD_PRESSURE_GRID_STEP,
                    chartLeft = chartLeft,
                    chartRight = chartRight,
                    chartTop = chartTop,
                    chartBottom = chartBottom,
                    gridColor = gridColor
                )

                drawThresholdLine(
                    value = thresholdHigh,
                    yMin = yMin,
                    yMax = yMax,
                    chartLeft = chartLeft,
                    chartRight = chartRight,
                    chartTop = chartTop,
                    chartBottom = chartBottom,
                    color = accentError.copy(alpha = 0.7f)
                )

                drawThresholdLine(
                    value = thresholdLow,
                    yMin = yMin,
                    yMax = yMax,
                    chartLeft = chartLeft,
                    chartRight = chartRight,
                    chartTop = chartTop,
                    chartBottom = chartBottom,
                    color = accentError.copy(alpha = 0.5f)
                )

                drawYAxisLabels(
                    textMeasurer = textMeasurer,
                    yMin = yMin,
                    yMax = yMax,
                    step = AppConfig.Graph.BLOOD_PRESSURE_GRID_STEP,
                    chartTop = chartTop,
                    chartBottom = chartBottom,
                    labelStyle = labelStyle,
                    labelColor = chartLabelColor
                )

                drawXAxisLabels(
                    textMeasurer = textMeasurer,
                    points = highPoints,
                    chartLeft = chartLeft,
                    chartRight = chartRight,
                    chartBottom = chartBottom,
                    labelStyle = labelStyle,
                    labelColor = chartLabelColor
                )

                drawDataLine(
                    points = highPoints,
                    yMin = yMin,
                    yMax = yMax,
                    chartLeft = chartLeft,
                    chartRight = chartRight,
                    chartTop = chartTop,
                    chartBottom = chartBottom,
                    lineColor = chartLineColor,
                    pointColor = chartLineColor,
                    abnormalColor = accentError,
                    abnormalThreshold = thresholdHigh
                )

                drawDataLine(
                    points = lowPoints,
                    yMin = yMin,
                    yMax = yMax,
                    chartLeft = chartLeft,
                    chartRight = chartRight,
                    chartTop = chartTop,
                    chartBottom = chartBottom,
                    lineColor = chartSecondaryLineColor,
                    pointColor = chartSecondaryLineColor,
                    abnormalColor = accentError,
                    abnormalThreshold = thresholdLow
                )
            }
        }
    }
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
