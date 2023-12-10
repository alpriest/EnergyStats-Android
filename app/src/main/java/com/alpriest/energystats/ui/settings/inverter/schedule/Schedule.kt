package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.models.SchedulePollcy
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.ui.theme.PowerFlowNegative
import com.alpriest.energystats.ui.theme.PowerFlowPositive
import java.time.LocalDateTime
import java.util.UUID

data class Schedule(
    val name: String,
    val phases: List<SchedulePhase>,
    val templateID: String? = null
) {
    companion object {
        fun create(name: String? = null, phases: List<SchedulePhase>, templateID: String? = null): Schedule {
            return Schedule(name ?: "Schedule", phases, templateID)
        }
    }
}

data class SchedulePhase(
    val id: String,
    val start: Time,
    val end: Time,
    val mode: SchedulerModeResponse,
    val forceDischargePower: Int,
    val forceDischargeSOC: Int,
    val batterySOC: Int,
    val color: Color
) {
    companion object {
        fun create(
            id: String? = null,
            start: Time,
            end: Time,
            mode: SchedulerModeResponse?,
            forceDischargePower: Int,
            forceDischargeSOC: Int,
            batterySOC: Int,
            color: Color
        ): SchedulePhase? {
            mode ?: return null

            return SchedulePhase(
                id ?: UUID.randomUUID().toString(),
                start,
                end,
                mode,
                forceDischargePower,
                forceDischargeSOC,
                batterySOC,
                color
            )
        }

        fun createWithDefaults(mode: SchedulerModeResponse): SchedulePhase {
            val color: Color = Color.scheduleColor(mode.key)
            return SchedulePhase(
                UUID.randomUUID().toString(),
                Time.current(),
                Time.current(),
                mode,
                0,
                10,
                10,
                color = color
            )
        }
    }

    val startPoint: Float
        get() = minutesAfterMidnight(start).toFloat() / (24 * 60)

    val endPoint: Float
        get() = minutesAfterMidnight(end).toFloat() / (24 * 60)

    private fun minutesAfterMidnight(time: Time): Int {
        return (time.hour * 60) + time.minute
    }

    fun toPollcy(): SchedulePollcy {
        return SchedulePollcy(
            start.hour,
            start.minute,
            end.hour,
            end.minute,
            forceDischargePower,
            mode.key,
            forceDischargeSOC,
            batterySOC
        )
    }
}

fun Color.Companion.scheduleColor(mode: String): Color {
    return when (mode) {
        "FeedIn" -> PowerFlowPositive
        "ForceCharge" -> PowerFlowNegative
        "ForceDischarge" -> PowerFlowPositive
        "SelfUse" -> LightGray
        else -> Color.Black
    }
}

private fun Time.Companion.current(): Time {
    val now = LocalDateTime.now()
    return Time(now.hour, now.minute)
}
