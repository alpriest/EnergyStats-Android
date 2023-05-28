package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.animation.core.SnapSpec
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.statsgraph.chartStyle
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider

@Composable
fun ParameterGraphView(viewModel: ParametersGraphTabViewModel, modifier: Modifier = Modifier) {
    val chartColors = viewModel.chartColorsStream.collectAsState().value
    val maxY = viewModel.maxYStream.collectAsState().value

    Column(modifier = modifier.fillMaxWidth()) {
        ProvideChartStyle(chartStyle(chartColors)) {
            Chart(
                chart = lineChart(
                    axisValuesOverrider = AxisValuesOverrider.fixed(minY = 0f, maxY = maxY)
                ),
                chartModelProducer = viewModel.producer,
                chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                startAxis = startAxis(
                    maxLabelCount = 5,
                    valueFormatter = DecimalFormatAxisValueFormatter("0.0")
                ),
                bottomAxis = bottomAxis(
                    label = axisLabelComponent(horizontalPadding = 2.dp),
                    tickPosition = HorizontalAxis.TickPosition.Center(offset = 0, spacing = 20),
                ),
                diffAnimationSpec = SnapSpec()
            )
        }
    }
}