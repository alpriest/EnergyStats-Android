package com.alpriest.energystats.shared.models

data class Device(
    val deviceSN: String,
    val hasPV: Boolean,
    val stationName: String?,
    val stationID: String,
    val hasBattery: Boolean,
    val deviceType: String,
    val battery: Battery?,
    val moduleSN: String
) {
    companion object
}