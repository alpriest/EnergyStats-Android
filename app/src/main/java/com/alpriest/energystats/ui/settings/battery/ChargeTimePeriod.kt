package com.alpriest.energystats.ui.settings.battery

import com.alpriest.energystats.models.Time

data class ChargeTimePeriod(
    var start: Time,
    var end: Time,
    var enabled: Boolean
) {
    val description: String?
        get() = if (enabled) {
            String.format("from %02d:%02d to %02d:%02d", start.hour, start.minute, end.hour, end.minute)
        } else {
            null
        }

    val validate: String?
        get() = if (start.after(end)) {
            "Start time must be before the end time"
        } else {
            null
        }

    val isValid: Boolean
        get() = validate == null
}

fun Time.after(end: Time): Boolean {
    return (hour * 60) + minute > (end.hour * 60 + end.minute)
}

fun Time.Companion.zero(): Time {
    return Time(0, 0)
}
