package com.alpriest.energystats.models

data class BatteryResponse(
    val power: Double,
    val soc: Int,
    val residual: Double,
    val temperature: Double
)

data class BatterySettingsResponse(
    val minGridSoc: Int,
    val minSoc: Int
)

data class EarningsResponse(
    val today: Earning
)

data class Earning(
    val generation: Double,
    val earnings: Double
)