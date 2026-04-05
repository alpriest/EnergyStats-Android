package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class SetPeakShavingSettingsRequest(
    val sn: String,
    val importLimit: Double,
    val soc: Int
)