package com.carenote.app.ui.screens.healthrecords.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
fun TemperatureChart(
    points: List<GraphDataPoint>,
    modifier: Modifier = Modifier
) {
    val yMin = AppConfig.Graph.TEMPERATURE_Y_MIN
    val yMax = AppConfig.Graph.TEMPERATURE_Y_MAX
    val threshold = AppConfig.HealthThresholds.TEMPERATURE_HIGH

    val colors = CareNoteColors.current
    val chartLabelColor = colors.chartLabelColor
    val chartLineColor = colors.chartLineColor
    val accentError = colors.accentError

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember(chartLabelColor) {
        TextStyle(
            fontSize = AppConfig.Graph.AXIS_LABEL_FONT_SIZE_SP.sp,
            color = chartLabelColor
        )
    }
    val gridColor = chartLabelColor.copy(alpha = 0.2f)

    val graphDescription = temperatureDescription(points, threshold)

    CareNoteCard(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(R.string.health_records_graph_temperature_title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TemperatureCanvas(
                points = points,
                yMin = yMin,
                yMax = yMax,
                threshold = threshold,
                graphDescription = graphDescription,
                textMeasurer = textMeasurer,
                labelStyle = labelStyle,
                gridColor = gridColor,
                chartLabelColor = chartLabelColor,
                chartLineColor = chartLineColor,
                accentError = accentError
            )
        }
    }
}

@Composable
private fun temperatureDescription(
    points: List<GraphDataPoint>,
    threshold: Double
): String {
    val abnormalCount = remember(points) {
        points.count { it.value >= threshold }
    }
    return if (abnormalCount > 0) {
        stringResource(
            R.string.a11y_graph_temperature_summary,
            points.size,
            points.minOf { it.value },
            points.maxOf { it.value },
            abnormalCount
        )
    } else {
        stringResource(
            R.string.a11y_graph_temperature_summary_no_abnormal,
            points.size,
            points.minOf { it.value },
            points.maxOf { it.value }
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun TemperatureCanvas(
    points: List<GraphDataPoint>,
    yMin: Double,
    yMax: Double,
    threshold: Double,
    graphDescription: String,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    labelStyle: TextStyle,
    gridColor: androidx.compose.ui.graphics.Color,
    chartLabelColor: androidx.compose.ui.graphics.Color,
    chartLineColor: androidx.compose.ui.graphics.Color,
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

        drawGridLines(
            yMin, yMax, AppConfig.Graph.TEMPERATURE_GRID_STEP,
            chartLeft, chartRight, chartTop, chartBottom, gridColor
        )
        drawThresholdLine(
            threshold, yMin, yMax,
            chartLeft, chartRight, chartTop, chartBottom,
            accentError.copy(alpha = 0.7f)
        )
        drawYAxisLabels(
            textMeasurer, yMin, yMax,
            AppConfig.Graph.TEMPERATURE_GRID_STEP,
            chartTop, chartBottom, labelStyle, chartLabelColor
        )
        drawXAxisLabels(
            textMeasurer, points,
            chartLeft, chartRight, chartBottom,
            labelStyle, chartLabelColor
        )
        drawDataLine(
            points, yMin, yMax,
            chartLeft, chartRight, chartTop, chartBottom,
            chartLineColor, chartLineColor,
            accentError, threshold
        )
    }
}
