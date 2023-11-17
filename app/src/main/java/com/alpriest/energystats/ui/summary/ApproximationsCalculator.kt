package com.alpriest.energystats.ui.summary

import com.alpriest.energystats.models.EarningsResponse
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.EnergyStatsFinancialModel
import com.alpriest.energystats.ui.flow.TotalsViewModel
import com.alpriest.energystats.ui.settings.FinancialModel
import com.alpriest.energystats.ui.statsgraph.AbsoluteSelfSufficiencyCalculator
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import com.alpriest.energystats.ui.statsgraph.NetSelfSufficiencyCalculator

class ApproximationsCalculator(
    private val network: FoxESSNetworking,
    private val configManager: ConfigManaging
) {
    fun calculateApproximations(
        grid: Double,
        feedIn: Double,
        loads: Double,
        batteryCharge: Double,
        batteryDischarge: Double,
        earnings: EarningsResponse?
    ): ApproximationsViewModel {
        val totalsViewModel = TotalsViewModel(grid, feedIn, loads, batteryCharge, batteryDischarge)

        val financialModel: EnergyStatsFinancialModel? = if (configManager.financialModel == FinancialModel.EnergyStats) {
            EnergyStatsFinancialModel(totalsViewModel, configManager)
        } else {
            null
        }

        val netResult = NetSelfSufficiencyCalculator().calculate(
            loads,
            grid
        )

        val absoluteResult = AbsoluteSelfSufficiencyCalculator().calculate(
            grid,
            feedIn,
            loads,
            batteryCharge,
            batteryDischarge
        )

        return ApproximationsViewModel(
            netSelfSufficiencyEstimate = "${netResult.first}%",
            netSelfSufficiencyEstimateCalculationBreakdown = netResult.second,
            absoluteSelfSufficiencyEstimate = "${absoluteResult.first}%",
            absoluteSelfSufficiencyEstimateCalculationBreakdown = absoluteResult.second,
            financialModel = financialModel,
            earnings = earnings,
            homeUsage = loads,
            totalsViewModel = totalsViewModel
        )
    }
}