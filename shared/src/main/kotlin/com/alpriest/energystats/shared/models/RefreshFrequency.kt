package com.alpriest.energystats.shared.models

enum class RefreshFrequency(val value: Int) {
    OneMinute(1),
    FiveMinutes(5),
    Auto(0);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: Auto
    }
}