package com.alpriest.energystats.ui.statsgraph

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

sealed class StatsDisplayMode {
    data class Day(val date: LocalDate) : StatsDisplayMode()
    data class Month(val month: Int, val year: Int) : StatsDisplayMode()
    data class Year(val year: Int) : StatsDisplayMode()
    data class Custom(val start: LocalDate, val end: LocalDate, val unit: CustomDateRangeDisplayUnit) : StatsDisplayMode()

    fun unit(): String {
        return when (this) {
            is Day -> "Hour"
            is Month -> "Day"
            is Year -> "Month"
            is Custom -> "Day"
        }
    }

    val xPlotCount: Int
        get() {
            return when (this) {
                is Day -> 24
                is Month -> YearMonth.of(year, month).lengthOfMonth()
                is Year -> 12
                is Custom -> {
                    val days = ChronoUnit.DAYS.between(start, end).toInt() + 1
                    if (days < 0) 0 else days
                }
            }
        }
}
/** Dates represented by a report or one selected graph bucket; end is exclusive. */
fun StatsDisplayMode.pricePeriod(point: Int? = null): Pair<LocalDate, LocalDate> = when (this) {
    is StatsDisplayMode.Day -> date to date.plusDays(1)
    is StatsDisplayMode.Month -> {
        val start = LocalDate.of(year, month, point ?: 1)
        start to if (point == null) start.plusMonths(1) else start.plusDays(1)
    }
    is StatsDisplayMode.Year -> {
        val start = LocalDate.of(year, point ?: 1, 1)
        start to if (point == null) start.plusYears(1) else start.plusMonths(1)
    }
    is StatsDisplayMode.Custom -> {
        if (point == null) start to end.plusDays(1)
        else if (unit == CustomDateRangeDisplayUnit.DAYS) {
            val day = start.plusDays(point.toLong())
            day to day.plusDays(1)
        } else {
            val month = start.withDayOfMonth(1).plusMonths(point.toLong())
            maxOf(start, month) to minOf(end.plusDays(1), month.plusMonths(1))
        }
    }
}
