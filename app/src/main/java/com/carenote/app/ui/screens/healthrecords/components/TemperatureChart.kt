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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.components.CareNoteCard
import com.carenote.app.ui.screens.healthrecords.GraphDataPoint
import com.carenote.app.ui.theme.AccentError
import com.carenote.app.ui.theme.PrimaryGreen
import com.carenote.app.ui.theme.TextSecondary

@Composable
fun TemperatureChart(
    points: List<GraphDataPoint>,
    modifier: Modifier = Modifier
) {
    val yMin = AppConfig.Graph.TEMPERATURE_Y_MIN
    val yMax = AppConfig.Graph.TEMPERATURE_Y_MAX
    val threshold = AppConfig.HealthThresholds.TEMPERATURE_HIGH
    val chartHeight = AppConfig.Graph.CHART_HEIGHT_DP
    val yAxisWidth = AppConfig.Graph.Y_AXIS_LABEL_WIDTH_DP
    val xAxisHeight = AppConfig.Graph.X_AXIS_LABEL_HEIGHT_DP

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember {
        TextStyle(fontSize = 10.sp, color = TextSecondary)
    }
    val gridColor = TextSecondary.copy(alpha = 0.2f)

    CareNoteCard(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(R.string.health_records_graph_temperature_title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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
                    step = 1.0,
                    chartLeft = chartLeft,
                    chartRight = chartRight,
                    chartTop = chartTop,
                    chartBottom = chartBottom,
                    gridColor = gridColor
                )

                drawThresholdLine(
                    value = threshold,
                    yMin = yMin,
                    yMax = yMax,
                    chartLeft = chartLeft,
                    chartRight = chartRight,
                    chartTop = chartTop,
                    chartBottom = chartBottom,
                    color = AccentError.copy(alpha = 0.7f)
                )

                drawYAxisLabels(
                    textMeasurer = textMeasurer,
                    yMin = yMin,
                    yMax = yMax,
                    step = 1.0,
                    chartTop = chartTop,
                    chartBottom = chartBottom,
                    labelStyle = labelStyle,
                    labelColor = TextSecondary
                )

                drawXAxisLabels(
                    textMeasurer = textMeasurer,
                    points = points,
                    chartLeft = chartLeft,
                    chartRight = chartRight,
                    chartBottom = chartBottom,
                    labelStyle = labelStyle,
                    labelColor = TextSecondary
                )

                drawDataLine(
                    points = points,
                    yMin = yMin,
                    yMax = yMax,
                    chartLeft = chartLeft,
                    chartRight = chartRight,
                    chartTop = chartTop,
                    chartBottom = chartBottom,
                    lineColor = PrimaryGreen,
                    pointColor = PrimaryGreen,
                    abnormalColor = AccentError,
                    abnormalThreshold = threshold
                )
            }
        }
    }
}
