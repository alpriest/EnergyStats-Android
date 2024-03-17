package com.alpriest.energystats.ui.summary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.parse
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import com.alpriest.energystats.ui.statsgraph.ReportType
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
    private val approximationsCalculator = ApproximationsCalculator(configManager, networking)
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val loadStateStream = MutableStateFlow(UiLoadState(LoadState.Inactive))

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
                val (yearlyTotals, emptyMonth) = fetchYear(year, device)

                emptyMonth?.let { month ->
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month - 1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    val date = calendar.time
                    val dateFormatter = SimpleDateFormat("MMMM y", Locale.getDefault())
                    oldestDataDate.value = dateFormatter.format(date)
                    hasFinished = true
                }

                yearlyTotals.forEach { (variable, value) ->
                    totals[variable] = (totals[variable] ?: 0.0) + value
                }
            } catch (ex: CancellationException) {
                // Ignore as the user navigated away
            } catch (ex: Exception) {
                hasFinished = true
                alertDialogMessage.value = MonitorAlertDialogData(ex, ex.localizedMessage)
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
        val reports = networking.fetchReport(deviceSN = device.deviceSN,
            variables = reportVariables,
            queryDate = QueryDate(year, null, null),
            reportType = ReportType.year
        )

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

        return approximationsCalculator.calculateApproximations(grid = grid,
            feedIn = feedIn,
            loads = loads,
            batteryCharge = batteryCharge,
            batteryDischarge = batteryDischarge)
    }
}