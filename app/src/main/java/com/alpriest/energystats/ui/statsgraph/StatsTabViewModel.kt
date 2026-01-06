package com.alpriest.energystats.ui.statsgraph

import android.content.Context
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import com.alpriest.energystats.helpers.isSameDay
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.ValueUsage
import com.alpriest.energystats.parseToLocalDateTime
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.AppLifecycleObserver
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.ExportProviding
import com.alpriest.energystats.ui.paramsgraph.LastLoadState
import com.alpriest.energystats.ui.paramsgraph.writeContentToUri
import com.alpriest.energystats.shared.models.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.statsgraph.StatsDisplayMode.Day
import com.alpriest.energystats.ui.summary.ApproximationsCalculator
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.math.max

data class StatsGraphViewData(
    val stats: Map<ReportVariable, List<StatsChartEntry>>,
    val batterySOC: List<StatsChartEntry>,
    val inverterUsage: List<StatsChartEntry>,
    val selfSufficiency: List<StatsChartEntry>
)

class StatsTabViewModel(
    val displayModeStream: MutableStateFlow<StatsDisplayMode>,
    val configManager: ConfigManaging,
    private val networking: Networking,
    val themeStream: MutableStateFlow<AppTheme>,
    val onWriteTempFile: (String, String) -> Uri?
) : ViewModel(), ExportProviding, AlertDialogMessageProviding {
    val graphVariablesStream = MutableStateFlow<List<StatsGraphVariable>>(listOf())
    var totalsStream: MutableStateFlow<Map<ReportVariable, Double>> = MutableStateFlow(mutableMapOf())
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

    private val _viewDataStateFlow = MutableStateFlow(StatsGraphViewData(mapOf(), listOf(), listOf(), listOf()))
    val viewDataStateFlow = _viewDataStateFlow.asStateFlow()
    val valuesAtTimeStream = MutableStateFlow<Map<ReportVariable, List<StatsChartEntry>>>(emptyMap())
    var selectedValueStream = MutableStateFlow<StatsGraphLineMarkerModel?>(null)

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

        viewModelScope.launch {
            themeStream.collect {
                configManager.currentDevice.value?.let {
                    updateGraphVariables(it)
                }
            }
        }

        viewModelScope.launch {
            selectedValueStream.collect { selectedValue ->
                if (selectedValue == null) {
                    valuesAtTimeStream.value = emptyMap()
                } else {
                    val viewData = _viewDataStateFlow.value
                    val statsAtTime = viewData.stats
                        .mapValues { (_, entries) ->
                            val nearest = entries.minByOrNull { entry -> abs(entry.x - selectedValue.x) }
                            nearest?.let { listOf(it) } ?: emptyList()
                        }
                        .filterValues { it.isNotEmpty() }

                    val nearestBattery = viewData.batterySOC.minByOrNull { entry -> abs(entry.x - selectedValue.x) }
                    val batterySocAtTime = mapOf(Pair(ReportVariable.BatterySOC, nearestBattery?.let { listOf(it) } ?: emptyList()))

                    val nearestSufficiency = viewData.selfSufficiency.minByOrNull { entry -> abs(entry.x - selectedValue.x) }
                    val sufficiencyAtTime = mapOf(Pair(ReportVariable.SelfSufficiency, nearestSufficiency?.let { listOf(it) } ?: emptyList()))

                    val nearestInverter = viewData.inverterUsage.minByOrNull { entry -> abs(entry.x - selectedValue.x) }
                    val inverterAtTime = mapOf(Pair(ReportVariable.InverterConsumption, nearestInverter?.let { listOf(it) } ?: emptyList()))

                    valuesAtTimeStream.value = statsAtTime + batterySocAtTime + sufficiencyAtTime + inverterAtTime
                }
            }
        }

        viewModelScope.launch {
            displayModeStream.collect { _ ->
                selectedValueStream.value = null
            }
        }
    }

    fun finalize() {
        appLifecycleObserver.detach()
    }

    private fun updateGraphVariables(device: Device) {
        graphVariablesStream.value = listOf(
            if (device.hasPV) ReportVariable.PvEnergyToTal else null,
            ReportVariable.Generation,
            ReportVariable.FeedIn,
            ReportVariable.GridConsumption,
            if (device.hasBattery) ReportVariable.ChargeEnergyToTal else null,
            if (device.hasBattery) ReportVariable.DischargeEnergyToTal else null,
            ReportVariable.Loads,
            if (configManager.showSelfSufficiencyStatsGraphOverlay && configManager.selfSufficiencyEstimateMode != SelfSufficiencyEstimateMode.Off) ReportVariable.SelfSufficiency else null,
            if (configManager.showInverterConsumption) ReportVariable.InverterConsumption else null,
            if (configManager.showBatterySOCOnDailyStats) ReportVariable.BatterySOC else null
        ).mapNotNull { it }.map {
            StatsGraphVariable(it, true)
        }
    }

    suspend fun load() {
        val device = configManager.currentDevice.value ?: return
        if (!requiresLoad()) return

        uiState.value = UiLoadState(LoadState.Active.Loading)
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
            ReportVariable.Loads,
            ReportVariable.PvEnergyToTal
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
                    unit = displayMode.unit
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

            val socGraphData = generateBatterySOC(device, displayMode)

            rawData = updatedData + generateSelfSufficiency(updatedData) + generateInverterConsumption(updatedData) + socGraphData
            totalsStream.value = totals.apply { putAll(totalsForInverterConsumption(rawData)) }
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

    private fun totalsForInverterConsumption(rawData: List<StatsGraphValue>): MutableMap<ReportVariable, Double> {
        val result = mutableMapOf<ReportVariable, Double>()

        result[ReportVariable.InverterConsumption] = rawData.sumOf { if (it.type == ReportVariable.InverterConsumption) it.graphValue else 0.0 }

        return result
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
        if (totalsStream.value.isNotEmpty()) {
            viewModelScope.launch {
                load()
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
            is Day -> {
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
        val statsEntries = rawData
            .filter { it.type != ReportVariable.SelfSufficiency }
            .filter { it.type != ReportVariable.InverterConsumption }
            .filter { it.type != ReportVariable.BatterySOC }
            .filter { !hiddenVariables.contains(it.type) }.groupBy { it.type }
            .mapValues { (_, values) ->
                values.map { value ->
                    StatsChartEntry(
                        periodDescription = periodDescription(value.graphPoint, displayModeStream.value),
                        x = value.graphPoint.toFloat(),
                        y = value.graphValue.toFloat(),
                        type = value.type,
                    )
                }
            }

        val now = LocalDateTime.now(ZoneId.systemDefault())
        val displayMode = displayModeStream.value

        val selfSufficiencyData = rawDataFiltered(ReportVariable.SelfSufficiency, hiddenVariables, displayMode, now)

        val inverterData = rawDataFiltered(ReportVariable.InverterConsumption, hiddenVariables, displayMode, now)

        val batterySOCData = rawDataFiltered(ReportVariable.BatterySOC, hiddenVariables, displayMode, now)

        _viewDataStateFlow.value = StatsGraphViewData(
            stats = statsEntries,
            batterySOC = batterySOCData,
            inverterUsage = inverterData,
            selfSufficiency = selfSufficiencyData
        )

        maxIndex = statsEntries.values.flatten()
            .maxByOrNull { it.y }
            ?.x

        prepareExport(rawData, displayModeStream.value)
    }

    private fun rawDataFiltered(
        type: ReportVariable,
        hiddenVariables: List<ReportVariable>,
        displayMode: StatsDisplayMode,
        now: LocalDateTime
    ): List<StatsChartEntry> {
        return rawData
            .filter { it.type == type }
            .filter { !hiddenVariables.contains(it.type) }
            .filter {
                when (displayMode) {
                    is Day -> {
                        if (displayMode.date == LocalDate.now()) {
                            it.graphPoint <= now.hour
                        } else {
                            true
                        }
                    }

                    is StatsDisplayMode.Month -> it.graphPoint <= now.dayOfMonth
                    is StatsDisplayMode.Year -> it.graphPoint <= now.monthValue
                    else -> true
                }
            }
            .map {
                StatsChartEntry(
                    periodDescription = periodDescription(it.graphPoint, displayModeStream.value),
                    x = it.graphPoint.toFloat(),
                    y = it.graphValue.toFloat(),
                    type = it.type,
                )
            }
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
        val solar = totals[ReportVariable.PvEnergyToTal] ?: 0.0

        approximationsViewModelStream.value = approximationsCalculator.calculateApproximations(
            grid = grid,
            feedIn = feedIn,
            loads = loads,
            batteryCharge = batteryCharge,
            batteryDischarge = batteryDischarge,
            solar = solar
        )
    }

    private suspend fun generateBatterySOC(device: Device, displayMode: StatsDisplayMode): List<StatsGraphValue> {
        return if (configManager.showBatterySOCOnDailyStats) {
            return fetchBatterySOC(device, displayMode)
        } else {
            listOf()
        }
    }

    private suspend fun fetchBatterySOC(
        device: Device,
        mode: StatsDisplayMode
    ): List<StatsGraphValue> {
        return when (mode) {
            is Day -> {
                val zone = ZoneId.systemDefault()
                val startOfDay = mode.date.atStartOfDay()
                val endOfDay = startOfDay.plusDays(1)

                try {
                    // Fetch SoC history for the selected day
                    val response = networking.fetchHistory(
                        deviceSN = device.deviceSN,
                        variables = listOf("SoC"),
                        start = startOfDay.atZone(zone).toEpochSecond() * 1000,
                        end = endOfDay.atZone(zone).toEpochSecond() * 1000
                    )

                    // Group by hour and average values for each hour
                    response.datas
                        .flatMap { it.data }
                        .groupBy { parseToLocalDateTime(it.time).hour }
                        .map { (hour, entries) ->
                            val avgValue = entries.map { it.value.toDouble() }.average()
                            StatsGraphValue(
                                type = ReportVariable.BatterySOC,
                                graphPoint = hour,
                                graphValue = avgValue
                            )
                        }
                        .sortedBy { it.graphPoint }
                } catch (e: Exception) {
                    Log.e("AWP", "fetchBatterySOC failed", e)
                    emptyList()
                }
            }

            else ->
                listOf()
        }
    }

    private fun generateInverterConsumption(rawData: List<StatsGraphValue>): List<StatsGraphValue> {
        return if (configManager.showInverterConsumption) {
            return calculateInverterConsumptionAcrossTimePeriod(rawData)
        } else {
            listOf()
        }
    }

    private fun calculateInverterConsumptionAcrossTimePeriod(rawData: List<StatsGraphValue>): List<StatsGraphValue> {
        val graphPoints = rawData.map { it.graphPoint }.distinct()
        val entries: MutableList<StatsGraphValue> = mutableListOf()

        for (graphPoint in graphPoints) {
            val valuesAtTime = ValuesAtTime(values = rawData.filter { it.graphPoint == graphPoint })

            val grid = valuesAtTime.values.firstOrNull { it.type == ReportVariable.GridConsumption }
            val feedIn = valuesAtTime.values.firstOrNull { it.type == ReportVariable.FeedIn }
            val loads = valuesAtTime.values.firstOrNull { it.type == ReportVariable.Loads }
            val batteryCharge = valuesAtTime.values.firstOrNull { it.type == ReportVariable.ChargeEnergyToTal }
            val batteryDischarge = valuesAtTime.values.firstOrNull { it.type == ReportVariable.DischargeEnergyToTal }
            val solar = valuesAtTime.values.firstOrNull { it.type == ReportVariable.PvEnergyToTal }

            if (grid != null && feedIn != null && loads != null && batteryCharge != null && batteryDischarge != null && solar != null) {
                val inverterConsumption =
                    max((solar.graphValue + grid.graphValue + batteryDischarge.graphValue) - (feedIn.graphValue + batteryCharge.graphValue + loads.graphValue), 0.0)

                entries.add(
                    StatsGraphValue(
                        type = ReportVariable.InverterConsumption,
                        graphPoint = graphPoint,
                        graphValue = inverterConsumption
                    )
                )
            }
        }

        totalsStream.value

        return entries
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
            val solar = valuesAtTime.values.firstOrNull { it.type == ReportVariable.PvEnergyToTal }

            if (grid != null && feedIn != null && loads != null && batteryCharge != null && batteryDischarge != null && solar != null) {
                val approximations = approximationsCalculator.calculateApproximations(
                    grid = grid.graphValue,
                    feedIn = feedIn.graphValue,
                    loads = loads.graphValue,
                    batteryCharge = batteryCharge.graphValue,
                    batteryDischarge = batteryDischarge.graphValue,
                    solar = solar.graphValue
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
        val selectedValue = valuesAtTimeStream.value.values.firstOrNull()?.firstOrNull() ?: return
        val valuesAtTime = ValuesAtTime(values = rawData.filter { it.graphPoint == selectedValue.x.toInt() })

        val grid = valuesAtTime.values.firstOrNull { it.type == ReportVariable.GridConsumption }
        val feedIn = valuesAtTime.values.firstOrNull { it.type == ReportVariable.FeedIn }
        val loads = valuesAtTime.values.firstOrNull { it.type == ReportVariable.Loads }
        val batteryCharge = valuesAtTime.values.firstOrNull { it.type == ReportVariable.ChargeEnergyToTal }
        val batteryDischarge = valuesAtTime.values.firstOrNull { it.type == ReportVariable.DischargeEnergyToTal }
        val solar = valuesAtTime.values.firstOrNull { it.type == ReportVariable.PvEnergyToTal }

        if (maxIndex == selectedValue.x && lastSelectedIndex != selectedValue.x) {
            val effect = VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(effect)
        }

        lastSelectedIndex = selectedValue.x

        if (grid != null && feedIn != null && loads != null && batteryCharge != null && batteryDischarge != null && solar != null) {
            val approximations = approximationsCalculator.calculateApproximations(
                grid = grid.graphValue,
                feedIn = feedIn.graphValue,
                loads = loads.graphValue,
                batteryCharge = batteryCharge.graphValue,
                batteryDischarge = batteryDischarge.graphValue,
                solar = solar.graphValue
            )

            approximationsViewModelStream.value = approximations
        }
    }

    fun clearSelectedValue() {
        selectedValueStream.value = null
        valuesAtTimeStream.value = emptyMap()
    }
}

@Composable
fun title(usage: ValueUsage): String {
    return when (usage) {
        ValueUsage.SNAPSHOT -> stringResource(R.string.power)
        ValueUsage.TOTAL -> stringResource(R.string.energy)
    }
}

class StatsChartEntry(
    val periodDescription: String,
    val x: Float,
    val y: Float,
    val type: ReportVariable
)
