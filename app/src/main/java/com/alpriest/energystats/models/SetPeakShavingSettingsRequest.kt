package com.alpriest.energystats.models

data class SetPeakShavingSettingsRequest(
    val sn: String,
    val importLimit: Double,
    val soc: Int
)
