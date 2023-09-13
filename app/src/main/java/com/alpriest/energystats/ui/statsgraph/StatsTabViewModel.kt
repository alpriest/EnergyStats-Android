package com.alpriest.energystats.ui.statsgraph

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.models.ReportResponse
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ValueUsage
import com.alpriest.energystats.models.parse
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.AppLifecycleObserver
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariable
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.Math.max
import java.text.DateFormatSymbols
import java.time.LocalDate

data class StatsGraphValue(val graphPoint: Int, val value: Double, val type: ReportVariable)

class StatsTabViewModel(
    val configManager: ConfigManaging,
    val networking: Networking,
    val onWriteTempFile: (String, String) -> Uri?
) : ViewModel() {
    var chartColorsStream = MutableStateFlow(listOf<Color>())
    val producer: ChartEntryModelProducer = ChartEntryModelProducer()
    val displayModeStream = MutableStateFlow<StatsDisplayMode>(StatsDisplayMode.Day(LocalDate.now()))
    val graphVariablesStream = MutableStateFlow<List<StatsGraphVariable>>(listOf())
    var rawData: List<StatsGraphValue> = listOf()
    var totalsStream: MutableStateFlow<MutableMap<ReportVariable, Double>> = MutableStateFlow(mutableMapOf())
    var exportFileUri: Uri? = null
    var netSelfSufficiencyEstimationStream = MutableStateFlow<String?>(null)
    var absoluteSelfSufficiencyEstimationStream = MutableStateFlow<String?>(null)
    var homeUsageStream = MutableStateFlow<Double?>(null)
    var totalSolarGeneratedStream = MutableStateFlow<Double?>(null)

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

    suspend fun load() {
        val device = configManager.currentDevice.value ?: return
        if (graphVariablesStream.value.isEmpty()) {
            updateGraphVariables(device)
        }
        val graphVariables = graphVariablesStream.value

        val displayMode = displayModeStream.value

        val queryDate = makeQueryDate(displayMode)
        val reportType = makeReportType(displayMode)
        val reportVariables = graphVariables.map { it.type }

        val reportData = networking.fetchReport(
            device.deviceID,
            variables = reportVariables,
            queryDate = queryDate,
            reportType = reportType
        )

        val rawTotals = generateTotals(device.deviceID, reportData, reportType, queryDate, reportVariables)

        rawData = reportData.flatMap { reportResponse ->
            val reportVariable = ReportVariable.parse(reportResponse.variable)

            return@flatMap reportResponse.data.map { dataPoint ->
                val graphPoint: Int = when (displayMode) {
                    is StatsDisplayMode.Day -> {
                        dataPoint.index - 1
                    }

                    is StatsDisplayMode.Month -> {
                        dataPoint.index
                    }

                    is StatsDisplayMode.Year -> {
                        dataPoint.index
                    }
                }

                return@map StatsGraphValue(
                    graphPoint = graphPoint,
                    value = dataPoint.value,
                    type = reportVariable
                )
            }
        }

        totalsStream.value = rawTotals
        refresh()
        calculateSelfSufficiencyEstimate()
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
            listOf(it.type.networkTitle(), it.graphPoint, it.value.toString()).joinToString(",")
        }

        val exportText = (listOf(headers) + rows).joinToString(separator = "\n")
        val exportFileName: String

        when (displayMode) {
            is StatsDisplayMode.Day -> {
                val date = displayMode.date

                val year = date.year
                val month = date.month.name
                val day = date.dayOfMonth

                exportFileName = "energystats_${year}_${month}_$day"
            }

            is StatsDisplayMode.Month -> {
                val dateFormatSymbols = DateFormatSymbols.getInstance()
                val month = dateFormatSymbols.months[displayMode.month]
                val year = displayMode.year
                exportFileName = "energystats_${year}_$month"
            }

            is StatsDisplayMode.Year -> {
                val year = displayMode.year
                exportFileName = "energystats_$year"
            }
        }

        exportFileUri = onWriteTempFile(exportFileName, exportText)
    }

    private suspend fun generateTotals(
        deviceID: String,
        reportData: ArrayList<ReportResponse>,
        reportType: ReportType,
        queryDate: QueryDate,
        reportVariables: List<ReportVariable>
    ): MutableMap<ReportVariable, Double> {
        val totals = mutableMapOf<ReportVariable, Double>()

        if (reportType == ReportType.day) {
            val reports = networking.fetchReport(deviceID, reportVariables, queryDate, ReportType.month)
            reports.forEach { response ->
                ReportVariable.parse(response.variable).let {
                    totals[it] = response.data.first { it.index == queryDate.day }.value
                }
            }
        } else {
            reportData.forEach { response ->
                ReportVariable.parse(response.variable).let {
                    totals[it] = response.data.sumOf { kotlin.math.abs(it.value) }
                }
            }
        }

        return totals
    }

    private fun refresh() {
        val hiddenVariables = graphVariablesStream.value.filter { !it.enabled }.map { it.type }
        val grouped = rawData.filter { !hiddenVariables.contains(it.type) }.groupBy { it.type }
        val entries = grouped
            .map { group ->
                group.value.map {
                    return@map StatsChartEntry(
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

    private fun makeQueryDate(displayMode: StatsDisplayMode): QueryDate {
        return when (displayMode) {
            is StatsDisplayMode.Day -> {
                val date = displayMode.date
                QueryDate(
                    year = date.year,
                    month = date.monthValue,
                    day = date.dayOfMonth
                )
            }

            is StatsDisplayMode.Month -> {
                QueryDate(year = displayMode.year, month = displayMode.month + 1, day = null)
            }

            is StatsDisplayMode.Year -> {
                QueryDate(year = displayMode.year, month = null, day = null)
            }
        }
    }

    private fun makeReportType(displayMode: StatsDisplayMode): ReportType {
        return when (displayMode) {
            is StatsDisplayMode.Day -> ReportType.day
            is StatsDisplayMode.Month -> ReportType.month
            is StatsDisplayMode.Year -> ReportType.year
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

        val generation = totals[ReportVariable.Generation]
        val feedIn = totals[ReportVariable.FeedIn]
        val grid = totals[ReportVariable.GridConsumption]
        val batteryCharge = totals[ReportVariable.ChargeEnergyToTal]
        val batteryDischarge = totals[ReportVariable.DischargeEnergyToTal]
        val loads = totals[ReportVariable.Loads]

        if (generation == null || feedIn == null || grid == null || batteryCharge == null || batteryDischarge == null || loads == null) {
            return
        }

        homeUsageStream.value = loads
        totalSolarGeneratedStream.value = max(0.0, (batteryCharge - batteryDischarge - grid + loads + feedIn))

        val netResult = NetSelfSufficiencyCalculator().calculate(
            loads,
            grid
        )
        netSelfSufficiencyEstimationStream.value = "${netResult}%"

        val absoluteResult = AbsoluteSelfSufficiencyCalculator().calculate(
            grid,
            feedIn,
            loads,
            batteryCharge,
            batteryDischarge
        )
        absoluteSelfSufficiencyEstimationStream.value = "${absoluteResult}%"
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
