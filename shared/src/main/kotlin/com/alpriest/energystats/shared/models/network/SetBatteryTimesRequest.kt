package com.alpriest.energystats.shared.models.network

import com.alpriest.energystats.shared.models.network.Time

data class SetBatteryTimesRequest(
    val sn: String,
    val enable1: Boolean,
    val startTime1: Time,
    val endTime1: Time,
    val enable2: Boolean,
    val startTime2: Time,
    val endTime2: Time
)