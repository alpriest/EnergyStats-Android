package com.alpriest.energystats.shared.models.network

data class PowerGenerationResponse(
    val today: Double,
    val month: Double,
    val cumulative: Double
)