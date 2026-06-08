package com.alpriest.energystats.shared.models

import com.alpriest.energystats.shared.models.network.OpenReportResponse
import com.alpriest.energystats.shared.models.network.OpenReportResponseData
import java.util.Calendar

class TotalsViewModel {
    val grid: Double
    val feedIn: Double
    val loads: Double
    val solar: Double
    val ct2: Double
    val inverterConsumption: Double

    constructor(
        grid: Double,
        feedIn: Double,
        loads: Double,
        solar: Double,
        ct2: Double,
        inverterConsumption: Double
    ) {
        this.grid = grid
        this.feedIn = feedIn
        this.loads = loads
        this.solar = solar
        this.ct2 = ct2
        this.inverterConsumption = inverterConsumption
    }

    constructor(
        reports: List<OpenReportResponse>,
        generationViewModel: GenerationViewModel?
    ) {
        grid = reports.todayValue(forKey = ReportVariable.GridConsumption.networkTitle())
        feedIn = reports.todayValue(forKey = ReportVariable.FeedIn.networkTitle())
        loads = reports.todayValue(forKey = ReportVariable.Loads.networkTitle())
        solar = reports.todayValue(forKey = ReportVariable.PvEnergyToTal.networkTitle())
        ct2 = generationViewModel?.ct2Total ?: 0.0
        val batteryDischarge = reports.todayValue(forKey = ReportVariable.DischargeEnergyToTal.networkTitle())
        val batteryCharge = reports.todayValue(forKey = ReportVariable.ChargeEnergyToTal.networkTitle())
        inverterConsumption = maxOf(solar + grid + batteryDischarge - feedIn - batteryCharge - loads, 0.0)
    }
}

private fun List<OpenReportResponse>.todayValue(forKey: String): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<OpenReportResponse>.currentData(forKey: String): OpenReportResponseData? {
    val todaysDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    return firstOrNull { it.variable.equals(forKey, ignoreCase = true) }?.values?.firstOrNull { it.index == todaysDate }
}