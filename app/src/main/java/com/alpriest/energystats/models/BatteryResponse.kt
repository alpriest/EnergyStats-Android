package com.alpriest.energystats.models

data class BatteryResponse(
    val power: Double,
    val soc: Int,
    val residual: Double
)

data class BatterySettingsResponse(
    val minSoc: Int
)