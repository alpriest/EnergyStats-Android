package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class GetSchedulerFlagResponse(
    val enable: Boolean,
    val support: Boolean
)