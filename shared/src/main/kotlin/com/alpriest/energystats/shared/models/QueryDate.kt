package com.alpriest.energystats.shared.models

import java.util.Calendar
import java.util.Date
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

fun QueryDate.toDate(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, (month ?: 1) - 1) // Month is 0-based in Calendar
    calendar.set(Calendar.DAY_OF_MONTH, day ?: 1) // Default to 1st if null
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}