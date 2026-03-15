package com.alpriest.energystats.shared.models

import com.alpriest.energystats.shared.models.network.SchedulePhaseNetworkModel
import com.alpriest.energystats.shared.models.network.ScheduleResponse
import com.alpriest.energystats.shared.models.network.Time
import java.util.UUID

private val Boolean.intValue: Int
    get() {
        return if (this) 1 else 0
    }

data class Schedule(
    val name: String,
    val phases: List<SchedulePhase>
) {
    fun isValid(): Boolean {
        val enabledPhases = phases.filter { it.enabled }

        for ((index, phase) in enabledPhases.withIndex()) {
            val phaseStart = phase.start.toMinutes()
            val phaseEnd = phase.end.toMinutes()

            // Check for overlap with other phases
            for (otherPhase in enabledPhases.subList(index + 1, enabledPhases.size)) {
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

    val hasTooManyPhases: Boolean = phases.size > MAX_PHASES_COUNT

    companion object {
        fun create(name: String? = null, phases: List<SchedulePhase>): Schedule {
            return Schedule(name ?: "Schedule", phases)
        }

        fun create(scheduleResponse: ScheduleResponse): Schedule {
            val phases = scheduleResponse.groups.mapNotNull { it.toSchedulePhase() }
            return Schedule(name = "", phases = phases)
        }

        const val MAX_PHASES_COUNT = 8
    }
}

data class SchedulePhase(
    val id: String,
    val enabled: Boolean,
    val start: Time,
    val end: Time,
    val mode: WorkMode,
    val forceDischargePower: Int,
    val forceDischargeSOC: Int,
    val minSocOnGrid: Int,
    val maxSOC: Int?
) {
    constructor(
        enabled: Boolean,
        start: Time,
        end: Time,
        mode: WorkMode,
        forceDischargePower: Int,
        forceDischargeSOC: Int,
        minSocOnGrid: Int,
        maxSOC: Int?
    ) : this(UUID.randomUUID().toString(), enabled, start, end, mode, forceDischargePower, forceDischargeSOC, minSocOnGrid, maxSOC)

    companion object {
        fun create(
            id: String? = null,
            enabled: Boolean,
            start: Time,
            end: Time,
            mode: WorkMode?,
            forceDischargePower: Int,
            forceDischargeSOC: Int,
            batterySOC: Int,
            maxSOC: Int?
        ): SchedulePhase? {
            if (mode == null || start == end) { return null }

            return SchedulePhase(
                id ?: UUID.randomUUID().toString(),
                enabled,
                start,
                end,
                mode,
                forceDischargePower,
                forceDischargeSOC,
                batterySOC,
                maxSOC
            )
        }

        fun create(mode: WorkMode, device: Device?, initialiseMaxSOC: Boolean): SchedulePhase {
            val minSOC = ((device?.battery?.minSOC ?: "0.1").toDouble() * 100.0).toInt()

            return SchedulePhase(
                true,
                Time.now(),
                Time.now().adding(1),
                mode,
                0,
                minSOC,
                minSOC,
                maxSOC = if (initialiseMaxSOC) 100 else null
            )
        }

        fun preview(): SchedulePhase {
            return create(
                enabled = true,
                start = Time(hour = 19, minute = 30),
                end = Time(hour = 23, minute = 30),
                mode = WorkModes.SelfUse,
                forceDischargePower = 0,
                forceDischargeSOC = 20,
                batterySOC = 20,
                maxSOC = 100
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

    fun toPhaseResponse(): SchedulePhaseNetworkModel {
        return SchedulePhaseNetworkModel(
            enable = enabled.intValue,
            startHour = start.hour,
            startMinute = start.minute,
            endHour = end.hour,
            endMinute = end.minute,
            workMode = mode,
            minSocOnGrid = minSocOnGrid,
            fdSoc = forceDischargeSOC,
            fdPwr = forceDischargePower,
            maxSoc = maxSOC
        )
    }
}

data class ScheduleTemplate(
    val id: String,
    val name: String,
    val phases: List<SchedulePhase>
)

internal fun SchedulePhaseNetworkModel.toSchedulePhase(): SchedulePhase? {
    if (startHour == 0 && endHour == 0 && startMinute == 0 && endMinute == 0) { return null }

    return SchedulePhase.create(
        enabled = enable.toBoolean,
        start = Time(hour = startHour, minute = startMinute),
        end = Time(hour = endHour, minute = endMinute),
        mode = workMode,
        forceDischargePower = fdPwr ?: 0,
        forceDischargeSOC = fdSoc,
        batterySOC = minSocOnGrid,
        maxSOC = maxSoc
    )
}

val Int.toBoolean: Boolean
    get() {
        return this == 1
    }