package com.alpriest.energystats.shared.models.network

data class FetchPeakShavingSettingsRequest(
    val sn: String
)

data class FetchPeakShavingSettingsResponse(
    val importLimit: SettingItem,
    val soc: SettingItem
)

data class SettingItem(
    val precision: Double,
    val range: Range,
    val unit: String,
    val value: String
) {
    data class Range(
        val min: Double,
        val max: Double
    )
}