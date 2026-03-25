package com.alpriest.energystats.ui.settings.inverter.schedule

import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.SchedulePhaseV3
import com.alpriest.energystats.shared.models.ScheduleV3
import com.alpriest.energystats.shared.models.WorkMode
import com.alpriest.energystats.shared.models.network.Time

class SchedulePhaseHelper {
    companion object {
        fun addNewTimePeriod(schedule: ScheduleV3, modes: List<WorkMode>): ScheduleV3 {
            val mode = modes.firstOrNull() ?: return schedule
            val newPhase = SchedulePhaseV3.create(
                start = Time.now(),
                end = Time.now().adding(1),
                mode = mode,
                extraParam = emptyMap()
            )
            val sortedPhases = (schedule.phases + newPhase).filterNotNull().sortedBy { it.start }

            return ScheduleV3(
                name = schedule.name,
                phases = sortedPhases
            )
        }

        fun appendPhasesInGaps(schedule: ScheduleV3, mode: WorkMode, device: Device?, initialiseMaxSOC: Boolean): ScheduleV3 {
            val minSOC = ((device?.battery?.minSOC ?: "0.1").toDouble() * 100.0).toInt()
            val newPhases = schedule.phases + createPhasesInGaps(schedule, mode, minSOC, initialiseMaxSOC)

            return ScheduleV3(
                name = schedule.name,
                phases = newPhases.sortedBy { it.start }
            )
        }

        private fun createPhasesInGaps(schedule: ScheduleV3, mode: WorkMode, soc: Int, initialiseMaxSOC: Boolean): List<SchedulePhaseV3> {
            val sortedPhases = schedule.phases.sortedBy { it.start }

            val scheduleStartTime = Time(0, 0)
            val scheduleEndTime = Time(23, 59)
            val newPhases = mutableListOf<SchedulePhaseV3>()
            var lastEnd: Time? = null

            for (phase in sortedPhases) {
                lastEnd?.let {
                    if (it < phase.start.adding(minutes = -1)) {
                        val newPhaseStart = it.adding(minutes = 1)
                        val newPhaseEnd = phase.start.adding(minutes = -1)

                        val newPhase = makePhase(newPhaseStart, newPhaseEnd, mode, soc, initialiseMaxSOC)
                        newPhases.add(newPhase)
                    }
                } ?: run {
                    if (phase.start > scheduleStartTime) {
                        val newPhaseEnd = phase.start.adding(minutes = -1)

                        val newPhase = makePhase(scheduleStartTime, newPhaseEnd, mode, soc, initialiseMaxSOC)
                        newPhases.add(newPhase)
                    }
                }
                lastEnd = phase.end
            }

            lastEnd?.let {
                if (it < scheduleEndTime) {
                    val finalPhaseStart = it.adding(minutes = 1)
                    val finalPhase = makePhase(finalPhaseStart, scheduleEndTime, mode, soc, initialiseMaxSOC)
                    newPhases.add(finalPhase)
                }
            }

            return newPhases
        }

        private fun makePhase(start: Time, end: Time, mode: WorkMode, soc: Int, initialiseMaxSOC: Boolean): SchedulePhaseV3 {
            val params = mutableMapOf(
                "minSocOnGrid" to soc.toDouble(),
                "forceDischargePower" to 0.0,
                "forceDischargeSOC" to soc.toDouble(),
            )

            if (initialiseMaxSOC) {
                params["maxSOC"] = 100.0
            }

            return SchedulePhaseV3(
                start = start,
                end = end,
                mode = mode,
                extraParam = params
            )
        }

        fun update(phase: SchedulePhaseV3, schedule: ScheduleV3): ScheduleV3 {
            return ScheduleV3(
                name = schedule.name,
                phases = schedule.phases.map {
                    if (it.id == phase.id) {
                        phase
                    } else {
                        it
                    }
                }
            )
        }

        fun delete(phaseID: String, schedule: ScheduleV3): ScheduleV3 {
            return ScheduleV3(
                name = schedule.name,
                phases = schedule.phases.mapNotNull {
                    if (it.id == phaseID) {
                        null
                    } else {
                        it
                    }
                }
            )
        }
    }
}