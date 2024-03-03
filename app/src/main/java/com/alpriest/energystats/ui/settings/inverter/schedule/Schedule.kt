package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.SchedulePhaseResponse
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.ui.theme.PowerFlowNegative
import com.alpriest.energystats.ui.theme.PowerFlowPositive
import java.util.UUID

private val Boolean.intValue: Int
    get() {
        return if (this) 1 else 0
    }

data class Schedule(
    val name: String,
    val phases: List<SchedulePhase>,
    val templateID: String? = null,
    val description: String?
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
            return Schedule(name ?: "Schedule", phases, templateID, description = null)
        }
    }
}

data class SchedulePhase(
    val id: String,
    val start: Time,
    val end: Time,
    val mode: WorkMode,
    val forceDischargePower: Int,
    val forceDischargeSOC: Int,
    val minSocOnGrid: Int,
    val color: Color
) {
    constructor(
        start: Time,
        end: Time,
        mode: WorkMode,
        forceDischargePower: Int,
        forceDischargeSOC: Int,
        minSocOnGrid: Int,
        color: Color
    ) : this(UUID.randomUUID().toString(), start, end, mode, forceDischargePower, forceDischargeSOC, minSocOnGrid, color)

    companion object {
        fun create(
            id: String? = null,
            start: Time,
            end: Time,
            mode: WorkMode?,
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

        fun create(mode: WorkMode, device: Device?): SchedulePhase {
            val color: Color = Color.scheduleColor(mode)
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
                mode = WorkMode.SelfUse,
                forceDischargePower = 0,
                forceDischargeSOC = 20,
                batterySOC = 20,
                color = Color.scheduleColor(WorkMode.SelfUse)
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

    fun isValid(): Boolean {
        return minSocOnGrid <= forceDischargeSOC && end > start
    }

    fun toPhaseResponse(): SchedulePhaseResponse {
        return SchedulePhaseResponse(
            enable = true.intValue,
            startHour = start.hour,
            startMinute = start.minute,
            endHour = end.hour,
            endMinute = end.minute,
            workMode = mode,
            minSocOnGrid = minSocOnGrid,
            fdSoc = forceDischargeSOC,
            fdPwr = forceDischargePower
        )
    }
}

fun Color.Companion.scheduleColor(mode: WorkMode): Color {
    return when (mode) {
        WorkMode.Feedin -> PowerFlowPositive
        WorkMode.ForceCharge -> PowerFlowNegative
        WorkMode.ForceDischarge -> PowerFlowPositive
        WorkMode.SelfUse -> LightGray
        else -> Black
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
