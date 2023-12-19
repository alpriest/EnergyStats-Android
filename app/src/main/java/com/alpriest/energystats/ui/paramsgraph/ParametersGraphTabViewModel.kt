package com.alpriest.energystats.ui.paramsgraph

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.AppLifecycleObserver
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider.Companion.fixed
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

interface ExportProviding {
    var exportFileUri: Uri?
    fun exportTo(context: Context, uri: Uri)
}

interface AlertDialogMessageProviding {
    val alertDialogMessage: MutableStateFlow<String?>
    fun resetDialogMessage() {
        alertDialogMessage.value = null
    }
}

class ParametersGraphTabViewModel(
    val networking: FoxESSNetworking,
    val configManager: ConfigManaging,
    val onWriteTempFile: (String, String) -> Uri?,
    val graphVariablesStream: MutableStateFlow<List<ParameterGraphVariable>>
) : ViewModel(), ExportProviding, AlertDialogMessageProviding {
    private var exportText: String = ""
    var exportFileName: String = ""
    override var exportFileUri: Uri? = null
    val hasDataStream = MutableStateFlow(false)
    var chartColorsStream = MutableStateFlow(listOf<Color>())
    val producers: MutableStateFlow<Map<String, ChartEntryModelProducer>> = MutableStateFlow(mapOf())
    val displayModeStream = MutableStateFlow(ParametersDisplayMode(LocalDate.now(), 24))
    var rawData: List<ParametersGraphValue> = listOf()
    var queryDate = QueryDate()
    var hours: Int = 24
    var valuesAtTimeStream = MutableStateFlow<List<DateTimeFloatEntry>>(listOf())
    var boundsStream = MutableStateFlow<List<ParameterGraphBounds>>(listOf())
    var entriesStream = MutableStateFlow<List<List<DateTimeFloatEntry>>>(listOf())
    override val alertDialogMessage = MutableStateFlow<String?>(null)

    private val appLifecycleObserver = AppLifecycleObserver(
        onAppGoesToBackground = { },
        onAppEntersForeground = { appEntersForeground() }
    )

    init {
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

        try {
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
        } catch (ex: Exception) {
            alertDialogMessage.value = ex.localizedMessage
        }
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
                }
            }

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

            producers.value = grouped
                .map { group ->
                    return@map Pair(group.key.unit, group.value)
                }
                .groupBy { it.first }
                .map { Pair(it.key, it.value.map { it.second }) }
                .toMap()
                .map {
                    Pair(
                        it.key,
                        ChartEntryModelProducer(
                            it.value.map { group ->
                                group.map { graphValue ->
                                    return@map DateTimeFloatEntry(
                                        type = graphValue.type,
                                        localDateTime = graphValue.time,
                                        x = graphValue.graphPoint.toFloat(),
                                        y = graphValue.value.toFloat()
                                    )
                                }
                            })
                    )
                }
                .toMap()
        }

        prepareExport(rawData, displayModeStream.value)
        storeVariables()
    }

    private fun prepareExport(rawData: List<ParametersGraphValue>, displayMode: ParametersDisplayMode) {
        val headers = listOf("Type", "Date", "Value").joinToString(",")
        val rows = rawData.map {
            listOf(it.type.variable, it.time.toString(), it.value.toString()).joinToString(",")
        }

        val date = displayMode.date
        val year = date.year
        val month = date.month.name
        val day = date.dayOfMonth

        exportText = (listOf(headers) + rows).joinToString(separator = "\n")
        val baseExportFileName = "energystats_${year}_${month}_$day"
        exportFileUri = onWriteTempFile(baseExportFileName, exportText)
        exportFileName = "$baseExportFileName.csv"
    }

    fun toggleVisibility(parameterGraphVariable: ParameterGraphVariable) {
        val stream = graphVariablesStream.value
        val updated = stream.map {
            if (it.type == parameterGraphVariable.type) {
                return@map ParameterGraphVariable(type = it.type, enabled = !it.enabled, isSelected = it.isSelected)
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

    private fun storeVariables() {
        configManager.selectedParameterGraphVariables = graphVariablesStream.value.filter { it.isSelected }.map { it.type.variable }
    }

    override fun exportTo(context: Context, uri: Uri) {
        writeContentToUri(context, uri, exportText)
    }
}

fun writeContentToUri(context: Context, uri: Uri, content: String) {
    val contentResolver: ContentResolver = context.contentResolver
    var outputStream: OutputStream? = null

    try {
        outputStream = contentResolver.openOutputStream(uri)
        outputStream?.write(content.toByteArray())
    } catch (e: Exception) {
        // Handle exceptions here
        e.printStackTrace()
    } finally {
        outputStream?.close()
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
