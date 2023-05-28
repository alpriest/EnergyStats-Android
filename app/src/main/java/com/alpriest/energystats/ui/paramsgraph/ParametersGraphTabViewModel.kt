package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.alpriest.energystats.ui.statsgraph.localDateTimeToMillis
import com.alpriest.energystats.ui.statsgraph.localDateToMillis
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.max

class ParametersGraphTabViewModel(
    val configManager: ConfigManaging,
    val networking: Networking
) : ViewModel() {
    var maxYStream = MutableStateFlow(0f)
    var chartColorsStream = MutableStateFlow(listOf<Color>())
    val producer: ChartEntryModelProducer = ChartEntryModelProducer()
    val displayModeStream = MutableStateFlow(ParametersDisplayMode(LocalDate.now(), 24))
    var rawData: List<ParametersGraphValue> = listOf()
    var totalsStream: MutableStateFlow<MutableMap<RawVariable, Double>> = MutableStateFlow(mutableMapOf())
    val graphVariablesStream: MutableStateFlow<List<ParameterGraphVariable>> = MutableStateFlow(listOf())
    var queryDate = QueryDate()
    var hours: Int = 24

    init {
        viewModelScope.launch {
            configManager.currentDevice
                .collect { it ->
                    it?.let { device ->
                        graphVariablesStream.value = device.variables.mapNotNull { rawVariable: RawVariable ->
                            val variable = configManager.variables.firstOrNull { it.variable == rawVariable.variable }

                            if (variable != null) {
                                return@mapNotNull ParameterGraphVariable(
                                    variable,
                                    isSelected = DefaultGraphVariables.contains(variable.variable),
                                    enabled = DefaultGraphVariables.contains(variable.variable),
                                )
                            } else {
                                return@mapNotNull null
                            }
                        }
                    }
                }
        }

        viewModelScope.launch {
            displayModeStream
                .collect { it ->
                    val previousHours = hours
                    val updatedDate = QueryDate(it.date.year, it.date.monthValue, it.date.dayOfMonth)

                    if (queryDate != updatedDate) {
                        queryDate = updatedDate
                        load()
                    }
                    if (it.hours != previousHours) {
                        hours = it.hours
                        refresh()
                    }
                }
        }
    }

    fun increase() {
        TODO("Not yet implemented")
    }

    fun decrease() {
        TODO("Not yet implemented")
    }

    suspend fun load() {
        val device = configManager.currentDevice.value ?: return
        val rawGraphVariables = graphVariablesStream.value.filter { it.isSelected }.map { it.type }.toList()
        val formatter = DateTimeFormatter.ofPattern(dateFormat)

        val raw = networking.fetchRaw(
            device.deviceID,
            variables = rawGraphVariables,
            queryDate = queryDate
        )

        var maxY = 0f
        val rawTotals: MutableMap<RawVariable, Double> = mutableMapOf()

        val rawData: List<ParametersGraphValue> = raw.flatMap { response ->
            val rawVariable = configManager.variables.firstOrNull { it.variable == response.variable } ?: return@flatMap emptyList()

            rawTotals[rawVariable] = response.data.sumOf { abs(it.value) }

            response.data.mapIndexed { index, item ->
//                val localDateTime = ZonedDateTime.parse(item.time, formatter)
//                val graphPoint: Long = localDateTimeToMillis(localDateTime)

                maxY = max(maxY, item.value.toFloat() + 0.5f)

                return@mapIndexed ParametersGraphValue(
                    graphPoint = index,
                    value = item.value,
                    type = rawVariable
                )
            }
        }

        // Fetch reports
        this.rawData = rawData
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

    fun toggleVisibility(parameterGraphVariable: ParameterGraphVariable) {
        val updated = graphVariablesStream.value.map {
            if (it.type == parameterGraphVariable.type) {
                return@map ParameterGraphVariable(it.type, !it.enabled, it.isSelected)
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

    fun setGraphVariables(graphVariables: List<ParameterGraphVariable>) {
        graphVariablesStream.value = graphVariables

        viewModelScope.launch {
            load()
        }
    }

    companion object {
        val DefaultGraphVariables = listOf(
            "generationPower",
            "batChargePower",
            "batDischargePower",
            "feedinPower",
            "gridConsumptionPower"
        )
    }
}