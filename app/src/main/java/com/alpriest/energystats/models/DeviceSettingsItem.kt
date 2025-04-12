package com.alpriest.energystats.models

enum class DeviceSettingsItem(val rawValue: String) {
    ExportLimit("ExportLimit"),
    MinSoc("MinSoc"),
    MinSocOnGrid("MinSocOnGrid"),
    MaxSoc("MaxSoc"),
    GridCode("GridCode")
}

data class FetchDeviceSettingsItemRequest(
    val sn: String,
    val key: String
)

data class FetchDeviceSettingsItemResponse(
    val value: String,
    val unit: String?,
    val precision: Double?,
    val range: Range?
) {
    data class Range(
        val min: Double,
        val max: Double
    )
}

data class SetDeviceSettingsItemRequest(
    val sn: String,
    val key: String,
    val value: String
)
