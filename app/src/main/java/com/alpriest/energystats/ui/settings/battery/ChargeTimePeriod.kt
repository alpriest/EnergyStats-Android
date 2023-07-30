package com.alpriest.energystats.ui.settings.battery

import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.Time
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

data class ChargeTimePeriod(
    var start: Time,
    var end: Time,
    var enabled: Boolean
) {
    val description: String?
        get() = if (enabled) {
            val format = SimpleDateFormat("HH:mm")
            String.format("%s - %s", format.format(start), format.format(end))
        } else {
            null
        }

    val validate: String?
        get() = if (start.after(end)) {
            "Charge time period validation failed."
        } else {
            null
        }

    val isValid: Boolean
        get() = validate == null

    fun asChargeTime(): ChargeTime {
        return ChargeTime(
            enableGrid = enabled,
            startTime = start,
            endTime = end
        )
    }
}

private fun Time.after(end: Time): Boolean {
    return (hour * 60) + minute > (end.hour * 60 + end.minute)
}

fun Time.toDate(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    return calendar.time
}

fun Date.toTime(): Time {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
}
