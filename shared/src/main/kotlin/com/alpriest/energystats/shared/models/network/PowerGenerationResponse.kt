package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class PowerGenerationResponse(
    val today: Double,
    val month: Double,
    val cumulative: Double
)