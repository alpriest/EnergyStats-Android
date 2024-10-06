package com.alpriest.energystats.ui.summary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.OpenReportResponseData
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.parse
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.paramsgraph.monthYear
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import com.alpriest.energystats.ui.statsgraph.ReportType
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.util.Calendar
import java.util.concurrent.CancellationException

class SummaryTabViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SummaryTabViewModel(network, configManager) as T
    }
}

class SummaryTabViewModel(
    private val networking: Networking,
    private val configManager: ConfigManaging,
) : ViewModel(), AlertDialogMessageProviding {
    val approximationsViewModelStream = MutableStateFlow<ApproximationsViewModel?>(null)
    val oldestDataDate = MutableStateFlow("")
    val latestDataDate = MutableStateFlow("")
    private val approximationsCalculator = ApproximationsCalculator(configManager, networking)
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val loadStateStream = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val summaryDateRangeStream = MutableStateFlow<SummaryDateRange>(SummaryDateRange.Automatic)

    suspend fun load(context: Context) {
        if (approximationsViewModelStream.value != null) {
            return
        }

        loadStateStream.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))
        configManager.currentDevice.value?.let {
            val totals = fetchAllYears(it)
            approximationsViewModelStream.value = makeApproximationsViewModel(totals = totals)
        }
        loadStateStream.value = UiLoadState(LoadState.Inactive)
    }

    suspend fun setDateRange(dateRange: SummaryDateRange, context: Context) {
        configManager.summaryDateRange = dateRange
        summaryDateRangeStream.value = dateRange
        approximationsViewModelStream.value = null
        load(context)
    }

    private val fromYear: Int
        get() {
            return when (val dateRange = configManager.summaryDateRange) {
                is SummaryDateRange.Automatic -> 2020
                is SummaryDateRange.Manual -> dateRange.from.year
            }
        }

    private val toYear: Int
        get() {
            return when (val dateRange = configManager.summaryDateRange) {
                is SummaryDateRange.Automatic -> Calendar.getInstance().get(Calendar.YEAR)
                is SummaryDateRange.Manual -> dateRange.to.year
            }
        }

    private val toDateDescription: String
        get() {
            return when (val dateRange = configManager.summaryDateRange) {
                is SummaryDateRange.Automatic -> "present"
                is SummaryDateRange.Manual -> "${dateRange.to.monthYear()} (manually selected)"
            }
        }

    private suspend fun fetchAllYears(device: Device): Map<ReportVariable, Double> {
        val totals = mutableMapOf<ReportVariable, Double>()
        var hasFinished = false
        latestDataDate.value = toDateDescription

        for (year in (fromYear..toYear).reversed()) {
            if (hasFinished) {
                break
            }

            try {
                val (yearlyTotals, emptyMonth) = fetchYear(year, device)

                emptyMonth?.let { month ->
                    oldestDataDate.value = when (val dateRange = configManager.summaryDateRange) {
                        is SummaryDateRange.Automatic -> LocalDate.of(year, month - 1, 1).monthYear()
                        is SummaryDateRange.Manual -> "$dateRange.from.year"
                    }
                    hasFinished = true
                }

                yearlyTotals.forEach { (variable, value) ->
                    totals[variable] = (totals[variable] ?: 0.0) + value
                }
            } catch (ex: CancellationException) {
                // Ignore as the user navigated away
            } catch (ex: Exception) {
                hasFinished = true
                alertDialogMessage.value = MonitorAlertDialogData(ex, ex.toString())
            }
        }

        return totals
    }

    private suspend fun fetchYear(year: Int, device: Device): Pair<Map<ReportVariable, Double>, Int?> {
        val reportVariables = listOf(
            ReportVariable.FeedIn,
            ReportVariable.Generation,
            ReportVariable.ChargeEnergyToTal,
            ReportVariable.DischargeEnergyToTal,
            ReportVariable.GridConsumption,
            ReportVariable.Loads
        )
        val rawReports = networking.fetchReport(
            deviceSN = device.deviceSN,
            variables = reportVariables,
            queryDate = QueryDate(year, null, null),
            reportType = ReportType.year
        )
        val reports = filterUnrequestedMonths(year, rawReports)

        val totals = mutableMapOf<ReportVariable, Double>()
        reports.forEach { reportResponse ->
            val reportVariable = ReportVariable.parse(reportResponse.variable)

            totals[reportVariable] = reportResponse.values.sumOf { kotlin.math.abs(it.value) }
        }

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // In Java Calendar, January is 0
        var emptyMonth: Int? = null
        for (month in 12 downTo 1) {
            var monthlyTotal = 0.0

            reportVariables.forEach { variable ->
                reports.firstOrNull { it.variable == variable.networkTitle() }?.values?.firstOrNull { it.index == month }?.value?.let {
                    monthlyTotal += it
                }
            }

            if (monthlyTotal == 0.0 && (month < currentMonth || year < currentYear)) {
                emptyMonth = month + 1
                break
            }
        }

        return Pair(totals, emptyMonth)
    }

    private fun filterUnrequestedMonths(year: Int, reports: List<OpenReportResponse>): List<OpenReportResponse> {
        return when (val dateRange = configManager.summaryDateRange) {
            is SummaryDateRange.Automatic -> reports
            is SummaryDateRange.Manual -> {
                reports.map { report ->
                    OpenReportResponse(
                        variable = report.variable,
                        unit = report.unit,
                        values = report.values.map { reportData ->
                            when {
                                year == dateRange.from.year && reportData.index < dateRange.from.month.value -> {
                                    OpenReportResponseData(index = reportData.index, value = 0.0)
                                }
                                year == dateRange.to.year && reportData.index > dateRange.to.month.value -> {
                                    OpenReportResponseData(index = reportData.index, value = 0.0)
                                }
                                else -> {
                                    OpenReportResponseData(index = reportData.index, value = reportData.value)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun makeApproximationsViewModel(
        totals: Map<ReportVariable, Double>
    ): ApproximationsViewModel? {
        val grid = totals[ReportVariable.GridConsumption]
        val feedIn = totals[ReportVariable.FeedIn]
        val loads = totals[ReportVariable.Loads]
        val batteryCharge = totals[ReportVariable.ChargeEnergyToTal]
        val batteryDischarge = totals[ReportVariable.DischargeEnergyToTal]

        if (grid == null || feedIn == null || loads == null || batteryCharge == null || batteryDischarge == null) {
            return null
        }

        return approximationsCalculator.calculateApproximations(
            grid = grid,
            feedIn = feedIn,
            loads = loads,
            batteryCharge = batteryCharge,
            batteryDischarge = batteryDischarge
        )
    }
}