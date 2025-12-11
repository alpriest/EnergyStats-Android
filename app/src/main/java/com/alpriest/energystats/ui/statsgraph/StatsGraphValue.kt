package com.alpriest.energystats.ui.statsgraph

import com.alpriest.energystats.models.ReportVariable
import java.text.DateFormatSymbols
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class StatsGraphValue(val type: ReportVariable, val graphPoint: Int, val graphValue: Double)

fun periodDescription(graphPoint: Int, displayMode: StatsDisplayMode): String {
    return when (displayMode) {
        is StatsDisplayMode.Day -> {
            val time = LocalTime.of(graphPoint, 0) // Assuming graphPoint represents the hour
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            time.format(formatter)
        }

        is StatsDisplayMode.Month -> {
            val dateFormatSymbols = DateFormatSymbols.getInstance()
            val monthName = dateFormatSymbols.months.getOrNull(displayMode.month) ?: "${displayMode.month}"
            "$graphPoint $monthName"
        }

        is StatsDisplayMode.Year -> {
            val dateFormatSymbols = DateFormatSymbols.getInstance()
            val monthName = dateFormatSymbols.months.getOrNull(graphPoint - 1) ?: "$graphPoint"
            "$monthName ${displayMode.year}"
        }

        is StatsDisplayMode.Custom -> {
            val date = displayMode.start.plusDays(graphPoint.toLong())
            val dateFormatSymbols = DateFormatSymbols.getInstance()
            val monthName = dateFormatSymbols.months.getOrNull(date.monthValue - 1) ?: "${date.monthValue}"
            "${date.dayOfMonth} $monthName"
        }
    }
}