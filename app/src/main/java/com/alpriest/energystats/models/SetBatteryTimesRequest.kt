package com.alpriest.energystats.models

data class SetBatteryTimesRequest(
    val sn: String,
    val enable1: Boolean,
    val startTime1: Time,
    val endTime1: Time,
    val enable2: Boolean,
    val startTime2: Time,
    val endTime2: Time
)