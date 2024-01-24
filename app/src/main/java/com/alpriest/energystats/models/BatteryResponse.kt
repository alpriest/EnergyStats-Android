package com.alpriest.energystats.models

data class BatteryResponse(
    val power: Double,
    val soc: Int,
    val residual: Double,
    val temperature: Double
)

data class BatteryTimesResponse(
    val enable1: Boolean,
    val startTime1: Time,
    val endTime1: Time,

    val enable2: Boolean,
    val startTime2: Time,
    val endTime2: Time,
)

data class ChargeTime(
    val enable: Boolean,
    val startTime: Time,
    val endTime: Time
)

data class DeviceSettingsGetResponse(
    val protocol: String,
    val values: DeviceSettingsValues
)

data class DeviceSettingsValues(
    val operation_mode__work_mode: String
)

data class GetSchedulerFlagRequest(
    val deviceSN: String
)

data class GetSchedulerFlagResponse(
    val enable: Boolean,
    val support: Boolean
)