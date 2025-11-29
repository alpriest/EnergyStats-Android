package com.alpriest.energystats.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.ui.statsgraph.chartStyle
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.Green
import com.alpriest.energystats.ui.theme.Red
import com.alpriest.energystats.ui.theme.TintColor
import com.patrykandpatrick.vico1.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico1.compose.axis.vertical.rememberEndAxis
import com.patrykandpatrick.vico1.compose.chart.Chart
import com.patrykandpatrick.vico1.compose.chart.line.lineChart
import com.patrykandpatrick.vico1.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico1.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico1.core.axis.AxisPosition
import com.patrykandpatrick.vico1.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico1.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico1.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico1.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico1.core.chart.values.ChartValues
import com.patrykandpatrick.vico1.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow

object ForecastDefaults {
    val predictionColor: Color = TintColor
    val color90: Color = Red.copy(alpha = 0.5f)
    val color10: Color = Green.copy(alpha = 0.5f)
}

@Composable
fun ForecastViewVico1(
    model: List<List<DateFloatEntry>>,
    todayTotal: Double,
    name: String?,
    title: String,
    themeStream: MutableStateFlow<AppTheme>
) {
    val theme = themeStream.collectAsState().value
    val chartColors = listOf(ForecastDefaults.color90, ForecastDefaults.color10, ForecastDefaults.predictionColor)
    val chartEntryModelProducer = ChartEntryModelProducer(model)

    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Text(
                buildAnnotatedString {
                    name?.let {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Companion.Bold)) {
                            append(it)
                            append(" ")
                        }
                    }

                    withStyle(style = SpanStyle()) {
                        append(title)
                        append(" ")
                    }

                    withStyle(style = SpanStyle()) {
                        append(todayTotal.energy(theme.displayUnit, theme.decimalPlaces))
                    }
                },
                style = TextStyle(color = MaterialTheme.colorScheme.onSecondary)
            )
        }
        ProvideChartStyle(chartStyle(chartColors, themeStream)) {
            Chart(
                chart = lineChart(
                    axisValuesOverrider = AxisValuesOverrider.fixed(minY = 0f, maxY = 48f)
                ),
                chartModelProducer = chartEntryModelProducer,
                endAxis = rememberEndAxis(
                    itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 5),
                    valueFormatter = DecimalFormatAxisValueFormatter("0.${"0".repeat(theme.decimalPlaces)} kW")
                ),
                bottomAxis = rememberBottomAxis(
                    itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 6, addExtremeLabelPadding = true),
                    valueFormatter = SolarGraphFormatAxisValueFormatter(),
                    guideline = null,
                ),
                horizontalLayout = HorizontalLayout.FullWidth(),
                isZoomEnabled = false
            )
        }
    }
}

class SolarGraphFormatAxisValueFormatter<Position : AxisPosition> : AxisValueFormatter<Position> {
    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        return String.format("%d:%02d", (value.toInt() / 2), 0)
    }
}