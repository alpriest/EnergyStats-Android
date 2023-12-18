package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.models.Device
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
    fun isValid(): Boolean {
        for ((index, phase) in phases.withIndex()) {
            val phaseStart = phase.start.toMinutes()
            val phaseEnd = phase.end.toMinutes()

            // Check for overlap with other phases
            for (otherPhase in phases.subList(index + 1, phases.size)) {
                val otherStart = otherPhase.start.toMinutes()
                val otherEnd = otherPhase.end.toMinutes()

                // Check if the time periods overlap
                // Updated to ensure periods must start/end on different minutes
                if (phaseStart <= otherEnd && otherStart < phaseEnd) {
                    return false
                }

                if (!phase.isValid()) {
                    return false
                }
            }
        }

        return true
    }

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
    constructor(
        start: Time,
        end: Time,
        mode: SchedulerModeResponse,
        forceDischargePower: Int,
        forceDischargeSOC: Int,
        batterySOC: Int,
        color: Color
    ): this(UUID.randomUUID().toString(), start, end, mode, forceDischargePower, forceDischargeSOC, batterySOC, color)

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

        fun create(mode: SchedulerModeResponse, device: Device?): SchedulePhase {
            val color: Color = Color.scheduleColor(mode.key)
            val minSOC = ((device?.battery?.minSOC ?: "0.1").toDouble() * 100.0).toInt()

            return SchedulePhase(
                UUID.randomUUID().toString(),
                Time.now(),
                Time.now().adding(1),
                mode,
                0,
                minSOC,
                minSOC,
                color = color
            )
        }

        fun preview(): SchedulePhase {
            return create(
                start = Time(hour = 19, minute = 30),
                end = Time(hour = 23, minute = 30),
                mode = SchedulerModeResponse(color = "#ff0000", name = "Self Use", key = "SelfUse"),
                forceDischargePower = 0,
                forceDischargeSOC = 20,
                batterySOC = 20,
                color = Color.scheduleColor("SelfUse")
            )!!
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

    fun isValid(): Boolean {
        return batterySOC <= forceDischargeSOC && end > start
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

data class ScheduleTemplateSummary(
    val id: String,
    val name: String,
    val enabled: Boolean
)

data class ScheduleTemplate(
    val id: String,
    val phases: List<SchedulePhase>
)
