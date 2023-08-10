package com.alpriest.energystats.ui.paramsgraph

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.alpriest.energystats.ui.statsgraph.StatsDisplayMode
import com.alpriest.energystats.ui.statsgraph.StatsGraphValue
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import kotlin.math.max

class ParametersGraphTabViewModel(
    val configManager: ConfigManaging,
    val networking: Networking,
    val onWriteTempFile: (String, String) -> Uri?
) : ViewModel() {
    var exportFileUri: Uri? = null
    val hasDataStream = MutableStateFlow(false)
    var maxYStream = MutableStateFlow(0f)
    var chartColorsStream = MutableStateFlow(listOf<Color>())
    val producer: ChartEntryModelProducer = ChartEntryModelProducer()
    val displayModeStream = MutableStateFlow(ParametersDisplayMode(LocalDate.now(), 24))
    var rawData: List<ParametersGraphValue> = listOf()
    val graphVariablesStream: MutableStateFlow<List<ParameterGraphVariable>> = MutableStateFlow(listOf())
    var queryDate = QueryDate()
    var hours: Int = 24
    var valuesAtTimeStream = MutableStateFlow<List<DateTimeFloatEntry>>(listOf())
    var boundsStream = MutableStateFlow<List<ParameterGraphBounds>>(listOf())
    var entriesStream = MutableStateFlow<List<List<DateTimeFloatEntry>>>(listOf())

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

    suspend fun load() {
        val device = configManager.currentDevice.value ?: return
        val rawGraphVariables = graphVariablesStream.value.filter { it.isSelected }.map { it.type }.toList()

        val raw = networking.fetchRaw(
            device.deviceID,
            variables = rawGraphVariables,
            queryDate = queryDate
        )

        var maxY = 0f

        val rawData: List<ParametersGraphValue> = raw.flatMap { response ->
            val rawVariable = configManager.variables.firstOrNull { it.variable == response.variable } ?: return@flatMap emptyList()

            response.data.mapIndexed { index, item ->
                maxY = max(maxY, item.value.toFloat() + 0.5f)
                val simpleDate = SimpleDateFormat(dateFormat, Locale.getDefault()).parse(item.time)
                val localDateTime = simpleDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

                return@mapIndexed ParametersGraphValue(
                    graphPoint = index,
                    time = localDateTime,
                    value = item.value,
                    type = rawVariable
                )
            }
        }

        this.rawData = rawData
        maxYStream.value = maxY

        refresh()
    }

    private fun refresh() {
        val hiddenVariables = graphVariablesStream.value.filter { !it.enabled }.map { it.type }
        val hours = displayModeStream.value.hours
        val now = LocalDateTime.now()
        val oldest = displayModeStream.value.date.atTime(now.hour, now.minute).minusHours(hours.toLong())
        val grouped = rawData
            .filter { !hiddenVariables.contains(it.type) }
            .filter {
                it.time > oldest
            }
            .groupBy { it.type }
        val entries = grouped
            .map { group ->
                group.value.map {
                    return@map DateTimeFloatEntry(
                        type = it.type,
                        localDateTime = it.time,
                        x = it.graphPoint.toFloat(),
                        y = it.value.toFloat()
                    )
                }.toList()
            }.toList()

        boundsStream.value = entries.map { entryList ->
            val max = entryList.maxBy { it.y }.y
            val min = entryList.minBy { it.y }.y
            ParameterGraphBounds(entryList.first().type, min, max)
        }

        chartColorsStream.value = grouped
            .map { it.key.colour() }

        if (entries.isEmpty()) {
            hasDataStream.value = false
            entriesStream.value = listOf()
        } else {
            hasDataStream.value = true
            entriesStream.value = entries
            producer.setEntries(entries)
        }

        prepareExport(rawData, displayModeStream.value)
    }

    private fun prepareExport(rawData: List<ParametersGraphValue>, displayMode: ParametersDisplayMode) {
        val headers = listOf("Type", "Date", "Value").joinToString(",")
        val rows = rawData.map {
            listOf(it.type.variable, it.time.toString(), it.value.toString()).joinToString(",")
        }

        val exportText = (listOf(headers) + rows).joinToString(separator = "\n")
        val exportFileName: String

        val date = displayMode.date
        val year = date.year
        val month = date.month.name
        val day = date.dayOfMonth

        exportFileName = "energystats_${year}_${month}_$day"

        exportFileUri = onWriteTempFile(exportFileName, exportText)
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

class DateTimeFloatEntry(
    val localDateTime: LocalDateTime,
    override val x: Float,
    override val y: Float,
    val type: RawVariable,
) : ChartEntry {
    override fun withY(y: Float): ChartEntry = DateTimeFloatEntry(
        localDateTime = localDateTime,
        x = x,
        y = y,
        type = type,
    )
}
