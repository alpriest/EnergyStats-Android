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

data class BatteryTimesResponse(
    val sn: String,
    val times: List<ChargeTime>
)

data class ChargeTime(
    private val enableCharge: Boolean = true,
    val enableGrid: Boolean,
    val startTime: Time,
    val endTime: Time
)

data class Time(
    val hour: Int,
    val minute: Int
) {
    companion object
}