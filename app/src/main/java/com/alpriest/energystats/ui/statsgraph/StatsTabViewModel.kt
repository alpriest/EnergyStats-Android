package com.alpriest.energystats.ui.statsgraph

import android.content.Context
import android.net.Uri
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
import com.alpriest.energystats.ui.paramsgraph.writeContentToUri
import com.alpriest.energystats.ui.summary.ApproximationsCalculator
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.time.LocalDate

data class StatsGraphValue(val graphPoint: Int, val value: Double, val type: ReportVariable)

class StatsTabViewModel(
    val configManager: ConfigManaging,
    private val networking: Networking,
    val onWriteTempFile: (String, String) -> Uri?
) : ViewModel(), ExportProviding, AlertDialogMessageProviding {
    var chartColorsStream = MutableStateFlow(listOf<Color>())
    val producer: ChartEntryModelProducer = ChartEntryModelProducer()
    val displayModeStream = MutableStateFlow<StatsDisplayMode>(StatsDisplayMode.Day(LocalDate.now()))
    val graphVariablesStream = MutableStateFlow<List<StatsGraphVariable>>(listOf())
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
            ReportVariable.Loads
        ).mapNotNull { it }.map {
            StatsGraphVariable(it, true)
        }
    }

    suspend fun load(context: Context) {
        val device = configManager.currentDevice.value ?: return
        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))
        if (graphVariablesStream.value.isEmpty()) {
            updateGraphVariables(device)
        }
        val graphVariables = graphVariablesStream.value
        val displayMode = displayModeStream.value
        val reportVariables = graphVariables.map { it.type }

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

            rawData = updatedData
            totalsStream.value = totals
            refresh()
            calculateSelfSufficiencyEstimate()
            uiState.value = UiLoadState(LoadState.Inactive)
        } catch (ex: Exception) {
            alertDialogMessage.value = MonitorAlertDialogData(ex, ex.localizedMessage)
            uiState.value = UiLoadState(LoadState.Inactive)
            return
        }
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
            listOf(it.type.networkTitle(), it.graphPoint, it.value.toString()).joinToString(",")
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
                val month = dateFormatSymbols.months[displayMode.month]
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
        val grouped = rawData.filter { !hiddenVariables.contains(it.type) }.groupBy { it.type }
        val entries = grouped
            .map { group ->
                group.value.map {
                    StatsChartEntry(
                        x = it.graphPoint.toFloat(),
                        y = it.value.toFloat(),
                        type = it.type,
                    )
                }.toList()
            }.toList()

        chartColorsStream.value = grouped
            .map { it.key.colour() }

        producer.setEntries(entries)
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
}

interface GraphVariable {
    val enabled: Boolean
    val colour: Color
}

@Composable
fun title(usage: ValueUsage): String {
    return when (usage) {
        ValueUsage.SNAPSHOT -> stringResource(R.string.power)
        ValueUsage.TOTAL -> stringResource(R.string.energy)
    }
}

enum class ReportType {
    day,
    month,
    year,
}

data class StatsChartEntry(
    override val x: Float,
    override val y: Float,
    val type: ReportVariable
) : ChartEntry {

    override fun withY(y: Float): ChartEntry = StatsChartEntry(
        x = x,
        y = y,
        type = type
    )
}
