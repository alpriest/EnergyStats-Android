package com.alpriest.energystats.shared.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

/** An undated rate is the default before any dated changes. Dates use ISO calendar days. */
data class EnergyPriceRate(
    @SerializedName("price") val price: Double,
    @SerializedName("effectiveFrom") val effectiveFrom: String? = null,
    @SerializedName("id") val id: String = UUID.randomUUID().toString()
)

fun List<EnergyPriceRate>.priceOn(date: LocalDate): Double =
    sortedByDescending { it.effectiveFrom ?: "" }
        .firstOrNull { it.effectiveFrom == null || LocalDate.parse(it.effectiveFrom) <= date }?.price ?: 0.0

/** End is exclusive. Calendar days keep weighting independent of daylight saving changes. */
fun List<EnergyPriceRate>.weightedPrice(start: LocalDate, end: LocalDate): Double {
    val days = ChronoUnit.DAYS.between(start, end)
    if (days <= 0) return priceOn(start)
    val boundaries = (listOf(start, end) + mapNotNull { it.effectiveFrom?.let(LocalDate::parse) }
        .filter { it > start && it < end }).distinct().sorted()
    return boundaries.zipWithNext().sumOf { (from, to) -> priceOn(from) * ChronoUnit.DAYS.between(from, to) } / days
}
