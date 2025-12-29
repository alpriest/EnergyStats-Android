package com.alpriest.energystats.shared.models

data class SetPeakShavingSettingsRequest(
    val sn: String,
    val importLimit: Double,
    val soc: Int
)