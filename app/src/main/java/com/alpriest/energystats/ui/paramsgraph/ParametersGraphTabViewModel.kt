package com.alpriest.energystats.ui.paramsgraph

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import com.alpriest.energystats.helpers.timeUntilNow
import com.alpriest.energystats.models.SolcastForecastResponse
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.models.solcastPrediction
import com.alpriest.energystats.models.truncated
import com.alpriest.energystats.parseToLocalDateTime
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.shared.models.QueryDate
import com.alpriest.energystats.shared.models.network.OpenHistoryResponse
import com.alpriest.energystats.shared.models.network.OpenHistoryResponseData
import com.alpriest.energystats.shared.models.network.UnitData
import com.alpriest.energystats.shared.models.toDate
import com.alpriest.energystats.shared.models.toUtcMillis
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.AppLifecycleObserver
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.flow.home.networkDateFormat
import com.alpriest.energystats.ui.paramsgraph.graphs.AxisScale
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.settings.solcast.toLocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.OutputStream
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.CancellationException

data class ParametersGraphViewState(
    val displayMode: ParametersDisplayMode,
    val variables: List<ParameterGraphVariable>
)

data class ParametersGraphViewData(
    val producers: Map<String, Pair<List<List<DateTimeFloatEntry>>, AxisScale>>,
    val colors: Map<String, List<Color>>
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
    private val _viewDataState = MutableStateFlow(ParametersGraphViewData(mapOf(), mapOf()))
    val viewDataState = _viewDataState.asStateFlow()
    val displayModeStream = MutableStateFlow(ParametersDisplayMode(LocalDate.now(), 24))
    private var rawData: List<ParametersGraphValue> = listOf()
    var queryDate = QueryDate()
    var hours: Int = 24
    private var _valuesAtTimeStream = MutableStateFlow<Map<Variable, List<DateTimeFloatEntry>>>(mapOf())
    var valuesAtTimeStream: StateFlow<Map<Variable, List<DateTimeFloatEntry>>> = _valuesAtTimeStream
    var selectedValueStream = MutableStateFlow<ParameterGraphLineMarkerModel?>(null)
    var boundsStream = MutableStateFlow<List<ParameterGraphBounds>>(listOf())
    var entriesStream = MutableStateFlow<List<List<DateTimeFloatEntry>>>(listOf())
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    var uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    private var lastLoadState: LastLoadState<ParametersGraphViewState>? = null

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

        viewModelScope.launch {
            selectedValueStream.collect { selectedValue ->
                if (selectedValue == null) {
                    _valuesAtTimeStream.value = mapOf()
                } else {
                    _valuesAtTimeStream.value = entriesStream.value.associate { entryList ->
                        val variableType = entryList.first().type
                        val matchingEntries = entryList.filter { it.localDateTime == selectedValue.time }
                        variableType to matchingEntries
                    }
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

    private fun requiresLoad(): Boolean {
        val lastLoadState = lastLoadState ?: return true

        val sufficientTimeHasPassed = lastLoadState.lastLoadTime.timeUntilNow() > (5 * 60)
        val viewDataHasChanged = lastLoadState.loadState.displayMode != displayModeStream.value ||
                lastLoadState.loadState.variables != graphVariablesStream.value
        return sufficientTimeHasPassed || viewDataHasChanged
    }

    suspend fun load() {
        val device = configManager.currentDevice.value ?: return
        if (!requiresLoad()) {
            return
        }
        val rawGraphVariables = graphVariablesStream.value
            .filter { it.isSelected }
            .filter { it.type.variable != Variable.solcastPrediction.variable }
            .map { it.type.variable }
            .toList()
        uiState.value = UiLoadState(LoadState.Active.Loading)

        try {
            val start = queryDate.toUtcMillis()
            val end = start + (86400 * 1000)

            val serverSpecifiedHistoryResponse = networking.fetchHistory(
                device.deviceSN,
                variables = rawGraphVariables,
                start = start,
                end = end
            )

            yield()

            val backfilledHistoryResponse = backfillMissingTimes(serverSpecifiedHistoryResponse)

            val rawData: List<ParametersGraphValue> = backfilledHistoryResponse.datas.flatMap { response ->
                val configVariable = configManager.variables.firstOrNull { cv -> cv.fuzzyNameMatches(response.variable) } ?: return@flatMap emptyList()

                response.data.mapIndexed { index, item ->
                    val localDateTime = parseToLocalDateTime(item.time)

                    return@mapIndexed ParametersGraphValue(
                        graphPoint = index,
                        time = localDateTime,
                        value = item.value,
                        type = configVariable
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
        val hours = displayModeStream.value.hours
        val now = LocalDateTime.now()
        val oldest = displayModeStream.value.date.atTime(now.hour, now.minute).minusHours(hours.toLong())
        val grouped = rawData
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

            // Build producers for all variables
            val allVariables = graphVariablesStream.value
                .filter { it.isSelected }
                .map { it.type }

            val producers = allVariables
                .map { variable ->
                    val valuesForVariable: List<ParametersGraphValue> = grouped[variable] ?: emptyList()
                    Pair(variable.unit, valuesForVariable)
                }
                .groupBy { it.first } // group by unit
                .map { (unit, pairsForUnit) ->
                    val groupedValues: List<List<ParametersGraphValue>> = pairsForUnit.map { it.second }
                    val allValues = groupedValues.flatMap { list -> list.map { it.value } }

                    val yAxisScale = if (allValues.isNotEmpty()) {
                        AxisScale(allValues.min().toFloat() * 0.9f, allValues.max().toFloat() * 1.1f)
                    } else {
                        // Default scale when no visible data exists for this unit.
                        AxisScale(0f, 1f)
                    }

                    unit to Pair(
                        groupedValues.map { group ->
                            group.map { graphValue ->
                                DateTimeFloatEntry(
                                    type = graphValue.type,
                                    localDateTime = graphValue.time,
                                    x = graphValue.graphPoint.toFloat(),
                                    y = graphValue.value.toFloat()
                                )
                            }
                        },
                        yAxisScale
                    )
                }
                .toMap()

            val chartColorsStream = graphVariablesStream.value
                .filter { it.isSelected }
                .groupBy { it.type.unit }
                .mapValues { (_, varsForUnit) ->
                    varsForUnit.map { variable ->
                        if (variable.enabled) {
                            variable.type.colour()
                        } else {
                            Color.Transparent
                        }
                    }
                }

            _viewDataState.value = ParametersGraphViewData(producers, chartColorsStream)
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

    fun toggleVisibility(parameterGraphVariable: ParameterGraphVariable, unit: String?) {
        val stream = graphVariablesStream.value
        val updated = stream.map {
            if (it.type == parameterGraphVariable.type) {
                return@map ParameterGraphVariable(type = it.type, enabled = !it.enabled, isSelected = it.isSelected)
            } else {
                return@map it
            }
        }

        if (updated.count { it.isSelected && it.enabled && (unit == null || it.type.unit == unit) } == 0) {
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
                    value = response.pvEstimate.truncated(configManager.decimalPlaces),
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

    private fun backfillMissingTimes(historyResponse: OpenHistoryResponse): OpenHistoryResponse {
        return OpenHistoryResponse(
            deviceSN = historyResponse.deviceSN,
            datas = historyResponse.datas.map { backfillMissingTimes(it) }
        )
    }

    private fun backfillMissingTimes(openHistoryResponseData: OpenHistoryResponseData): OpenHistoryResponseData {
        if (openHistoryResponseData.data.size < 2) return openHistoryResponseData

        val first = parseToLocalDateTime(openHistoryResponseData.data[0].time)
        val second = parseToLocalDateTime(openHistoryResponseData.data[1].time)
        val interval = Duration.between(first, second) // assume constant interval
        val formatter = DateTimeFormatter.ofPattern(networkDateFormat)

        val midnight = first.toLocalDate().atStartOfDay()

        val backfilled = mutableListOf<UnitData>()
        var t = first
        while (t.isAfter(midnight)) {
            t = t.minus(interval)
            if (t.isAfter(midnight) || t.isEqual(midnight)) {
                backfilled.add(0, UnitData(t.format(formatter), 0.0))
            }
        }

        return OpenHistoryResponseData(
            unit = openHistoryResponseData.unit,
            variable = openHistoryResponseData.variable,
            name = openHistoryResponseData.name,
            data = backfilled + openHistoryResponseData.data
        )
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
    val x: Float,
    val y: Float,
    val type: Variable,
) {
    var graphPoint: Long = this.localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond()

    fun formattedValue(decimalPlaces: Int): String {
        return when (type.unit) {
            "kW" -> y.toDouble().kW(decimalPlaces)
            else -> "$y ${type.unit}"
        }
    }
}
