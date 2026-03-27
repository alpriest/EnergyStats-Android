package com.alpriest.energystats.shared.models

import com.alpriest.energystats.shared.models.network.SchedulePhaseRequest
import com.alpriest.energystats.shared.models.network.SchedulePhaseResponse
import com.alpriest.energystats.shared.models.network.ScheduleResponse
import com.alpriest.energystats.shared.models.network.Time
import java.util.UUID

data class ScheduleV3(
    val name: String,
    val phases: List<SchedulePhaseV3>
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

    val hasTooManyPhases: Boolean = phases.size > MAX_PHASES_COUNT

    companion object {
        fun create(name: String? = null, phases: List<SchedulePhaseV3>): ScheduleV3 {
            return ScheduleV3(name ?: "Schedule", phases)
        }

        fun create(scheduleResponse: ScheduleResponse): ScheduleV3 {
            val phases = scheduleResponse.groups.mapNotNull { it.toSchedulePhase() }
            return ScheduleV3(name = "", phases = phases)
        }

        const val MAX_PHASES_COUNT = 8
    }
}

data class SchedulePhaseV3(
    val id: String,
    val start: Time,
    val end: Time,
    val mode: WorkMode,
    val extraParam: Map<String, Double>
) {
    constructor(
        start: Time,
        end: Time,
        mode: WorkMode,
        extraParam: Map<String, Double>
    ) : this(UUID.randomUUID().toString(), start, end, mode, extraParam)

    companion object {
        fun create(
            id: String? = null,
            start: Time,
            end: Time,
            mode: WorkMode?,
            extraParam: Map<String, Double>
        ): SchedulePhaseV3? {
            if (mode == null || start == end) {
                return null
            }

            return SchedulePhaseV3(
                id ?: UUID.randomUUID().toString(),
                start,
                end,
                mode,
                extraParam
            )
        }

//        fun create(mode: WorkMode, device: Device?, initialiseMaxSOC: Boolean): SchedulePhaseV3 {
//            val minSOC = ((device?.battery?.minSOC ?: "0.1").toDouble() * 100.0).toInt()
//
//            return SchedulePhaseV3(
//                Time.now(),
//                Time.now().adding(1),
//                mode,
//                extraParam
//            )
//        }

        fun preview(): SchedulePhaseV3 {
            return create(
                start = Time(hour = 19, minute = 30),
                end = Time(hour = 23, minute = 30),
                mode = WorkModes.SelfUse,
                extraParam = mapOf(
                    "forceDischargePower" to 0.0,
                    "forceDischargeSOC" to 20.0,
                    "batterySOC" to 20.0,
                    "maxSOC" to 100.0
                )
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
        return end > start
    }

    fun toPhaseResponse(): SchedulePhaseRequest {
        return SchedulePhaseRequest(
            startHour = start.hour,
            startMinute = start.minute,
            endHour = end.hour,
            endMinute = end.minute,
            workMode = mode,
            extraParam = extraParam
        )
    }

    fun hasExtraParam(key: String): Boolean =
        extraParam.keys.map { it.lowercase() }.contains(key.lowercase())

    fun valueFor(key: String): Double? {
        return extraParam.entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
    }

    fun stringValueFor(key: String): String =
        valueFor(key)?.toInt()?.toString() ?: "??"
}

data class ScheduleTemplateV3(
    val id: String,
    val name: String,
    val phases: List<SchedulePhaseV3>
)

internal fun SchedulePhaseResponse.toSchedulePhase(): SchedulePhaseV3? {
    if (startHour == 0 && endHour == 0 && startMinute == 0 && endMinute == 0) {
        return null
    }

    return SchedulePhaseV3.create(
        start = Time(hour = startHour, minute = startMinute),
        end = Time(hour = endHour, minute = endMinute),
        mode = workMode,
        extraParam = extraParam ?: emptyMap()
    )
}
