package com.alpriest.energystats.ui.paramsgraph

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.EnergyStatsApplication
import com.alpriest.energystats.R
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.models.toUtcMillis
import com.alpriest.energystats.parseToLocalDate
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.AppLifecycleObserver
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.CancellationException

interface ExportProviding {
    var exportFileUri: Uri?
    fun exportTo(context: Context, uri: Uri)
}

interface AlertDialogMessageProviding {
    val alertDialogMessage: MutableStateFlow<MonitorAlertDialogData?>
    fun resetDialogMessage() {
        alertDialogMessage.value = null
    }
}

data class AxisScale(val min: Float?, val max: Float?)

class ParametersGraphTabViewModel(
    val networking: Networking,
    val configManager: ConfigManaging,
    val onWriteTempFile: (String, String) -> Uri?,
    val graphVariablesStream: MutableStateFlow<List<ParameterGraphVariable>>
) : ViewModel(), ExportProviding, AlertDialogMessageProviding {
    private var exportText: String = ""
    var exportFileName: String = ""
    override var exportFileUri: Uri? = null
    val hasDataStream = MutableStateFlow(false)
    var chartColorsStream: MutableStateFlow<Map<String, List<Color>>> = MutableStateFlow(mapOf())
    val producers: MutableStateFlow<Map<String, Pair<ChartEntryModelProducer, AxisScale>>> = MutableStateFlow(mapOf())
    val displayModeStream = MutableStateFlow(ParametersDisplayMode(LocalDate.now(), 24))
    var rawData: List<ParametersGraphValue> = listOf()
    var queryDate = QueryDate()
    var hours: Int = 24
    var valuesAtTimeStream = MutableStateFlow<List<DateTimeFloatEntry>>(listOf())
    var boundsStream = MutableStateFlow<List<ParameterGraphBounds>>(listOf())
    var entriesStream = MutableStateFlow<List<List<DateTimeFloatEntry>>>(listOf())
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    var uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val xDataPointCount: MutableStateFlow<Float> = MutableStateFlow(360f)

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
                    val context = EnergyStatsApplication.applicationContext()

                    if (queryDate != updatedDate) {
                        queryDate = updatedDate
                        load(context)
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
                val context = EnergyStatsApplication.applicationContext()
                load(context)
            }
        }
    }

    suspend fun load(context: Context) {
        val device = configManager.currentDevice.value ?: return
        val rawGraphVariables = graphVariablesStream.value.filter { it.isSelected }.map { it.type.variable }.toList()
        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

        try {
            val start = queryDate.toUtcMillis()
            val end = start + (86400 * 1000)

            val historyResponse = networking.fetchHistory(
                device.deviceSN,
                variables = rawGraphVariables,
                start = start,
                end = end
            )

            val rawData: List<ParametersGraphValue> = historyResponse.datas.flatMap { response ->
                val rawVariable = configManager.variables.firstOrNull { it.variable == response.variable } ?: return@flatMap emptyList()

                response.data.mapIndexed { index, item ->
                    val localDateTime = parseToLocalDate(item.time)

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
        } catch (ex: CancellationException) {
            // Ignore as the user navigated away
        } catch (ex: Exception) {
            alertDialogMessage.value = MonitorAlertDialogData(ex, ex.localizedMessage)
        } finally {
            uiState.value = UiLoadState(LoadState.Inactive)
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
                    DateTimeFloatEntry(
                        type = it.type,
                        localDateTime = it.time,
                        x = it.graphPoint.toFloat(),
                        y = it.value.toFloat()
                    )
                }
            }

        xDataPointCount.value = calculateDataPointCount()
        boundsStream.value = entries.map { entryList ->
            val max = (entryList.maxBy { it.y }.y)
            val min = (entryList.minBy { it.y }.y)

            ParameterGraphBounds(entryList.first().type, min, max, entryList.last().y)
        }

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
                    val values = it.value.flatMap { it.map { it.value } }
                    val yAxisScale = AxisScale(values.min().toFloat() * 0.9f, values.max().toFloat() * 1.1f)

                    Pair(
                        it.key,
                        Pair(
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
                                }),
                            yAxisScale
                        )
                    )
                }
                .toMap()

            chartColorsStream.value = grouped
                .map { group ->
                    return@map Pair(group.key.unit, group.value)
                }
                .groupBy { it.first }
                .map { Pair(it.key, it.value.map { it.second.first() }) }
                .associate { Pair(it.first, it.second.map { it.type.colour() }) }
        }

        prepareExport(rawData, displayModeStream.value)
        storeVariables()
    }

    private fun calculateDataPointCount(): Float {
        val reportingIntervalMinutes: Float = if (rawData.count() > 1) {
            val firstTime = rawData[0]
            val secondTime = rawData[1]

            val duration = Duration.between(firstTime.time, secondTime.time)
            duration.toMinutes().toFloat()
        } else {
            5f
        }

        return (60 / reportingIntervalMinutes) * 24
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
    val type: Variable,
) : ChartEntry {
    override fun withY(y: Float): ChartEntry = DateTimeFloatEntry(
        localDateTime = localDateTime,
        x = x,
        y = y,
        type = type,
    )
}
