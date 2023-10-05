package com.alpriest.energystats.ui.flow

import com.alpriest.energystats.models.ReportData
import com.alpriest.energystats.models.ReportResponse
import com.alpriest.energystats.models.ReportVariable
import java.lang.Double.max
import java.util.Calendar

class TotalsViewModel(val grid: Double, val feedIn: Double, val loads: Double, batteryCharge: Double, batteryDischarge: Double) {
    val solar: Double

    init {
        solar = max(0.0, batteryCharge - batteryDischarge - grid + loads + feedIn)
    }

    constructor(reports: List<ReportResponse>) :
            this(
                grid = reports.todayValue(forKey = ReportVariable.GridConsumption.networkTitle()),
                feedIn = reports.todayValue(forKey = ReportVariable.FeedIn.networkTitle()),
                loads = reports.todayValue(forKey = ReportVariable.Loads.networkTitle()),
                batteryCharge = reports.todayValue(forKey = ReportVariable.ChargeEnergyToTal.networkTitle()),
                batteryDischarge = reports.todayValue(forKey = ReportVariable.DischargeEnergyToTal.networkTitle())
            ) {
    }
}

private fun List<ReportResponse>.todayValue(forKey: String): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<ReportResponse>.currentData(forKey: String): ReportData? {
    val todaysDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    return firstOrNull { it.variable.lowercase() == forKey.lowercase() }?.data?.firstOrNull { it.index == todaysDate }
}
