package com.alpriest.energystats.shared.models

data class SolarRangeDefinitions(
    val threshold1: Double,
    val threshold2: Double,
    val threshold3: Double
) {
    companion object {
        val defaults: SolarRangeDefinitions
            get() = SolarRangeDefinitions(1.0, 2.0, 3.0)
    }
}