package com.alpriest.energystats.ui.flow

import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.OpenReportResponseData
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.ui.CalculationBreakdown
import java.lang.Double.max
import java.util.Calendar

class TotalsViewModel(val grid: Double, val feedIn: Double, val loads: Double, batteryCharge: Double, batteryDischarge: Double) {
    val solar: Double
    val solarBreakdown: CalculationBreakdown

    init {
        solar = max(0.0, batteryCharge - batteryDischarge - grid + loads + feedIn)
        solarBreakdown = CalculationBreakdown(
            formula = "max(0, batteryCharge - batteryDischarge - gridImport + home + gridExport)",
            calculation = { "max(0, ${batteryCharge.roundedToString(it)} - ${batteryDischarge.roundedToString(it)} - ${grid.roundedToString(it)} + ${loads.roundedToString(it)} + ${feedIn.roundedToString(it)}" }
        )
    }

    constructor(reports: List<OpenReportResponse>) :
            this(
                grid = reports.todayValue(forKey = ReportVariable.GridConsumption.networkTitle()),
                feedIn = reports.todayValue(forKey = ReportVariable.FeedIn.networkTitle()),
                loads = reports.todayValue(forKey = ReportVariable.Loads.networkTitle()),
                batteryCharge = reports.todayValue(forKey = ReportVariable.ChargeEnergyToTal.networkTitle()),
                batteryDischarge = reports.todayValue(forKey = ReportVariable.DischargeEnergyToTal.networkTitle())
            ) {
    }
}

private fun List<OpenReportResponse>.todayValue(forKey: String): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<OpenReportResponse>.currentData(forKey: String): OpenReportResponseData? {
    val todaysDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    return firstOrNull { it.variable.lowercase() == forKey.lowercase() }?.values?.firstOrNull { it.index == todaysDate }
}
