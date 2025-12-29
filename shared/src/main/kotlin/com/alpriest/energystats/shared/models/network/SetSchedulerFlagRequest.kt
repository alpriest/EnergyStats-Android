package com.alpriest.energystats.shared.models.network

data class SetSchedulerFlagRequest(
    val deviceSN: String,
    val enable: Int
)