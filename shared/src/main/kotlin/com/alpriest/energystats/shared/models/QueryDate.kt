package com.alpriest.energystats.shared.models

import java.time.LocalDate
import java.util.Calendar
import java.util.TimeZone

data class QueryDate(val year: Int, val month: Int?, val day: Int?) {
    companion object {
        operator fun invoke(): QueryDate {
            val calendar = Calendar.getInstance()
            return QueryDate(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1,
                day = calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun from(date: LocalDate): QueryDate {
            return QueryDate(date.year, date.monthValue, date.dayOfMonth)
        }
    }
}

fun QueryDate.toUtcMillis(): Long {
    val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
        set(Calendar.YEAR, year)
        // Adjust month to 0-based index used by Calendar
        set(Calendar.MONTH, (month ?: 1) - 1)
        set(Calendar.DAY_OF_MONTH, day ?: 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

fun QueryDate.toDate(): LocalDate {
    return LocalDate.of(year, month ?: 1, day ?: 1)
}