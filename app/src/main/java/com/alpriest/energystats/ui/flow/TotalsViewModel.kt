package com.alpriest.energystats.ui.flow

import com.alpriest.energystats.models.ReportData
import com.alpriest.energystats.models.ReportResponse
import com.alpriest.energystats.models.ReportVariable
import java.util.ArrayList
import java.util.Calendar

class TotalsViewModel(report: ArrayList<ReportResponse>) {
    val homeTotal: Double = report.todayValue(forKey = ReportVariable.Loads.networkTitle())
    val gridImportTotal: Double = report.todayValue(forKey = ReportVariable.GridConsumption.networkTitle())
    val gridExportTotal: Double = report.todayValue(forKey = ReportVariable.FeedIn.networkTitle())
}

private fun List<ReportResponse>.todayValue(forKey: String): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<ReportResponse>.currentData(forKey: String): ReportData? {
    val todaysDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    return firstOrNull { it.variable.lowercase() == forKey.lowercase() }?.data?.firstOrNull { it.index == todaysDate }
}
