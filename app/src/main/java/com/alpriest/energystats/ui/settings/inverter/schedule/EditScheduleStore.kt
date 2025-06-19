package com.alpriest.energystats.ui.settings.inverter.schedule

import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.DeviceCapability
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

        fun modes(configManager: ConfigManaging): List<WorkMode> {
            return WorkMode.entries.filter {
                if (it == WorkMode.PeakShaving) {
                    configManager.selectedDeviceSN?.let {
                        configManager.getDeviceSupports(DeviceCapability.PeakShaving, it)
                    } ?: false
                } else {
                    true
                }
            }.filter {
                it.title() != ""
            }
        }
    }
}

enum class WorkMode {
    SelfUse,
    Feedin,
    Backup,
    ForceCharge,
    ForceDischarge,
    Invalid,
    PeakShaving;

    fun title(): String {
        return when (this) {
            SelfUse -> "Self Use"
            Feedin -> return "Feed In First"
            Backup -> return "Backup"
            ForceCharge -> return "Force Charge"
            ForceDischarge -> return "Force Discharge"
            PeakShaving -> return "Peak Shaving"
            Invalid -> return ""
        }
    }
}

