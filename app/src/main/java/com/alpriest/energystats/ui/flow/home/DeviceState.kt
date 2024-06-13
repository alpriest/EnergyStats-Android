package com.alpriest.energystats.ui.flow.home

enum class DeviceState(val value: Int) {
    Online(1),
    Fault(2),
    Offline(3),
    Unknown(99);

    companion object {
        fun fromInt(value: Int) = DeviceState.values().firstOrNull { it.value == value } ?: Online
    }
}