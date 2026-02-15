package com.alpriest.energystats.ui.statsgraph

import com.alpriest.energystats.shared.models.ReportVariable
import java.text.DateFormatSymbols
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
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
            when (displayMode.unit) {
                CustomDateRangeDisplayUnit.DAYS -> {
                    val date = displayMode.start.plusDays(graphPoint.toLong())
                    val dateFormatSymbols = DateFormatSymbols.getInstance()
                    val monthName = dateFormatSymbols.months.getOrNull(date.monthValue - 1) ?: "${date.monthValue}"
                    "${date.dayOfMonth} $monthName"
                }

                CustomDateRangeDisplayUnit.MONTHS -> {
                    val date = displayMode.start.plusMonths(graphPoint.toLong())
                    val dateFormatSymbols = DateFormatSymbols.getInstance()
                    val monthName = dateFormatSymbols.months.getOrNull(date.monthValue - 1) ?: "${date.monthValue}"
                    "$monthName ${date.year}"
                }
            }
        }
    }
}

private fun StatsGraphValue.isInRangeFor(
    displayMode: StatsDisplayMode,
    now: LocalDateTime = LocalDateTime.now(ZoneId.systemDefault())
): Boolean =
    when (displayMode) {
        is StatsDisplayMode.Day -> {
            if (displayMode.date == now.toLocalDate()) {
                graphPoint <= now.hour
            } else true
        }
        is StatsDisplayMode.Month -> graphPoint <= now.dayOfMonth
        is StatsDisplayMode.Year -> graphPoint <= now.monthValue
        else -> true
    }

fun List<StatsGraphValue>.filterToNow(
    displayMode: StatsDisplayMode,
    now: LocalDateTime = LocalDateTime.now(ZoneId.systemDefault())
): List<StatsGraphValue> = filter { it.isInRangeFor(displayMode, now) }