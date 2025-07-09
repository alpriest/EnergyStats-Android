package com.alpriest.energystats.ui.settings.inverter.schedule

import android.content.Context
import com.alpriest.energystats.R
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

    fun title(context: Context): String {
        return when (this) {
            SelfUse -> context.getString(R.string.self_use)
            Feedin -> context.getString(R.string.feed_in)
            Backup -> context.getString(R.string.backup)
            ForceCharge -> context.getString(R.string.force_charge)
            ForceDischarge -> context.getString(R.string.force_discharge)
            PeakShaving -> context.getString(R.string.peak_shaving)
            Invalid -> return ""
        }
    }

    fun subtitle(context: Context): String {
        return when (this) {
            SelfUse -> context.getString(R.string.self_use_mode)
            Feedin -> context.getString(R.string.feed_in_first_mode)
            Backup -> context.getString(R.string.backup_mode)
            ForceCharge -> context.getString(R.string.workmode_force_charge_description)
            ForceDischarge -> context.getString(R.string.workmode_force_discharge_description)
            PeakShaving -> context.getString(R.string.peak_shaving_explanation)
            Invalid -> return ""
        }
    }

    companion object {
        fun from(value: String): WorkMode {
            return when (value) {
                "SelfUse" -> SelfUse
                "Feedin" -> Feedin
                "Backup" -> Backup
                "ForceCharge" -> ForceCharge
                "ForceDischarge" -> ForceDischarge
                else -> SelfUse
            }
        }
    }
}
