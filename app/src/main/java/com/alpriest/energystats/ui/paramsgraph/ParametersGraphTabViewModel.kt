package com.alpriest.energystats.ui.paramsgraph

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.EnergyStatsApplication
import com.alpriest.energystats.R
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.SolcastForecastResponse
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.models.rounded
import com.alpriest.energystats.models.solcastPrediction
import com.alpriest.energystats.models.toDate
import com.alpriest.energystats.models.toUtcMillis
import com.alpriest.energystats.parseToLocalDate
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.AppLifecycleObserver
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.settings.solcast.toLocalDateTime
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

data class LastLoadState<T>(
    val lastLoadTime: LocalDateTime,
    val loadState: T
)

data class AxisScale(val min: Float?, val max: Float?)

data class ParametersGraphViewState(
    val displayMode: ParametersDisplayMode,
    val variables: List<ParameterGraphVariable>
)

class ParametersGraphTabViewModel(
    val networking: Networking,
    val configManager: ConfigManaging,
    val onWriteTempFile: (String, String) -> Uri?,
    val graphVariablesStream: MutableStateFlow<List<ParameterGraphVariable>>,
    private val solarForecastProvider: () -> SolcastCaching
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
    private var lastLoadState: LastLoadState<ParametersGraphViewState>? = null
    var lastMarkerModelStream = MutableStateFlow<ParameterGraphVerticalLineMarkerModel?>(null)

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

    private fun requiresLoad(): Boolean {
        val lastLoadState = lastLoadState ?: return true

        val sufficientTimeHasPassed = lastLoadState.lastLoadTime.timeUntilNow() > (5 * 60)
        val viewDataHasChanged = lastLoadState.loadState.displayMode != displayModeStream.value ||
                lastLoadState.loadState.variables != graphVariablesStream.value
        return sufficientTimeHasPassed || viewDataHasChanged
    }

    suspend fun load(context: Context) {
        val device = configManager.currentDevice.value ?: return
        if (!requiresLoad()) {
            return
        }
        val rawGraphVariables = graphVariablesStream.value
            .filter { it.isSelected }
            .filter { it.type.variable != Variable.solcastPrediction.variable }
            .map { it.type.variable }
            .toList()
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

            yield()

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
            val solarData: List<ParametersGraphValue> =
                if (graphVariablesStream.value.filter { it.isSelected }.any { it.type.variable == Variable.solcastPrediction.variable }) fetchSolarForecasts() else listOf()

            this.rawData = rawData + solarData
            refresh()
            lastLoadState = LastLoadState(lastLoadTime = LocalDateTime.now(), ParametersGraphViewState(displayModeStream.value, graphVariablesStream.value))
        } catch (ex: CancellationException) {
            Log.d("AWP", "CancellationException")
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

    private suspend fun fetchSolarForecasts(): List<ParametersGraphValue> {
        val settings = configManager.solcastSettings
        if (settings.sites.isEmpty() || settings.apiKey == null) {
            return listOf()
        }

        val queryDate = getQueryDate()

        try {
            val sites = settings.sites
            val data = sites.flatMap {
                val forecasts = solarForecastProvider().fetchForecast(it, settings.apiKey, false).forecasts
                return@flatMap forecasts.filter { response ->
                    isSameDay(response.periodEnd, queryDate)
                }
            }

            val groupedForecasts = aggregateForecasts(data)

            return groupedForecasts.map { response ->
                val dateTime = response.periodEnd.toLocalDateTime()
                val minutesSinceMidnight = dateTime.hour * 60 + dateTime.minute
                val graphPoint = (minutesSinceMidnight / (24.0 * 60.0) * 288).toInt() // Scale between 0 and 288

                ParametersGraphValue(
                    graphPoint = graphPoint,
                    time = dateTime,
                    value = response.pvEstimate.rounded(configManager.decimalPlaces),
                    type = Variable.solcastPrediction
                )
            }.sortedBy { it.time }
        } catch (ex: Exception) {
            return listOf()
        }
    }

    private fun aggregateForecasts(forecasts: List<SolcastForecastResponse>): List<SolcastForecastResponse> {
        return forecasts.groupBy { it.periodEnd }
            .map { (periodEnd, entries) ->
                SolcastForecastResponse(
                    pvEstimate = entries.sumOf { it.pvEstimate },
                    pvEstimate10 = entries.sumOf { it.pvEstimate10 },
                    pvEstimate90 = entries.sumOf { it.pvEstimate90 },
                    periodEnd = periodEnd,
                    period = entries.firstOrNull()?.period ?: ""
                )
            }.sortedBy { it.periodEnd } // Ensure chronological order
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        return localDate1 == localDate2
    }

    private fun getQueryDate(): Date {
        return queryDate.toDate()
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

    fun formattedValue(decimalPlaces: Int): String {
        return when (type.unit) {
            "kW" -> y.toDouble().kW(decimalPlaces)
            else -> "$y ${type.unit}"
        }
    }
}

fun LocalDateTime.timeUntilNow(): Long {
    val now = LocalDateTime.now(ZoneId.systemDefault())
    return Duration.between(this, now).seconds
}

fun LocalDateTime.isSameDay(other: LocalDateTime): Boolean {
    return this.toLocalDate() == other.toLocalDate()
}

fun LocalDate.monthYear(): String {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month.value - 1)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val date = calendar.time
    val dateFormatter = SimpleDateFormat("MMMM y", Locale.getDefault())
    return dateFormatter.format(date)
}
