package com.alpriest.energystats.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.EarningsResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.parse
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.FinanceAmount
import com.alpriest.energystats.ui.flow.FinanceAmountType
import com.alpriest.energystats.ui.paramsgraph.ToastMessageProviding
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import com.alpriest.energystats.ui.statsgraph.ReportType
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SummaryTabViewModelFactory(
    private val network: FoxESSNetworking,
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SummaryTabViewModel(network, configManager) as T
    }
}

class SummaryTabViewModel(
    private val network: FoxESSNetworking,
    private val configManager: ConfigManaging,
) : ViewModel(), ToastMessageProviding {
    val approximationsViewModelStream = MutableStateFlow<ApproximationsViewModel?>(null)
    val foxESSTotalStream = MutableStateFlow<FinanceAmount?>(null)
    val oldestDataDate = MutableStateFlow("")
    private val approximationsCalculator = ApproximationsCalculator(network, configManager)
    override val toastMessage = MutableStateFlow<String?>(null)

    suspend fun load() {
        if (approximationsViewModelStream.value != null) {
            return
        }

        configManager.currentDevice.value?.let {
            val foxEarnings = network.fetchEarnings(deviceID = it.deviceID)
            foxESSTotalStream.value = FinanceAmount(
                type = FinanceAmountType.TOTAL,
                amount = foxEarnings.cumulate.earnings,
                currencyCode = foxEarnings.currencyCode(),
                currencySymbol = foxEarnings.currencySymbol()
            )

            val totals = fetchAllYears(it)
            approximationsViewModelStream.value = makeApproximationsViewModel(totals = totals, response = foxEarnings)
        }
    }

    private suspend fun fetchAllYears(device: Device): Map<ReportVariable, Double> {
        val totals = mutableMapOf<ReportVariable, Double>()
        val maxYears = 20
        var hasFinished = false

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        for (year in (currentYear - maxYears..currentYear).reversed()) {
            if (hasFinished) {
                break
            }

            try {
                val (yearlyTotals, emptyMonth) = fetchYear(year, device) // Assuming fetchYear is a suspend function

                emptyMonth?.let { month ->
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month - 1) // In Java Calendar, January is 0
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    val date = calendar.time
                    val dateFormatter = SimpleDateFormat("MMMM YYYY", Locale.getDefault())
                    oldestDataDate.value = dateFormatter.format(date)
                    hasFinished = true
                }

                yearlyTotals.forEach { (variable, value) ->
                    totals[variable] = (totals[variable] ?: 0.0) + value
                }
            } catch (ex: Exception) {
                hasFinished = true
                toastMessage.value = ex.localizedMessage
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
        val reports = network.fetchReport(deviceID = device.deviceID,
            variables = reportVariables,
            queryDate = QueryDate(year, null, null),
            reportType = ReportType.year
        )

        val totals = mutableMapOf<ReportVariable, Double>()
        reports.forEach { reportResponse ->
            val reportVariable = ReportVariable.parse(reportResponse.variable) ?: return@forEach

            totals[reportVariable] = reportResponse.data.sumOf { kotlin.math.abs(it.value) }
        }

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // In Java Calendar, January is 0
        var emptyMonth: Int? = null
        for (month in 12 downTo 1) {
            var monthlyTotal = 0.0

            reportVariables.forEach { variable ->
                reports.firstOrNull { it.variable == variable.networkTitle() }?.data?.firstOrNull { it.index == month }?.value?.let {
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

    private fun makeApproximationsViewModel(
        totals: Map<ReportVariable, Double>,
        response: EarningsResponse
    ): ApproximationsViewModel? {
        val grid = totals[ReportVariable.GridConsumption]
        val feedIn = totals[ReportVariable.FeedIn]
        val loads = totals[ReportVariable.Loads]
        val batteryCharge = totals[ReportVariable.ChargeEnergyToTal]
        val batteryDischarge = totals[ReportVariable.DischargeEnergyToTal]

        if (grid == null || feedIn == null || loads == null || batteryCharge == null || batteryDischarge == null) {
            return null
        }

        return approximationsCalculator.calculateApproximations(grid = grid,
            feedIn = feedIn,
            loads = loads,
            batteryCharge = batteryCharge,
            batteryDischarge = batteryDischarge,
            earnings = response)
    }
}