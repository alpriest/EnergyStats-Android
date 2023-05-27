package com.alpriest.energystats.ui.statsgraph

import androidx.compose.animation.core.SnapSpec
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.statsgraph.StatsDisplayMode.Day
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsGraphView(viewModel: StatsGraphTabViewModel, modifier: Modifier = Modifier) {
    val displayMode = viewModel.displayModeStream.collectAsState().value
    val chartColors = viewModel.chartColorsStream.collectAsState().value
    val maxY = viewModel.maxYStream.collectAsState().value

    Column(modifier = modifier.fillMaxWidth()) {
        ProvideChartStyle(chartStyle(chartColors)) {
            Chart(
                chart = columnChart(
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
                    tickPosition = HorizontalAxis.TickPosition.Center(offset = 0, spacing = 2),
                    valueFormatter = CustomFormatAxisValueFormatter(displayMode)
                ),
                diffAnimationSpec = SnapSpec()
            )
        }
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(when (displayMode) {
                is Day -> stringResource(R.string.day)
                is StatsDisplayMode.Month -> stringResource(R.string.month)
                is StatsDisplayMode.Year -> stringResource(R.string.year)
            })
        }
    }
}

@Composable
@Preview(showBackground = true)
fun StatsGraphViewPreview() {
    StatsGraphView(StatsGraphTabViewModel(FakeConfigManager(), DemoNetworking()))
}

class CustomFormatAxisValueFormatter<Position : AxisPosition>(private val displayMode: StatsDisplayMode) :
    AxisValueFormatter<Position> {

    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        return when (displayMode) {
            is Day -> value.toInt().toString()
            is StatsDisplayMode.Month -> value.toInt().toString()
            is StatsDisplayMode.Year -> {
                val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, value.toInt())
                return monthFormat.format(calendar.time)
            }
        }
    }
}
