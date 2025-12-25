package com.alpriest.energystats.helpers

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDateTime.timeUntilNow(): Long {
    val now = LocalDateTime.now(ZoneId.systemDefault())
    return Duration.between(this, now).seconds
}

fun LocalDateTime.isSameDay(other: LocalDateTime): Boolean {
    return this.toLocalDate() == other.toLocalDate()
}

fun LocalDate.monthYearString(): String {
    val formatter = DateTimeFormatter.ofPattern("MMMM y", Locale.getDefault())
    return this.format(formatter)
}

fun LocalDateTime.fullDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/y, HH:mm:ss")
    return this.format(formatter)
}

val dayMonthFormat = DateTimeFormatter.ofPattern("d MMM")
val dayMonthYearFormat = DateTimeFormatter.ofPattern("d MMM yyyy")