package com.alpriest.energystats.helpers

import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale

fun LocalDateTime.timeUntilNow(): Long {
    val now = LocalDateTime.now(ZoneId.systemDefault())
    return Duration.between(this, now).seconds
}

fun LocalDateTime.isSameDay(other: LocalDateTime): Boolean {
    return this.toLocalDate() == other.toLocalDate()
}

fun LocalDate.monthYear(): String {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month.value - 1)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val date = calendar.time
    val dateFormatter = SimpleDateFormat("MMMM y", Locale.getDefault())
    return dateFormatter.format(date)
}