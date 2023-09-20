package com.alpriest.energystats.ui.paramsgraph

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.AppLifecycleObserver
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGraphVariableChooserViewModel
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

class ParametersGraphTabViewModel(
    val configManager: ConfigManaging,
    val networking: Networking,
    val onWriteTempFile: (String, String) -> Uri?
) : ViewModel() {
    var exportFileUri: Uri? = null
    val hasDataStream = MutableStateFlow(false)
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

    private val appLifecycleObserver = AppLifecycleObserver(
        onAppGoesToBackground = { },
        onAppEntersForeground = { appEntersForeground() }
    )

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
                                    isSelected = selectedGraphVariables().contains(variable.variable),
                                    enabled = selectedGraphVariables().contains(variable.variable),
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

        appLifecycleObserver.attach()
    }

    fun finalize() {
        appLifecycleObserver.detach()
    }

    private fun appEntersForeground() {
        if (rawData.isNotEmpty()) {
            viewModelScope.launch {
                load()
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

        val rawData: List<ParametersGraphValue> = raw.flatMap { response ->
            val rawVariable = configManager.variables.firstOrNull { it.variable == response.variable } ?: return@flatMap emptyList()

            response.data.mapIndexed { index, item ->
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

            ParameterGraphBounds(entryList.first().type, min, max, entryList.last().y)
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
        storeVariables()
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

    fun selectedGraphVariables(): List<String> {
        if (configManager.selectedParameterGraphVariables.isEmpty()) {
            return ParameterGraphVariableChooserViewModel.DefaultGraphVariables
        } else {
            return configManager.selectedParameterGraphVariables
        }
    }

    private fun storeVariables() {
        configManager.selectedParameterGraphVariables = graphVariablesStream.value.filter { it.isSelected }.map { it.type.variable }
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
