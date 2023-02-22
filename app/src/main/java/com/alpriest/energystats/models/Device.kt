package com.alpriest.energystats.models

data class Device(
    val deviceID: String,
    val deviceSN: String,
    val hasBattery: Boolean,
    val hasPV: Boolean
)