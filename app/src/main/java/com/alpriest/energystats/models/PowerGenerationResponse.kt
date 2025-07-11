package com.alpriest.energystats.models

data class PowerGenerationResponse(
    val today: Double,
    val month: Double,
    val cumulative: Double
)