package com.alpriest.energystats.ui.graph

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
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
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsGraphView(viewModel: StatsGraphTabViewModel, modifier: Modifier = Modifier) {
    val displayMode = viewModel.displayModeStream.collectAsState().value

    Column(modifier = modifier.fillMaxWidth()) {
        ProvideChartStyle(rememberChartStyle(viewModel.chartColors)) {
            Chart(
                chart = columnChart(),
                chartModelProducer = viewModel.producer,
                chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                startAxis = startAxis(
                    maxLabelCount = 5,
                    valueFormatter = DecimalFormatAxisValueFormatter("0.0")
                ),
                bottomAxis = bottomAxis(
                    tickPosition = HorizontalAxis.TickPosition.Center(offset = 1, spacing = 3),
                    valueFormatter = CustomFormatAxisValueFormatter(displayMode)
                ),
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun StatsGraphViewPreview() {
    StatsGraphView(StatsGraphTabViewModel(FakeConfigManager(), DemoNetworking()))
}

class CustomFormatAxisValueFormatter<Position : AxisPosition>(val displayMode: StatsDisplayMode) :
    AxisValueFormatter<Position> {

    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        return when (displayMode) {
            is StatsDisplayMode.Day -> value.toInt().toString()
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
