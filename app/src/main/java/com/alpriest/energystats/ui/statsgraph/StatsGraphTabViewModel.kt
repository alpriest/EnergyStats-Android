package com.alpriest.energystats.ui.statsgraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ValueUsage
import com.alpriest.energystats.models.parse
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.Math.abs
import java.lang.Math.max
import java.time.LocalDate

data class StatsGraphValue(val graphPoint: Int, val value: Double, val type: ReportVariable)

class StatsGraphTabViewModel(
    val configManager: ConfigManaging,
    val networking: Networking
) : ViewModel() {
    var maxYStream = MutableStateFlow(0f)
    var chartColorsStream = MutableStateFlow(listOf<Color>())
    val producer: ChartEntryModelProducer = ChartEntryModelProducer()
    val displayModeStream = MutableStateFlow<StatsDisplayMode>(StatsDisplayMode.Day(LocalDate.now()))
    val graphVariablesStream = MutableStateFlow(listOf(
        ReportVariable.Generation,
        ReportVariable.FeedIn,
        ReportVariable.GridConsumption,
        ReportVariable.ChargeEnergyToTal,
        ReportVariable.DischargeEnergyToTal
    ).map {
        StatsGraphVariable(it, true)
    })
    var rawData: List<StatsGraphValue> = listOf()
    var totalsStream: MutableStateFlow<MutableMap<ReportVariable, Double>> = MutableStateFlow(mutableMapOf())

    suspend fun load() {
        val device = configManager.currentDevice.value ?: return
        val graphVariables = graphVariablesStream.value

        val displayMode = displayModeStream.value

        val queryDate = makeQueryDate(displayMode)
        val reportType = makeReportType(displayMode)

        val reportData = networking.fetchReport(
            device.deviceID,
            variables = graphVariables.map { it.type }.toTypedArray(),
            queryDate = queryDate,
            reportType = reportType
        )

        var maxY = 0f
        val rawTotals: MutableMap<ReportVariable, Double> = mutableMapOf()

        rawData = reportData.flatMap { reportResponse ->
            val reportVariable = ReportVariable.parse(reportResponse.variable)

            rawTotals[reportVariable] = reportResponse.data.map { abs(it.value) }.sum()

            return@flatMap reportResponse.data.map { dataPoint ->
                val graphPoint: Int = when (displayMode) {
                    is StatsDisplayMode.Day -> {
                        dataPoint.index - 1
                    }
                    is StatsDisplayMode.Month -> {
                        dataPoint.index
                    }
                    is StatsDisplayMode.Year -> {
                        dataPoint.index
                    }
                }

                maxY = max(maxY, dataPoint.value.toFloat() + 0.5f)

                return@map StatsGraphValue(
                    graphPoint = graphPoint,
                    value = dataPoint.value,
                    type = reportVariable
                )
            }
        }

        totalsStream.value = rawTotals
        maxYStream.value = maxY
        refresh()
    }

    private fun refresh() {
        val hiddenVariables = graphVariablesStream.value.filter { !it.enabled }.map { it.type }
        val grouped = rawData.filter { !hiddenVariables.contains(it.type) }.groupBy { it.type }
        val entries = grouped
            .map { group ->
                group.value.map {
                    return@map FloatEntry(x = it.graphPoint.toFloat(), y = it.value.toFloat())
                }.toList()
            }.toList()

        chartColorsStream.value = grouped
            .map { it.key.colour() }

        producer.setEntries(entries)
    }

    private fun makeQueryDate(displayMode: StatsDisplayMode): QueryDate {
        return when (displayMode) {
            is StatsDisplayMode.Day -> {
                val date = displayMode.date
                QueryDate(
                    year = date.year,
                    month = date.monthValue,
                    day = date.dayOfMonth
                )
            }
            is StatsDisplayMode.Month -> {
                QueryDate(year = displayMode.year, month = displayMode.month + 1, day = null)
            }
            is StatsDisplayMode.Year -> {
                QueryDate(year = displayMode.year, month = null, day = null)
            }
        }
    }

    private fun makeReportType(displayMode: StatsDisplayMode): ReportType {
        return when (displayMode) {
            is StatsDisplayMode.Day -> ReportType.day
            is StatsDisplayMode.Month -> ReportType.month
            is StatsDisplayMode.Year -> ReportType.year
        }
    }

    fun toggleVisibility(statsGraphVariable: StatsGraphVariable) {
        val updated = graphVariablesStream.value.map {
            if (it.type == statsGraphVariable.type) {
                return@map StatsGraphVariable(it.type, !it.enabled)
            } else {
                return@map it
            }
        }

        if (updated.count { it.enabled } == 0) {
            return
        }

        graphVariablesStream.value = updated
        refresh()
    }

    fun total(variable: StatsGraphVariable): Double? {
        return totalsStream.value[variable.type]
    }
}

interface GraphVariable {
    val enabled: Boolean
    val colour: Color
}

@Composable
fun title(usage: ValueUsage): String {
    return when (usage) {
        ValueUsage.SNAPSHOT -> stringResource(R.string.power)
        ValueUsage.TOTAL -> stringResource(R.string.energy)
    }
}

enum class ReportType {
    day,
    month,
    year,
}
