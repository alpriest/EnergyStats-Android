package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class SetSchedulerFlagRequest(
    val deviceSN: String,
    val enable: Int
)