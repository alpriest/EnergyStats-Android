package com.alpriest.energystats.ui.flow.home

enum class DeviceState(val value: Int) {
    Online(1),
    Fault(2),
    Offline(3);

    companion object {
        fun fromInt(value: Int) = DeviceState.values().firstOrNull { it.value == value } ?: DeviceState.Online
    }
}