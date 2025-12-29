package com.alpriest.energystats.shared.models

data class PowerGenerationResponse(
    val today: Double,
    val month: Double,
    val cumulative: Double
)