package com.alpriest.energystats.ui.settings.inverter.schedule

import android.content.Context
import com.alpriest.energystats.R
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.DeviceCapability
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.reflect.Type

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
            return WorkModes.allCases.filter {
                if (it == WorkModes.PeakShaving) {
                    configManager.selectedDeviceSN?.let {
                        configManager.getDeviceSupports(DeviceCapability.PeakShaving, it)
                    } ?: false
                } else {
                    true
                }
            }.filter {
                it.showInUI()
            }
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

    val allCases: List<WorkMode> = listOf(
        SelfUse, Feedin, Backup, ForceCharge, ForceDischarge, PeakShaving
    )
}

fun WorkMode.title(context: Context): String = when (this) {
    WorkModes.SelfUse -> context.getString(R.string.self_use)
    WorkModes.Feedin -> context.getString(R.string.feed_in)
    WorkModes.Backup -> context.getString(R.string.backup)
    WorkModes.ForceCharge -> context.getString(R.string.force_charge)
    WorkModes.ForceDischarge -> context.getString(R.string.force_discharge)
    WorkModes.PeakShaving -> context.getString(R.string.peak_shaving)
    else -> ""
}

fun WorkMode.networkTitle(context: Context): String = when (this) {
    WorkModes.SelfUse -> "SelfUse"
    WorkModes.Feedin -> "Feedin"
    WorkModes.Backup -> "Backup"
    WorkModes.ForceCharge -> "ForceCharge"
    WorkModes.ForceDischarge -> "ForceDischarge"
    else -> this
}

fun WorkMode.subtitle(context: Context): String = when (this) {
    WorkModes.SelfUse -> context.getString(R.string.self_use_mode)
    WorkModes.Feedin -> context.getString(R.string.feed_in_first_mode)
    WorkModes.Backup -> context.getString(R.string.backup_mode)
    WorkModes.ForceCharge -> context.getString(R.string.workmode_force_charge_description)
    WorkModes.ForceDischarge -> context.getString(R.string.workmode_force_discharge_description)
    WorkModes.PeakShaving -> context.getString(R.string.peak_shaving_explanation)
    else -> ""
}

fun WorkMode.showInUI(): Boolean {
    return when (this) {
        WorkModes.SelfUse -> true
        WorkModes.Feedin -> true
        WorkModes.Backup -> true
        WorkModes.ForceCharge -> true
        WorkModes.ForceDischarge -> true
        WorkModes.PeakShaving -> true
        else -> false
    }
}

enum class WorkModeOLD {
    SelfUse,
    Feedin,
    Backup,
    ForceCharge,
    ForceDischarge,
    Invalid,
    PeakShaving,
    Unsupported;

    fun title(context: Context): String {
        return when (this) {
            SelfUse -> context.getString(R.string.self_use)
            Feedin -> context.getString(R.string.feed_in_first)
            Backup -> context.getString(R.string.backup)
            ForceCharge -> context.getString(R.string.force_charge)
            ForceDischarge -> context.getString(R.string.force_discharge)
            PeakShaving -> context.getString(R.string.peak_shaving)
            Invalid, Unsupported -> return ""
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
            Invalid, Unsupported -> return ""
        }
    }

    fun networkTitle(): String {
        return when (this) {
            SelfUse -> "SelfUse"
            Feedin -> "Feedin"
            Backup -> "Backup"
            ForceCharge -> "ForceCharge"
            ForceDischarge -> "ForceDischarge"
            PeakShaving -> "PeakShaving"
            else -> ""
        }
    }

    fun showInUI(): Boolean {
        return when (this) {
            SelfUse -> true
            Feedin -> true
            Backup -> true
            ForceCharge -> true
            ForceDischarge -> true
            PeakShaving -> true
            else -> false
        }
    }

    companion object {
        fun from(value: String): WorkModeOLD {
            return when (value) {
                "SelfUse" -> SelfUse
                "Feedin" -> Feedin
                "Backup" -> Backup
                "ForceCharge" -> ForceCharge
                "ForceDischarge" -> ForceDischarge
                "PeakShaving" -> PeakShaving
                else -> SelfUse
            }
        }
    }

    class Deserializer : JsonDeserializer<WorkModeOLD> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): WorkModeOLD {
            if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
                val raw = json.asString
                return WorkModeOLD.entries.find { it.name == raw || it.networkTitle() == raw } ?: Unsupported
            }

            return Unsupported
        }
    }
}
