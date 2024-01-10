package com.alpriest.energystats.ui.settings.inverter.schedule

import com.alpriest.energystats.models.SchedulerModeResponse
import kotlinx.coroutines.flow.MutableStateFlow

class EditScheduleStore(
    var scheduleStream: MutableStateFlow<Schedule?> = MutableStateFlow(null),
    var phaseId: String? = null,
    var allowDeletion: Boolean = false,
    var modes: List<SchedulerModeResponse> = listOf()
) {
    fun reset() {
        scheduleStream.value = null
        phaseId = null
        allowDeletion = false
    }

    companion object {
        val shared: EditScheduleStore = EditScheduleStore()
    }
}