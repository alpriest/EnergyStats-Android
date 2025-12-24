package com.alpriest.energystats.ui.settings.inverter.schedule

import android.content.Context
import com.alpriest.energystats.R
import com.alpriest.energystats.stores.ConfigManaging
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
            return configManager.workModes
        }
    }
}

typealias WorkMode = String

object WorkModes {
    const val SelfUse = "SelfUse"
    const val Feedin = "Feedin"
    const val Backup = "Backup"
    const val ForceCharge = "ForceCharge"
    const val ForceDischarge = "ForceDischarge"
    const val PeakShaving = "PeakShaving"
}

fun WorkMode.title(context: Context): String = when (this) {
    WorkModes.SelfUse -> context.getString(R.string.self_use)
    WorkModes.Feedin -> context.getString(R.string.feed_in)
    WorkModes.Backup -> context.getString(R.string.backup)
    WorkModes.ForceCharge -> context.getString(R.string.force_charge)
    WorkModes.ForceDischarge -> context.getString(R.string.force_discharge)
    WorkModes.PeakShaving -> context.getString(R.string.peak_shaving)
    else -> this
}

fun WorkMode.networkTitle(): String = when (this) {
    WorkModes.SelfUse -> "SelfUse"
    WorkModes.Feedin -> "Feedin"
    WorkModes.Backup -> "Backup"
    WorkModes.ForceCharge -> "ForceCharge"
    WorkModes.ForceDischarge -> "ForceDischarge"
    WorkModes.PeakShaving -> "PeakShaving"
    else -> this
}

fun WorkMode.subtitle(context: Context): String? = when (this) {
    WorkModes.SelfUse -> context.getString(R.string.self_use_mode)
    WorkModes.Feedin -> context.getString(R.string.feed_in_first_mode)
    WorkModes.Backup -> context.getString(R.string.backup_mode)
    WorkModes.ForceCharge, "BatteryCharge(AC)" -> context.getString(R.string.workmode_force_charge_description)
    WorkModes.ForceDischarge, "BatteryDischarge(AC)" -> context.getString(R.string.workmode_force_discharge_description)
    WorkModes.PeakShaving -> context.getString(R.string.peak_shaving_explanation)
    "BatteryCharge(BAT)" -> context.getString(R.string.workmode_force_charge_mode_bat_description)
    "BatteryDischarge(BAT)" -> context.getString(R.string.workmode_force_discharge_mode_bat_description)
    else -> null
}
