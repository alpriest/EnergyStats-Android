package com.alpriest.energystats.ui.settings.inverter.schedule

import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.Schedule
import com.alpriest.energystats.shared.models.SchedulePhase
import com.alpriest.energystats.shared.models.network.Time
import com.alpriest.energystats.shared.models.WorkMode

class SchedulePhaseHelper {
    companion object {
        fun addNewTimePeriod(schedule: Schedule, modes: List<WorkMode>, device: Device?, initialiseMaxSOC: Boolean): Schedule {
            val mode = modes.firstOrNull() ?: return schedule
            val newPhase = SchedulePhase.create(mode = mode, device = device, initialiseMaxSOC)
            val sortedPhases = schedule.phases + newPhase
            sortedPhases.sortedBy { it.start }

            return Schedule(
                name = schedule.name,
                phases = sortedPhases
            )
        }

        fun appendPhasesInGaps(schedule: Schedule, mode: WorkMode, device: Device?, initialiseMaxSOC: Boolean): Schedule {
            val minSOC = ((device?.battery?.minSOC ?: "0.1").toDouble() * 100.0).toInt()
            val newPhases = schedule.phases + createPhasesInGaps(schedule, mode, minSOC, initialiseMaxSOC)

            return Schedule(
                name = schedule.name,
                phases = newPhases.sortedBy { it.start }
            )
        }

        private fun createPhasesInGaps(schedule: Schedule, mode: WorkMode, soc: Int, initialiseMaxSOC: Boolean): List<SchedulePhase> {
            val sortedPhases = schedule.phases.sortedBy { it.start }

            val scheduleStartTime = Time(0, 0)
            val scheduleEndTime = Time(23, 59)
            val newPhases = mutableListOf<SchedulePhase>()
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

        private fun makePhase(start: Time, end: Time, mode: WorkMode, soc: Int, initialiseMaxSOC: Boolean): SchedulePhase {
            return SchedulePhase(
                start = start,
                end = end,
                mode = mode,
                forceDischargePower = 0,
                forceDischargeSOC = soc,
                minSocOnGrid = soc,
                maxSOC = if (initialiseMaxSOC) 100 else null
            )
        }

        fun update(phase: SchedulePhase, schedule: Schedule): Schedule {
            return Schedule(
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

        fun delete(phaseID: String, schedule: Schedule): Schedule {
            return Schedule(
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