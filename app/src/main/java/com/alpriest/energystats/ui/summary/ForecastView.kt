package com.alpriest.energystats.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.Green
import com.alpriest.energystats.ui.theme.Red
import com.alpriest.energystats.ui.theme.TintColor
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import kotlinx.coroutines.flow.MutableStateFlow

object ForecastDefaults {
    val predictionColor: Color = TintColor
    val color90: Color = Red.copy(alpha = 0.5f)
    val color10: Color = Green.copy(alpha = 0.5f)
}

@Composable
fun ForecastView(
    model: List<List<DateFloatEntry>>,
    todayTotal: Double,
    name: String?,
    title: String,
    themeStream: MutableStateFlow<AppTheme>
) {
    val theme = themeStream.collectAsState().value

    val chartColors = listOf(
        ForecastDefaults.color90,
        ForecastDefaults.color10,
        ForecastDefaults.predictionColor
    )

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(model) {
        modelProducer.runTransaction {
            lineSeries {
                model.forEach { seriesEntries ->
                    series(
                        x = seriesEntries.map { it.x.toDouble() },
                        y = seriesEntries.map { it.y.toDouble() }
                    )
                }
            }
        }
    }

    val yAxisValueFormatter = remember(theme.decimalPlaces) {
        CartesianValueFormatter { _, value, _ ->
            "%.${theme.decimalPlaces}f kW".format(value)
        }
    }

    val lineProvider = LineCartesianLayer.LineProvider.series(
        // 0: high (90%) – line only
        LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(fill(chartColors[0])),
            areaFill = null
        ),
        // 1: low (10%) – line only
        LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(fill(chartColors[1])),
            areaFill = null
        ),
        // 2: prediction – line only
        LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(fill(chartColors[2])),
            areaFill = null
        )
    )

    val lineLayer = rememberLineCartesianLayer(
        lineProvider = lineProvider,
        rangeProvider = CartesianLayerRangeProvider.fixed(minX = 0.0, maxX = 48.0)
    )

    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                buildAnnotatedString {
                    name?.let {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
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

        CartesianChartHost(
            chart = rememberCartesianChart(
                lineLayer,
                endAxis = VerticalAxis.rememberEnd(
                    itemPlacer = VerticalAxis.ItemPlacer.count(count = { 5 }),
                    valueFormatter = yAxisValueFormatter
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                        spacing = { 6 },
                        addExtremeLabelPadding = true
                    ),
                    valueFormatter = { _, x, _ ->
                        val hour = x.toInt() / 2
                        "%d:%02d".format(hour, 0)
                    },
                    guideline = null
                )
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxWidth(),
            scrollState = rememberVicoScrollState(scrollEnabled = false)
        )
    }
}
