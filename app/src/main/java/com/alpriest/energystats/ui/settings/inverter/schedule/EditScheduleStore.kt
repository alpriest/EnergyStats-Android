package com.alpriest.energystats.ui.settings.inverter.schedule

import kotlinx.coroutines.flow.MutableStateFlow

class EditScheduleStore(
    var scheduleStream: MutableStateFlow<Schedule?> = MutableStateFlow(null),
    var templateStream: MutableStateFlow<ScheduleTemplate?> = MutableStateFlow(null),
    var phaseId: String? = null,
    var allowDeletion: Boolean = false
) {
    fun reset() {
        templateStream.value = null
        scheduleStream.value = null
        phaseId = null
        allowDeletion = false
    }

    companion object {
        val shared: EditScheduleStore = EditScheduleStore()
    }

    var modes: List<WorkMode> = WorkMode.values().filter { it.title() != "" }
}

enum class WorkMode {
    SelfUse,
    Feedin,
    Backup,
    ForceCharge,
    ForceDischarge,
    Invalid;

    fun title(): String {
        return when (this) {
            SelfUse -> "Self Use"
            Feedin -> return "Feed In First"
            Backup -> return "Backup"
            ForceCharge -> return "Force Charge"
            ForceDischarge -> return "Force Discharge"
            Invalid -> return ""
        }
    }
}
