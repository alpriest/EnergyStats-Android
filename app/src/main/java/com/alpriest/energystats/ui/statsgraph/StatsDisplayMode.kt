package com.alpriest.energystats.ui.statsgraph

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

sealed class StatsDisplayMode {
    data class Day(val date: LocalDate) : StatsDisplayMode()
    data class Month(val month: Int, val year: Int) : StatsDisplayMode()
    data class Year(val year: Int) : StatsDisplayMode()
    data class Custom(val start: LocalDate, val end: LocalDate) : StatsDisplayMode()

    fun unit(): String {
        return when (this) {
            is Day -> "Hour"
            is Month -> "Day"
            is Year -> "Month"
            is Custom -> "Day"
        }
    }

    val segmentCount: Int
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