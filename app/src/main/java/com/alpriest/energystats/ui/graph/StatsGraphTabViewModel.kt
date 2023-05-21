package com.alpriest.energystats.ui.graph

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.parse
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.Math.abs
import java.time.LocalDate

data class StatsGraphValue(val graphPoint: Int, val value: Double, val type: ReportVariable)

class StatsGraphTabViewModel(
    val configManager: ConfigManaging,
    val networking: Networking
) : ViewModel() {
    var chartColors = listOf<Color>()
    val producer: ChartEntryModelProducer = ChartEntryModelProducer()
    val displayModeStream = MutableStateFlow<StatsDisplayMode>(StatsDisplayMode.Day(LocalDate.now()))
    val variables = listOf(
        ReportVariable.Generation,
        ReportVariable.FeedIn,
        ReportVariable.GridConsumption,
        ReportVariable.ChargeEnergyToTal,
        ReportVariable.DischargeEnergyToTal
    )
    var rawData: List<StatsGraphValue> = listOf()
    private var totals: MutableMap<ReportVariable, Double> = mutableMapOf()

    suspend fun loadData() {
        val device = configManager.currentDevice.value ?: return

        chartColors = variables.map { it.colour() }
        val displayMode = displayModeStream.value

        val queryDate = makeQueryDate(displayMode)
        val reportType = makeReportType(displayMode)

        Log.i("AWP", "fetchData: Called")
        Log.i("AWP", "fetchData: $queryDate $reportType")

        val reportData = networking.fetchReport(
            device.deviceID,
            variables = variables.toTypedArray(),
            queryDate = queryDate,
            reportType = reportType
        )

        rawData = reportData.flatMap { reportResponse ->
            val reportVariable = ReportVariable.parse(reportResponse.variable)

            totals[reportVariable] = reportResponse.data.map { abs(it.value) }.sum()

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

                return@map StatsGraphValue(
                    graphPoint = graphPoint,
                    value = dataPoint.value,
                    type = reportVariable
                )
            }
        }

        val entries = rawData
            .groupBy { it.type }
            .map { group ->
                group.value.map {
                    FloatEntry(x = it.graphPoint.toFloat(), y = it.value.toFloat())
                }.toList()
            }.toList()

        chartColors = reportData
            .groupBy { it.variable }
            .map { ReportVariable.parse(it.value.first().variable).colour() }

        producer.setEntries(entries)
    }

    private fun makeQueryDate(displayMode: StatsDisplayMode): QueryDate {
        return when (displayMode) {
            is StatsDisplayMode.Day -> {
                val date = displayMode.date
                QueryDate(
                    year = date.year,
                    month = date.monthValue + 1,
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

    fun totalOf(it: ReportVariable): Double {
        return totals[it] ?: 0.0
    }

}

enum class ReportType {
    day,
    month,
    year,
}
