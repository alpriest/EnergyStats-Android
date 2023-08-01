package com.alpriest.energystats.ui.settings.battery

import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.Time

data class ChargeTimePeriod(
    var start: Time,
    var end: Time,
    var enabled: Boolean
) {
    val description: String
        get() = String.format("%02d:%02d to %02d:%02d", start.hour, start.minute, end.hour, end.minute)

    val isValid: Boolean
        get() = !start.after(end)

    fun asChargeTime(): ChargeTime {
        return ChargeTime(
            enableGrid = enabled,
            startTime = start,
            endTime = end
        )
    }

    companion object
}

fun Time.after(end: Time): Boolean {
    return (hour * 60) + minute > ((end.hour * 60) + end.minute)
}

fun Time.Companion.zero(): Time {
    return Time(0, 0)
}
