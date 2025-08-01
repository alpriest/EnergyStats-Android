package com.alpriest.energystats.models

import android.content.Context
import com.alpriest.energystats.R

enum class DeviceSettingsItem(val rawValue: String) {
    ExportLimit("ExportLimit"),
    MinSoc("MinSoc"),
    MinSocOnGrid("MinSocOnGrid"),
    MaxSoc("MaxSoc"),
    GridCode("GridCode"),
    WorkMode("WorkMode");

    fun title(context: Context): String {
        return when (this) {
            ExportLimit -> context.getString(R.string.export_limit)
            MaxSoc -> context.getString(R.string.max_soc)
            else -> ""
        }
    }

    fun fallbackUnit(): String {
        return when (this) {
            MaxSoc -> "%"
            else -> ""
        }
    }

    fun description(context: Context): String {
        return when(this) {
            ExportLimit -> context.getString(R.string.export_limit_description)
            MaxSoc -> context.getString(R.string.maxsoc_description)
            else -> ""
        }
    }

    fun behaviour(context: Context): String {
        return when(this) {
            ExportLimit -> context.getString(R.string.export_limit_behaviour)
            MaxSoc -> context.getString(R.string.maxsoc_behaviour)
            else -> ""
        }
    }
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
