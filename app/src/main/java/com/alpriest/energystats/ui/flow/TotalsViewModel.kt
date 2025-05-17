package com.alpriest.energystats.ui.flow

import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.OpenReportResponseData
import com.alpriest.energystats.models.ReportVariable
import java.util.Calendar

class TotalsViewModel(val grid: Double, val feedIn: Double, val loads: Double, val solar: Double) {
    constructor(reports: List<OpenReportResponse>, deviceHasPV: Boolean):
        this(
            grid = reports.todayValue(forKey = ReportVariable.GridConsumption.networkTitle()),
            feedIn = reports.todayValue(forKey = ReportVariable.FeedIn.networkTitle()),
            loads = reports.todayValue(forKey = ReportVariable.Loads.networkTitle()),
            solar = calculateSolar(reports, deviceHasPV)
        )

    companion object {
        private fun calculateSolar(reports: List<OpenReportResponse>, deviceHasPV: Boolean): Double {
            val solar: Double
            val home = reports.todayValue(forKey = ReportVariable.Loads.networkTitle())
            val gridExport = reports.todayValue(forKey = ReportVariable.FeedIn.networkTitle())
            val gridImport = reports.todayValue(forKey = ReportVariable.GridConsumption.networkTitle())
            if (deviceHasPV) {
                solar = reports.todayValue(forKey = ReportVariable.PvEnergyToTal.networkTitle())
            } else {
                val batteryCharge = reports.todayValue(forKey = ReportVariable.ChargeEnergyToTal.networkTitle())
                val batteryDischarge = reports.todayValue(forKey = ReportVariable.DischargeEnergyToTal.networkTitle())
                solar = maxOf(0.0, batteryCharge - batteryDischarge - gridImport + home + gridExport)
            }

            return solar
        }
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
