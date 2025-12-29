package com.alpriest.energystats.shared.models.network

data class GetSchedulerFlagResponse(
    val enable: Boolean,
    val support: Boolean
)