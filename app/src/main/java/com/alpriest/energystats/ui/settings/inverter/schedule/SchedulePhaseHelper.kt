package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.models.Time

class SchedulePhaseHelper {
    companion object {
        fun addNewTimePeriod(schedule: Schedule, modes: List<SchedulerModeResponse>, device: Device?): Schedule {
            val mode = modes.firstOrNull() ?: return schedule
            val newPhase = SchedulePhase.create(mode = mode, device = device)
            val sortedPhases = schedule.phases + newPhase
            sortedPhases.sortedBy { it.start }

            return Schedule(
                name = schedule.name,
                phases = sortedPhases,
                templateID = schedule.templateID
            )
        }

        fun appendPhasesInGaps(schedule: Schedule, mode: SchedulerModeResponse, device: Device?): Schedule {
            val minSOC = ((device?.battery?.minSOC ?: "0.1").toDouble() * 100.0).toInt()
            val newPhases = schedule.phases + createPhasesInGaps(schedule, mode, minSOC)

            return Schedule(
                name = schedule.name,
                phases = newPhases.sortedBy { it.start },
                templateID = schedule.templateID
            )
        }

        private fun createPhasesInGaps(schedule: Schedule, mode: SchedulerModeResponse, soc: Int): List<SchedulePhase> {
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

                        val newPhase = makePhase(newPhaseStart, newPhaseEnd, mode, soc)
                        newPhases.add(newPhase)
                    }
                } ?: run {
                    if (phase.start > scheduleStartTime) {
                        val newPhaseEnd = phase.start.adding(minutes = -1)

                        val newPhase = makePhase(scheduleStartTime, newPhaseEnd, mode, soc)
                        newPhases.add(newPhase)
                    }
                }
                lastEnd = phase.end
            }

            lastEnd?.let {
                if (it < scheduleEndTime) {
                    val finalPhaseStart = it.adding(minutes = 1)
                    val finalPhase = makePhase(finalPhaseStart, scheduleEndTime, mode, soc)
                    newPhases.add(finalPhase)
                }
            }

            return newPhases
        }

        private fun makePhase(start: Time, end: Time, mode: SchedulerModeResponse, soc: Int): SchedulePhase {
            return SchedulePhase(
                start = start,
                end = end,
                mode = mode,
                forceDischargePower = 0,
                forceDischargeSOC = soc,
                batterySOC = soc,
                color = Color.scheduleColor(mode.key)
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
                },
                templateID = schedule.templateID
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
                },
                templateID = schedule.templateID
            )
        }
    }
}