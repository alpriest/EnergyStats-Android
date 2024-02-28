package com.alpriest.energystats.ui.summary

import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.EnergyStatsFinancialModel
import com.alpriest.energystats.ui.flow.TotalsViewModel
import com.alpriest.energystats.ui.statsgraph.AbsoluteSelfSufficiencyCalculator
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import com.alpriest.energystats.ui.statsgraph.NetSelfSufficiencyCalculator

class ApproximationsCalculator(
    private val configManager: ConfigManaging
) {
    fun calculateApproximations(
        grid: Double,
        feedIn: Double,
        loads: Double,
        batteryCharge: Double,
        batteryDischarge: Double
    ): ApproximationsViewModel {
        val totalsViewModel = TotalsViewModel(grid, feedIn, loads, batteryCharge, batteryDischarge)

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
            netSelfSufficiencyEstimate = "${netResult.first}%",
            netSelfSufficiencyEstimateCalculationBreakdown = netResult.second,
            absoluteSelfSufficiencyEstimate = "${absoluteResult.first}%",
            absoluteSelfSufficiencyEstimateCalculationBreakdown = absoluteResult.second,
            financialModel = financialModel,
            homeUsage = loads,
            totalsViewModel = totalsViewModel
        )
    }
}