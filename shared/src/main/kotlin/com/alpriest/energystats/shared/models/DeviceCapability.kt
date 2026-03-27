package com.alpriest.energystats.shared.models

enum class DeviceCapability {
    ScheduleMaxSOC,
    PeakShaving;

    fun schedulePropertyKey(): String {
        return when (this) {
            ScheduleMaxSOC -> "maxsoc"
            PeakShaving -> "peakshaving"
        }
    }
}