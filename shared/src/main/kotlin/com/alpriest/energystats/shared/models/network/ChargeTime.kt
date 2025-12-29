package com.alpriest.energystats.shared.models.network

import com.alpriest.energystats.shared.models.network.Time

data class ChargeTime(
    val enable: Boolean,
    val startTime: Time,
    val endTime: Time
)