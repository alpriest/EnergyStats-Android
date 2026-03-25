package com.alpriest.energystats.shared.models

import com.alpriest.energystats.shared.models.ScheduleV3.Companion.MAX_PHASES_COUNT
import com.alpriest.energystats.shared.models.network.SchedulePhaseRequest
import com.alpriest.energystats.shared.models.network.Time
import java.util.UUID

private val Boolean.intValue: Int
    get() {
        return if (this) 1 else 0
    }

data class ScheduleV1(
    val name: String,
    val phases: List<SchedulePhaseV1>
) {
    fun isValid(): Boolean {
        val phasesToCheck = phases.filter {
            !it.isAllDaySynthesized()
        }

        for ((index, phase) in phasesToCheck.withIndex()) {
            val phaseStart = phase.start.toMinutes()
            val phaseEnd = phase.end.toMinutes()

            // Check for overlap with other phases
            for (otherPhase in phasesToCheck.subList(index + 1, phasesToCheck.size)) {
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
}

data class SchedulePhaseV1(
    val id: String,
    val start: Time,
    val end: Time,
    val mode: WorkMode,
    val forceDischargePower: Int,
    val forceDischargeSOC: Int,
    val minSocOnGrid: Int,
    val maxSOC: Int?
) {
    constructor(
        start: Time,
        end: Time,
        mode: WorkMode,
        forceDischargePower: Int,
        forceDischargeSOC: Int,
        minSocOnGrid: Int,
        maxSOC: Int?
    ) : this(UUID.randomUUID().toString(), start, end, mode, forceDischargePower, forceDischargeSOC, minSocOnGrid, maxSOC)

    companion object {
        fun create(
            id: String? = null,
            start: Time,
            end: Time,
            mode: WorkMode?,
            forceDischargePower: Int,
            forceDischargeSOC: Int,
            batterySOC: Int,
            maxSOC: Int?
        ): SchedulePhaseV1? {
            if (mode == null || start == end) {
                return null
            }

            return SchedulePhaseV1(
                id ?: UUID.randomUUID().toString(),
                start,
                end,
                mode,
                forceDischargePower,
                forceDischargeSOC,
                batterySOC,
                maxSOC
            )
        }

        fun create(mode: WorkMode, device: Device?, initialiseMaxSOC: Boolean): SchedulePhaseV1 {
            val minSOC = ((device?.battery?.minSOC ?: "0.1").toDouble() * 100.0).toInt()

            return SchedulePhaseV1(
                Time.now(),
                Time.now().adding(1),
                mode,
                0,
                minSOC,
                minSOC,
                maxSOC = if (initialiseMaxSOC) 100 else null
            )
        }

        fun preview(): SchedulePhaseV1 {
            return create(
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

    fun isAllDaySynthesized(): Boolean {
        return start.hour == 0 && start.minute == 0 && end.hour == 23 && end.minute == 59
    }
}

data class ScheduleTemplateV1(
    val id: String,
    val name: String,
    val phases: List<SchedulePhaseV1>
)

val Int.toBoolean: Boolean
    get() {
        return this == 1
    }