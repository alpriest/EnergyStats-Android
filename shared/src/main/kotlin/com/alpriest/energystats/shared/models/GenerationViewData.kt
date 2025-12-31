package com.alpriest.energystats.shared.models

data class GenerationViewData(
    var today: Double,
    var month: Double,
    var cumulative: Double
)