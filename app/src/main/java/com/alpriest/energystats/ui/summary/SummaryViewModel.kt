package com.alpriest.energystats.ui.summary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.helpers.monthYearString
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.QueryDate
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.SummaryDateRange
import com.alpriest.energystats.shared.models.network.OpenReportResponse
import com.alpriest.energystats.shared.models.network.OpenReportResponseData
import com.alpriest.energystats.shared.models.network.ReportType
import com.alpriest.energystats.shared.models.parse
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.shared.ui.roundedToString
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.concurrent.CancellationException
import kotlin.math.abs

class SummaryTabViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SummaryTabViewModel(network, configManager, application) as T
    }
}


class SummaryTabViewModel(
    private val networking: Networking,
    private val configManager: ConfigManaging,
    application: Application
) : AndroidViewModel(application), AlertDialogMessageProviding {
    private var latestDataDate = ""
    private val approximationsCalculator = ApproximationsCalculator(configManager, networking)
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val loadStateStream = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val summaryDateRangeStream = MutableStateFlow(configManager.summaryDateRange)
    private var solarGenerationByMonth: MutableList<SolarGenerationPeriodAmount> = mutableListOf()

    private var grouping: TimeGrouping = TimeGrouping.MONTH

    private val _viewDataStream: MutableStateFlow<SummaryViewData?> = MutableStateFlow(null)
    val viewDataStream: StateFlow<SummaryViewData?> = _viewDataStream

    suspend fun load() {
        if (viewDataStream.value != null) {
            return
        }

        loadStateStream.value = UiLoadState(LoadState.Active.Loading)
        configManager.currentDevice.value?.let { device ->
            solarGenerationByMonth = mutableListOf()
            val (totals, oldestDataDate) = fetchAllYears(device)
            makeApproximationsViewModel(totals)?.let { model ->
                val financialData: SummaryViewData.FinancialData? = model.financialModel?.let { financialModel ->
                    SummaryViewData.FinancialData(
                        exportIncome = financialModel.exportIncome.amount,
                        gridImportAvoided = financialModel.solarSaving.amount,
                        totalBenefit = financialModel.total.amount,
                        payback = SummaryViewData.PaybackData.create(
                            financialModel.payback(
                                installDate = oldestDataDate
                            )?.monthsRemaining,
                            purchasePrice = configManager.installationPurchasePrice.roundedToString(
                                decimalPlaces = 0,
                                currencySymbol = configManager.currencySymbol
                            ),
                            oldestDataDate = oldestDataDate
                        )
                    )
                }

                val bestSolarData: SummaryViewData.BestSolarData? = findBest(grouping, solarGenerationByMonth)

                _viewDataStream.value = SummaryViewData(
                    solar = model.totalsViewModel?.solar,
                    homeUsage = model.totalsViewModel?.loads,
                    financialData = financialData,
                    bestSolar = bestSolarData,
                    hasPV = device.hasPV,
                    oldestDataDate = oldestDataDate.monthYearString(),
                    latestDataDate = latestDataDate,
                    currencySymbol = configManager.currencySymbol
                )
            }
        }

        viewModelScope.launch {
            configManager.appSettingsStream
                .map { it.deductInverterConsumptionFromGridAvoided }
                .distinctUntilChanged()
                .drop(1)
                .collect { it ->
                    _viewDataStream.value = null
                    load()
                }
        }

        loadStateStream.value = UiLoadState(LoadState.Inactive)
    }

    fun setDateRange(dateRange: SummaryDateRange) {
        viewModelScope.launch {
            configManager.summaryDateRange = dateRange
            summaryDateRangeStream.value = dateRange
            _viewDataStream.value = null
            load()
        }
    }

    fun toggleBestSolarGrouping() {
        val viewData = _viewDataStream.value ?: return

        grouping = when (grouping) {
            TimeGrouping.MONTH -> TimeGrouping.YEAR
            TimeGrouping.YEAR -> TimeGrouping.MONTH
        }

        _viewDataStream.value = viewData.copy(
            bestSolar = findBest(grouping, solarGenerationByMonth)
        )
    }

    private fun findBest(grouping: TimeGrouping, periods: List<SolarGenerationPeriodAmount>): SummaryViewData.BestSolarData? {
        val filteredPeriods = periods.filter { it.amount > 0.0 }.removingExtremeOutliers()

        when (grouping) {
            TimeGrouping.MONTH -> {
                val maxMonth = filteredPeriods.maxByOrNull { it.amount }
                maxMonth?.let { period ->
                    val formatter = DateTimeFormatter.ofPattern("MMMM, yyyy")
                    val date = LocalDate.of(
                        period.year,
                        period.month,
                        1
                    )

                    return SummaryViewData.BestSolarData(
                        description = date.format(formatter),
                        amount = period.amount,
                        period = grouping
                    )
                }
            }

            TimeGrouping.YEAR -> {
                val amountsByYear = filteredPeriods
                    .groupBy { it -> it.year }
                    .map { group ->
                        SolarGenerationPeriodAmount(
                            year = group.key,
                            month = 1,
                            amount = group.value.map { it.amount }.reduce { acc, number -> acc + number }
                        )
                    }
                val maxYear = amountsByYear.maxByOrNull { it.amount }
                maxYear?.let {
                    return SummaryViewData.BestSolarData(
                        description = it.year.toString(),
                        amount = it.amount,
                        period = grouping
                    )
                }
            }
        }

        return null
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
                is SummaryDateRange.Automatic -> application.getString(R.string.present)
                is SummaryDateRange.Manual -> application.getString(R.string.manually_selected, dateRange.to.monthYearString())
            }
        }

    private suspend fun fetchAllYears(device: Device): Pair<Map<ReportVariable, Double>, LocalDate> {
        val totals = mutableMapOf<ReportVariable, Double>()
        var hasFinished = false
        latestDataDate = toDateDescription
        var oldestDataDate = when (val dateRange = configManager.summaryDateRange) {
            is SummaryDateRange.Automatic -> LocalDate.MIN
            is SummaryDateRange.Manual -> dateRange.from
        }
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        for (year in (fromYear..toYear).reversed()) {
            if (hasFinished) {
                break
            }

            try {
                val (yearlyTotals, emptyMonth) = fetchYear(year, device)

                emptyMonth?.let { month ->
                    oldestDataDate = when (val dateRange = configManager.summaryDateRange) {
                        is SummaryDateRange.Automatic -> LocalDate.of(year, month, 1).plusMonths(1)
                        is SummaryDateRange.Manual -> dateRange.from
                    }

                    if (toYear != currentYear) {
                        if (year < toYear) {
                            hasFinished = true
                        }
                    } else {
                        hasFinished = true
                    }
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

        return Pair(totals, oldestDataDate)
    }

    private suspend fun fetchYear(year: Int, device: Device): Pair<Map<ReportVariable, Double>, Int?> {
        val reportVariables = listOf(
            ReportVariable.FeedIn,
            ReportVariable.Generation,
            ReportVariable.ChargeEnergyToTal,
            ReportVariable.DischargeEnergyToTal,
            ReportVariable.GridConsumption,
            ReportVariable.Loads,
            ReportVariable.PvEnergyToTal
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

            totals[reportVariable] = reportResponse.values.sumOf { abs(it.value) }
        }

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // In Java Calendar, January is 0
        var emptyMonth: Int? = null
        for (month in 12 downTo 1) {
            var monthlyTotal = 0.0

            reportVariables.forEach { variable ->
                val report = reports.firstOrNull { it.variable == variable.networkTitle() }
                report?.values?.firstOrNull { it.index == month }?.value?.let {
                    monthlyTotal += it

                    if (report.variable == ReportVariable.PvEnergyToTal.networkTitle()) {
                        solarGenerationByMonth.add(SolarGenerationPeriodAmount(year = year, month = month, amount = it))
                    }
                }
            }

            if (monthlyTotal == 0.0 && ((month < currentMonth && year == currentYear) || month < currentMonth || year < currentYear)) {
                emptyMonth = month
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
                                year == dateRange.from.year && reportData.index < (dateRange.from.monthValue) -> {
                                    OpenReportResponseData(index = reportData.index, value = 0.0)
                                }

                                year == dateRange.to.year && reportData.index > (dateRange.to.monthValue) -> {
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
        val solar = totals[ReportVariable.PvEnergyToTal]

        if (grid == null || feedIn == null || loads == null || batteryCharge == null || batteryDischarge == null || solar == null) {
            return null
        }

        return approximationsCalculator.calculateApproximations(
            grid = grid,
            feedIn = feedIn,
            loads = loads,
            batteryCharge = batteryCharge,
            batteryDischarge = batteryDischarge,
            solar = solar
        )
    }
}

private fun List<SolarGenerationPeriodAmount>.removingExtremeOutliers(): List<SolarGenerationPeriodAmount> {
    return if (count() > 0) {
        val values = map { it.amount }
        val total = values.reduce { acc, number -> acc + number }
        val average = total / count().toDouble()

        filter {
            it.amount < (average * 3.0)
        }
    } else {
        this
    }
}

data class SolarGenerationPeriodAmount(
    val year: Int,
    val month: Int,
    val amount: Double
)
