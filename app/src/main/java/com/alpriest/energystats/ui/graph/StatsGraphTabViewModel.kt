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
import java.util.*

class StatsGraphTabViewModel(
    val configManager: ConfigManaging,
    val networking: Networking
) : ViewModel() {
    var chartColors = listOf<Color>()
    val producer: ChartEntryModelProducer = ChartEntryModelProducer()
    val displayModeStream = MutableStateFlow<StatsDisplayMode>(StatsDisplayMode.Day(Calendar.getInstance().timeInMillis))
    val variables = listOf(
        ReportVariable.Generation,
        ReportVariable.FeedIn,
        ReportVariable.GridConsumption,
        ReportVariable.ChargeEnergyToTal,
        ReportVariable.DischargeEnergyToTal
    )

    init {
        viewModelScope.launch {
            displayModeStream.collect { newValue ->
                fetchData()
            }
        }
    }

    private suspend fun fetchData() {
        val device = configManager.currentDevice.value ?: return

        chartColors = variables.map { it.colour() }

        Log.i("AWP", "fetchData: Called")
        Log.i("AWP", "fetchData: ${displayModeStream.value}")

        val queryDate = makeQueryDate(displayModeStream.value)
        val reportType = makeReportType(displayModeStream.value)

        val reportData = networking.fetchReport(
            device.deviceID,
            variables = variables.toTypedArray(),
            queryDate = queryDate,
            reportType = reportType
        )

        val entries = reportData
            .groupBy { it.variable }
            .map { group ->
                group.value.flatMap {
                    it.data.map {
                        FloatEntry(x = it.index.toFloat(), y = it.value.toFloat())
                    }
                }.toList()
            }.toList()

        chartColors = reportData
            .groupBy { it.variable }
            .map { ReportVariable.parse(it.value.first().variable).colour() }

        producer.setEntries(entries)
    }

    fun makeQueryDate(displayMode: StatsDisplayMode): QueryDate {
        return when (displayMode) {
            is StatsDisplayMode.Day -> {
                val date = Date(displayMode.date)
                QueryDate(
                    year = Calendar.getInstance().apply { time = date }.get(Calendar.YEAR),
                    month = Calendar.getInstance().apply { time = date }.get(Calendar.MONTH) + 1,
                    day = Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_MONTH)
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

    fun makeReportType(displayMode: StatsDisplayMode): ReportType {
        return when (displayMode) {
            is StatsDisplayMode.Day -> ReportType.day
            is StatsDisplayMode.Month -> ReportType.month
            is StatsDisplayMode.Year -> ReportType.year
        }
    }

}

enum class ReportType {
    day,
    month,
    year,
}
