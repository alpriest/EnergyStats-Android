package com.alpriest.energystats.models

import com.alpriest.energystats.ui.settings.inverter.schedule.WorkMode

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

data class GetSchedulerFlagRequest(
    val deviceSN: String
)

data class GetSchedulerFlagResponse(
    val enable: Boolean,
    val support: Boolean
)

data class ScheduleResponse(
    val enable: Int,
    val groups: List<SchedulePhaseResponse>
)

data class SchedulePhaseResponse(
    val enable: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val workMode: WorkMode,
    val minSocOnGrid: Int,
    val fdSoc: Int,
    val fdPwr: Int?
)