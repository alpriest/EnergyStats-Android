package com.alpriest.energystats.ui.flow

import com.alpriest.energystats.models.ReportData
import com.alpriest.energystats.models.ReportResponse
import com.alpriest.energystats.models.ReportVariable
import java.lang.Double.max
import java.util.Calendar

class TotalsViewModel(reports: List<ReportResponse>) {
    val homeTotal: Double = reports.todayValue(forKey = ReportVariable.Loads.networkTitle())
    val gridImportTotal: Double = reports.todayValue(forKey = ReportVariable.GridConsumption.networkTitle())
    val gridExportTotal: Double = reports.todayValue(forKey = ReportVariable.FeedIn.networkTitle())
    val solar: Double

    init {
        val batteryCharge = reports.todayValue(forKey = ReportVariable.ChargeEnergyToTal.networkTitle())
        val batteryDischarge = reports.todayValue(forKey = ReportVariable.DischargeEnergyToTal.networkTitle())

        solar = max(0.0, batteryCharge - batteryDischarge - gridImportTotal + homeTotal + gridExportTotal)
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
