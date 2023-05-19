package com.alpriest.energystats.ui.graph

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
    configManager: ConfigManaging,
    networking: Networking
) : ViewModel() {
    var chartColors = listOf<Color>()
    val producer: ChartEntryModelProducer = ChartEntryModelProducer()
    val displayMode = MutableStateFlow<StatsDisplayMode>(StatsDisplayMode.Day(Calendar.getInstance().timeInMillis))
    val variables = listOf(
        ReportVariable.Generation,
        ReportVariable.FeedIn,
        ReportVariable.GridConsumption,
        ReportVariable.ChargeEnergyToTal,
        ReportVariable.DischargeEnergyToTal
    )

    init {
        viewModelScope.launch {
            configManager.currentDevice.value?.let {
                chartColors = variables.map { it.colour() }

                val reportData = networking.fetchReport(
                    it.deviceID,
                    variables = variables.toTypedArray(),
                    queryDate = QueryDate(2023, 5, 14) // TODO Correct
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
        }
    }
}