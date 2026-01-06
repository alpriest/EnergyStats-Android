package com.alpriest.energystats.shared.models

import com.alpriest.energystats.shared.models.network.OpenReportResponse
import com.alpriest.energystats.shared.models.network.OpenReportResponseData
import java.util.Calendar

class TotalsViewModel(val grid: Double, val feedIn: Double, val loads: Double, val solar: Double, val ct2: Double) {
    constructor(reports: List<OpenReportResponse>, generationViewModel: GenerationViewModel?):
        this(
            grid = reports.todayValue(forKey = ReportVariable.GridConsumption.networkTitle()),
            feedIn = reports.todayValue(forKey = ReportVariable.FeedIn.networkTitle()),
            loads = reports.todayValue(forKey = ReportVariable.Loads.networkTitle()),
            solar = reports.todayValue(forKey = ReportVariable.PvEnergyToTal.networkTitle()),
            ct2 = generationViewModel?.ct2Total ?: 0.0
        )
}

private fun List<OpenReportResponse>.todayValue(forKey: String): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<OpenReportResponse>.currentData(forKey: String): OpenReportResponseData? {
    val todaysDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    return firstOrNull { it.variable.lowercase() == forKey.lowercase() }?.values?.firstOrNull { it.index == todaysDate }
}