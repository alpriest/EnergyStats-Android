package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class GetSchedulerFlagRequest(
    val deviceSN: String
)