package com.alpriest.energystats.ui.settings.inverter.schedule

import kotlinx.coroutines.flow.MutableStateFlow

class EditScheduleStore(
    var scheduleStream: MutableStateFlow<Schedule?> = MutableStateFlow(null),
    var phaseId: String? = null,
    var allowDeletion: Boolean = false
) {
    fun reset() {
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
    FeedIn,
    Backup,
    ForceCharge,
    ForceDischarge,
    Invalid;

    fun title(): String {
        return when (this) {
            SelfUse -> "Self Use"
            FeedIn -> return "Feed In First"
            Backup -> return "Backup"
            ForceCharge -> return "Force Charge"
            ForceDischarge -> return "Force Discharge"
            Invalid -> return ""
        }
    }
}
