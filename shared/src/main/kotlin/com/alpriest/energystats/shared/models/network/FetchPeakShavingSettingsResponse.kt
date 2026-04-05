package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class FetchPeakShavingSettingsRequest(
    val sn: String
)

@Serializable
data class FetchPeakShavingSettingsResponse(
    val importLimit: SettingItem,
    val soc: SettingItem
)

@Serializable
data class SettingItem(
    val precision: Double,
    val range: Range,
    val unit: String,
    val value: String
) {
    @Serializable
    data class Range(
        val min: Double,
        val max: Double
    )
}