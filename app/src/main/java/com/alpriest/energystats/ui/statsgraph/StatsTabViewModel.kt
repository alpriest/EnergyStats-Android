package com.alpriest.energystats.ui.statsgraph

import android.content.Context
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.EnergyStatsApplication
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ValueUsage
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.AppLifecycleObserver
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.paramsgraph.ExportProviding
import com.alpriest.energystats.ui.paramsgraph.LastLoadState
import com.alpriest.energystats.ui.paramsgraph.isSameDay
import com.alpriest.energystats.ui.paramsgraph.writeContentToUri
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.summary.ApproximationsCalculator
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException

data class StatsGraphValue(val type: ReportVariable, val graphPoint: Int, val graphValue: Double) {
    fun periodDescription(displayMode: StatsDisplayMode): String {
        return when (displayMode) {
            is StatsDisplayMode.Day -> {
                val time = LocalTime.of(graphPoint, 0) // Assuming graphPoint represents the hour
                val formatter = DateTimeFormatter.ofPattern("HH:mm") // 24-hour format
                time.format(formatter)
            }

            is StatsDisplayMode.Month -> {
                val dateFormatSymbols = DateFormatSymbols.getInstance()
                val monthName = dateFormatSymbols.months.getOrNull(displayMode.month) ?: "${displayMode.month}"
                "$graphPoint $monthName"
            }

            is StatsDisplayMode.Year -> {
                val dateFormatSymbols = DateFormatSymbols.getInstance()
                val monthName = dateFormatSymbols.months.getOrNull(graphPoint - 1) ?: "$graphPoint"
                "$monthName ${displayMode.year}"
            }

            is StatsDisplayMode.Custom -> {
                val start = displayMode.start
                val end = displayMode.end

                "${start.year}_${start.month}_$start.day_${end.year}_${end.month}_$end.day"
            }
        }
    }
}

class StatsTabViewModel(
    val configManager: ConfigManaging,
    networking: Networking,
    val onWriteTempFile: (String, String) -> Uri?
) : ViewModel(), ExportProviding, AlertDialogMessageProviding {
    var chartColorsStream = MutableStateFlow(listOf<ReportVariable>())
    val selfSufficiencyGraphDataStream = MutableStateFlow<ChartEntryModel?>(null)
    val statsGraphDataStream = MutableStateFlow<ChartEntryModel?>(null)
    val displayModeStream = MutableStateFlow<StatsDisplayMode>(StatsDisplayMode.Day(LocalDate.now()))
    val graphVariablesStream = MutableStateFlow<List<StatsGraphVariable>>(listOf())
    var valuesAtTimeStream = MutableStateFlow<List<StatsChartEntry>>(listOf())
    var totalsStream: MutableStateFlow<MutableMap<ReportVariable, Double>> = MutableStateFlow(mutableMapOf())
    var exportFileName: String = ""
    override var exportFileUri: Uri? = null
    var approximationsViewModelStream = MutableStateFlow<ApproximationsViewModel?>(null)
    var showingGraphStream = MutableStateFlow(true)
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    var uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    private var rawData: List<StatsGraphValue> = listOf()
    private var exportText: String = ""
    private val approximationsCalculator: ApproximationsCalculator = ApproximationsCalculator(configManager, networking)
    private val fetcher = StatsDataFetcher(networking, approximationsCalculator)
    private var lastLoadState: LastLoadState<StatsDisplayMode>? = null
    private var maxIndex: Float? = null
    private var lastSelectedIndex: Float? = null

    private val appLifecycleObserver = AppLifecycleObserver(
        onAppGoesToBackground = { },
        onAppEntersForeground = { appEntersForeground() }
    )

    init {
        appLifecycleObserver.attach()
        viewModelScope.launch {
            configManager.currentDevice
                .collect {
                    it?.let { device ->
                        updateGraphVariables(device)
                    }
                }
        }
    }

    fun finalize() {
        appLifecycleObserver.detach()
    }

    private fun updateGraphVariables(device: Device) {
        graphVariablesStream.value = listOf(
            ReportVariable.Generation,
            ReportVariable.FeedIn,
            ReportVariable.GridConsumption,
            if (device.hasBattery) ReportVariable.ChargeEnergyToTal else null,
            if (device.hasBattery) ReportVariable.DischargeEnergyToTal else null,
            ReportVariable.Loads,
            if (configManager.showSelfSufficiencyStatsGraphOverlay && configManager.selfSufficiencyEstimateMode != SelfSufficiencyEstimateMode.Off) ReportVariable.SelfSufficiency else null
        ).mapNotNull { it }.map {
            StatsGraphVariable(it, true)
        }
    }

    suspend fun load(context: Context) {
        val device = configManager.currentDevice.value ?: return
        if (!requiresLoad()) return
        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))
        if (graphVariablesStream.value.isEmpty()) {
            updateGraphVariables(device)
        }
        val displayMode = displayModeStream.value
        val reportVariables: List<ReportVariable> = listOf(
            ReportVariable.FeedIn,
            ReportVariable.Generation,
            ReportVariable.ChargeEnergyToTal,
            ReportVariable.DischargeEnergyToTal,
            ReportVariable.GridConsumption,
            ReportVariable.Loads
        )

        try {
            val updatedData: List<StatsGraphValue>
            val totals: MutableMap<ReportVariable, Double>

            if (displayMode is StatsDisplayMode.Custom) {
                val result = fetcher.fetchCustomData(
                    device,
                    displayMode.start,
                    displayMode.end,
                    reportVariables,
                    displayMode
                )
                updatedData = result.first
                totals = result.second
            } else {
                val result = fetcher.fetchData(
                    device,
                    reportVariables,
                    displayMode
                )
                updatedData = result.first
                totals = result.second
            }

            yield()

            rawData = updatedData + generateSelfSufficiency(updatedData)
            totalsStream.value = totals
            refresh()
            calculateSelfSufficiencyEstimate()
            uiState.value = UiLoadState(LoadState.Inactive)
            lastLoadState = LastLoadState(LocalDateTime.now(), displayMode)
        } catch (ex: CancellationException) {
            Log.d("AWP", "CancellationException")
            // Ignore as the user navigated away
        } catch (ex: Exception) {
            alertDialogMessage.value = MonitorAlertDialogData(ex, ex.localizedMessage)
            uiState.value = UiLoadState(LoadState.Inactive)
            return
        }
    }

    private fun requiresLoad(): Boolean {
        val lastLoadState = lastLoadState ?: return true

        val now = LocalDateTime.now(ZoneId.systemDefault())
        val lastLoadHour = lastLoadState.lastLoadTime.hour
        val currentHour = now.hour

        val sufficientTimeHasPassed = !lastLoadState.lastLoadTime.isSameDay(now) || lastLoadHour != currentHour
        val viewDataHasChanged = lastLoadState.loadState != displayModeStream.value

        return sufficientTimeHasPassed || viewDataHasChanged
    }

    private fun appEntersForeground() {
        val context = EnergyStatsApplication.applicationContext()
        if (totalsStream.value.isNotEmpty()) {
            viewModelScope.launch {
                load(context)
            }
        }
    }

    private fun prepareExport(rawData: List<StatsGraphValue>, displayMode: StatsDisplayMode) {
        val headers = listOf("Type", "Date", "Value").joinToString(",")
        val rows = rawData.map {
            listOf(it.type.networkTitle(), it.graphPoint, it.graphValue.toString()).joinToString(",")
        }

        val baseExportFileName: String

        when (displayMode) {
            is StatsDisplayMode.Day -> {
                val date = displayMode.date

                val year = date.year
                val month = date.month.name
                val day = date.dayOfMonth

                baseExportFileName = "energystats_${year}_${month}_$day"
            }

            is StatsDisplayMode.Month -> {
                val dateFormatSymbols = DateFormatSymbols.getInstance()
                val month = dateFormatSymbols.months.getOrNull(displayMode.month) ?: "${displayMode.month}"
                val year = displayMode.year
                baseExportFileName = "energystats_${year}_$month"
            }

            is StatsDisplayMode.Year -> {
                val year = displayMode.year
                baseExportFileName = "energystats_$year"
            }

            is StatsDisplayMode.Custom -> {
                val start = displayMode.start
                val end = displayMode.end

                baseExportFileName = "energystats_${start.year}_${start.month}_$start.day_${end.year}_${end.month}_$end.day"
            }
        }

        exportText = (listOf(headers) + rows).joinToString(separator = "\n")
        exportFileUri = onWriteTempFile(baseExportFileName, exportText)
        exportFileName = "$baseExportFileName.txt"
    }

    override fun exportTo(context: Context, uri: Uri) {
        writeContentToUri(context, uri, exportText)
    }

    private fun refresh() {
        val hiddenVariables = graphVariablesStream.value.filter { !it.enabled }.map { it.type }
        val grouped = rawData
            .filter { it.type != ReportVariable.SelfSufficiency }
            .filter { !hiddenVariables.contains(it.type) }.groupBy { it.type }
        val entries = grouped
            .map { group ->
                group.value.map {
                    StatsChartEntry(
                        periodDescription = it.periodDescription(displayModeStream.value),
                        x = it.graphPoint.toFloat(),
                        y = it.graphValue.toFloat(),
                        type = it.type,
                    )
                }.toList()
            }.toList()

        chartColorsStream.value = grouped.keys.toList()
        statsGraphDataStream.value = ChartEntryModelProducer(entries).getModel()

        selfSufficiencyGraphDataStream.value = ChartEntryModelProducer(rawData
            .filter { it.type == ReportVariable.SelfSufficiency }
            .filter { !hiddenVariables.contains(it.type) }
            .map {
                StatsChartEntry(
                    periodDescription = it.periodDescription(displayModeStream.value),
                    x = it.graphPoint.toFloat(),
                    y = it.graphValue.toFloat(),
                    type = it.type
                )
            }).getModel()

        maxIndex = entries.flatten()
            .maxByOrNull { it.y }
            ?.x

        prepareExport(rawData, displayModeStream.value)
    }

    fun toggleVisibility(statsGraphVariable: StatsGraphVariable) {
        val updated = graphVariablesStream.value.map {
            if (it.type == statsGraphVariable.type) {
                return@map StatsGraphVariable(it.type, !it.enabled)
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

    private fun calculateSelfSufficiencyEstimate() {
        val totals = totalsStream.value
        val feedIn = totals[ReportVariable.FeedIn] ?: 0.0
        val grid = totals[ReportVariable.GridConsumption] ?: 0.0
        val batteryCharge = totals[ReportVariable.ChargeEnergyToTal] ?: 0.0
        val batteryDischarge = totals[ReportVariable.DischargeEnergyToTal] ?: 0.0
        val loads = totals[ReportVariable.Loads] ?: 0.0

        approximationsViewModelStream.value = approximationsCalculator.calculateApproximations(
            grid = grid,
            feedIn = feedIn,
            loads = loads,
            batteryCharge = batteryCharge,
            batteryDischarge = batteryDischarge,
        )
    }

    private fun generateSelfSufficiency(rawData: List<StatsGraphValue>): List<StatsGraphValue> {
        return if (configManager.selfSufficiencyEstimateMode != SelfSufficiencyEstimateMode.Off && configManager.showSelfSufficiencyStatsGraphOverlay) {
            calculateSelfSufficiencyAcrossTimePeriod(rawData)
        } else {
            listOf()
        }
    }

    private fun calculateSelfSufficiencyAcrossTimePeriod(rawData: List<StatsGraphValue>): List<StatsGraphValue> {
        val graphPoints = rawData.map { it.graphPoint }.distinct()
        val entries: MutableList<StatsGraphValue> = mutableListOf()

        for (graphPoint in graphPoints) {
            val valuesAtTime = ValuesAtTime(values = rawData.filter { it.graphPoint == graphPoint })

            val grid = valuesAtTime.values.firstOrNull { it.type == ReportVariable.GridConsumption }
            val feedIn = valuesAtTime.values.firstOrNull { it.type == ReportVariable.FeedIn }
            val loads = valuesAtTime.values.firstOrNull { it.type == ReportVariable.Loads }
            val batteryCharge = valuesAtTime.values.firstOrNull { it.type == ReportVariable.ChargeEnergyToTal }
            val batteryDischarge = valuesAtTime.values.firstOrNull { it.type == ReportVariable.DischargeEnergyToTal }

            if (grid != null && feedIn != null && loads != null && batteryCharge != null && batteryDischarge != null) {
                val approximations = approximationsCalculator.calculateApproximations(
                    grid = grid.graphValue,
                    feedIn = feedIn.graphValue,
                    loads = loads.graphValue,
                    batteryCharge = batteryCharge.graphValue,
                    batteryDischarge = batteryDischarge.graphValue,
                )

                approximations.netSelfSufficiencyEstimateValue?.let {
                    entries.add(
                        StatsGraphValue(
                            type = ReportVariable.SelfSufficiency,
                            graphPoint = graphPoint,
                            graphValue = it
                        )
                    )
                }
            }
        }

        return entries
    }

    fun updateApproximationsFromSelectedValues(context: Context) {
        val selectedValue = valuesAtTimeStream.value.firstOrNull() ?: return
        val valuesAtTime = ValuesAtTime(values = rawData.filter { it.graphPoint == selectedValue.x.toInt() })

        val grid = valuesAtTime.values.firstOrNull { it.type == ReportVariable.GridConsumption }
        val feedIn = valuesAtTime.values.firstOrNull { it.type == ReportVariable.FeedIn }
        val loads = valuesAtTime.values.firstOrNull { it.type == ReportVariable.Loads }
        val batteryCharge = valuesAtTime.values.firstOrNull { it.type == ReportVariable.ChargeEnergyToTal }
        val batteryDischarge = valuesAtTime.values.firstOrNull { it.type == ReportVariable.DischargeEnergyToTal }

        if (maxIndex == selectedValue.x && lastSelectedIndex != selectedValue.x) {
            val effect = VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(effect)
        }

        lastSelectedIndex = selectedValue.x

        if (grid != null && feedIn != null && loads != null && batteryCharge != null && batteryDischarge != null) {
            val approximations = approximationsCalculator.calculateApproximations(
                grid = grid.graphValue,
                feedIn = feedIn.graphValue,
                loads = loads.graphValue,
                batteryCharge = batteryCharge.graphValue,
                batteryDischarge = batteryDischarge.graphValue,
            )

            approximationsViewModelStream.value = approximations
        }
    }
}

data class ValuesAtTime<T>(val values: List<T>)

interface GraphVariable {
    val enabled: Boolean

    @Composable
    fun colour(themeStream: MutableStateFlow<AppTheme>): Color
}

@Composable
fun title(usage: ValueUsage): String {
    return when (usage) {
        ValueUsage.SNAPSHOT -> stringResource(R.string.power)
        ValueUsage.TOTAL -> stringResource(R.string.energy)
    }
}

@Suppress("EnumEntryName")
enum class ReportType {
    day,
    month,
    year,
}

class StatsChartEntry(
    val periodDescription: String,
    override val x: Float,
    override val y: Float,
    val type: ReportVariable
) : ChartEntry {
    override fun withY(y: Float): ChartEntry = StatsChartEntry(
        periodDescription = periodDescription,
        x = x,
        y = y,
        type = type
    )
}
