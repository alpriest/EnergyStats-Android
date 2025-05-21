package com.alpriest.energystats.ui.summary

import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.parse
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.EnergyStatsFinancialModel
import com.alpriest.energystats.ui.flow.TotalsViewModel
import com.alpriest.energystats.ui.statsgraph.AbsoluteSelfSufficiencyCalculator
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import com.alpriest.energystats.ui.statsgraph.NetSelfSufficiencyCalculator
import com.alpriest.energystats.ui.statsgraph.ReportType

class ApproximationsCalculator(
    private val configManager: ConfigManaging,
    private val networking: Networking
) {
    fun calculateApproximations(
        grid: Double,
        feedIn: Double,
        loads: Double,
        batteryCharge: Double,
        batteryDischarge: Double,
        solar: Double
    ): ApproximationsViewModel {
        val totalsViewModel = TotalsViewModel(grid, feedIn, loads, solar, ct2 = 0.0) // ApproximationsCalculator is always called from places where we're looking at historical data, and ct2 isn't available)

        val financialModel = EnergyStatsFinancialModel(totalsViewModel, configManager)

        val netResult = NetSelfSufficiencyCalculator().calculate(
            grid,
            feedIn,
            loads,
            batteryCharge,
            batteryDischarge
        )

        val absoluteResult = AbsoluteSelfSufficiencyCalculator().calculate(
            loads,
            grid
        )

        return ApproximationsViewModel(
            netSelfSufficiencyEstimateValue = netResult.first,
            netSelfSufficiencyEstimate = "${netResult.first}%",
            netSelfSufficiencyEstimateCalculationBreakdown = netResult.second,
            absoluteSelfSufficiencyEstimateValue = absoluteResult.first,
            absoluteSelfSufficiencyEstimate = "${absoluteResult.first}%",
            absoluteSelfSufficiencyEstimateCalculationBreakdown = absoluteResult.second,
            financialModel = financialModel,
            homeUsage = loads,
            totalsViewModel = totalsViewModel
        )
    }

    suspend fun generateTotals(
        deviceSN: String,
        reportData: List<OpenReportResponse>,
        reportType: ReportType,
        queryDate: QueryDate? = null,
        reportVariables: List<ReportVariable>
    ): MutableMap<ReportVariable, Double> {
        val totals = mutableMapOf<ReportVariable, Double>()

        if (reportType == ReportType.day && queryDate != null) {
            val reports = networking.fetchReport(deviceSN, reportVariables, queryDate, ReportType.month)
            reports.forEach { response ->
                ReportVariable.parse(response.variable).let {
                    totals[it] = response.values.firstOrNull { it.index == queryDate.day }?.value ?: 0.0
                }
            }
        } else {
            reportData.forEach { response ->
                ReportVariable.parse(response.variable).let {
                    totals[it] = response.values.sumOf { kotlin.math.abs(it.value) }
                }
            }
        }

        return totals
    }
}