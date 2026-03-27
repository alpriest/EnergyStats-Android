package com.alpriest.energystats.ui.settings.inverter.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.SchedulePhaseV3
import com.alpriest.energystats.shared.models.WorkMode
import com.alpriest.energystats.shared.models.WorkModes
import com.alpriest.energystats.shared.models.network.SchedulePropertyDefinition
import com.alpriest.energystats.shared.models.network.SchedulePropertyDefinitionRange
import com.alpriest.energystats.shared.models.network.Time
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SchedulePhaseFieldDefinition(
    val key: String,
    val isStandard: Boolean,
    val title: String,
    val precision: Double,
    val range: SchedulePropertyDefinitionRange?,
    val unit: String?,
    val value: Double?,
    val error: String?,
    val description: String?
)

data class EditPhaseViewData(
    val id: String,
    val startTime: Time,
    val endTime: Time,
    val workMode: WorkMode,
    val modes: List<WorkMode>,
    val fields: List<SchedulePhaseFieldDefinition>,
    val showAdvancedFields: Boolean
)

class EditPhaseViewModelFactory(val navController: NavHostController, val configManager: ConfigManaging) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditPhaseViewModel(navController, configManager) as T
    }
}

class EditPhaseViewModel(val navController: NavHostController, val configManager: ConfigManaging) : ViewModel(), AlertDialogMessageProviding {
    val modes: List<WorkMode>

    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    private val _viewDataStream = MutableStateFlow(EditPhaseViewData(id = "", Time.now(), Time.now(), WorkModes.SelfUse, listOf(), listOf(), false))
    val viewDataStream: StateFlow<EditPhaseViewData> = _viewDataStream

    private val _errorStream = MutableStateFlow<Map<String, String>>(emptyMap())
    val errorStream: StateFlow<Map<String, String>> = _errorStream
    private var originalPhase: SchedulePhaseV3? = null

    init {
        EditScheduleStore.shared.scheduleStream.value?.let { schedule ->
            val originalPhase = schedule.phases.first { it.id == EditScheduleStore.shared.phaseId }
            this.originalPhase = originalPhase
            _viewDataStream.value = EditPhaseViewData(
                originalPhase.id,
                originalPhase.start,
                originalPhase.end,
                originalPhase.mode,
                EditScheduleStore.modes(configManager),
                emptyList(),
                false
            )
        }

        modes = EditScheduleStore.modes(configManager)
    }

    fun load(context: Context) {
        viewModelScope.launch {
            _viewDataStream.collect { validate(context) }
        }
    }

    fun deletePhase() {
        val schedule = EditScheduleStore.shared.scheduleStream.value ?: return
        val originalPhaseID = viewDataStream.value.id ?: return
        EditScheduleStore.shared.scheduleStream.value = SchedulePhaseHelper.delete(originalPhaseID, schedule)
        navController.popBackStack()
    }

    private fun validate(context: Context) {
        var minSOCError: String? = null
        var fdSOCError: String? = null
        var timeError: String? = null
        var forceDischargePowerError: String? = null
        var maxSOCError: String? = null

//        minSOCStream.value.toIntOrNull()?.let {
//            if (it !in 10..100) {
//                minSOCError = context.getString(R.string.please_enter_a_number_between_10_and_100)
//            }
//        }
//
//        forceDischargeSOCStream.value.toIntOrNull()?.let {
//            if (it !in 10..100) {
//                fdSOCError = context.getString(R.string.please_enter_a_number_between_10_and_100)
//            }
//        }
//
//        minSOCStream.value.toIntOrNull()?.let { soc ->
//            forceDischargeSOCStream.value.toIntOrNull()?.let { fdSOC ->
//                if (soc > fdSOC) {
//                    minSOCError = context.getString(R.string.min_soc_must_be_less_than_or_equal_to_force_discharge_soc)
//                }
//            }
//        }
//
//        if (startTimeStream.value >= endTimeStream.value) {
//            timeError = context.getString(R.string.end_time_must_be_after_start_time)
//        }
//
//        if (workModeStream.value == WorkModes.ForceDischarge && forceDischargePowerStream.value.toIntOrNull() == 0) {
//            forceDischargePowerError = context.getString(R.string.force_discharge_power_needs_to_be_greater_than_0_to_discharge)
//        }
//
//        maxSocStream.value.toIntOrNull()?.let { maxSoc ->
//            minSOCStream.value.toIntOrNull()?.let { minSoc ->
//                if (maxSoc !in minSoc..100) {
//                    maxSOCError = context.getString(R.string.please_enter_a_number_between_10_and_100)
//                }
//            }
//        }
//        errorStream.value = EditPhaseErrorData(minSOCError, fdSOCError, timeError, forceDischargePowerError, maxSOCError)
    }

    fun save(context: Context) {
        val viewData = viewDataStream.value
        val userSpecifiedFields = viewData.fields
        val fdSocValue = userSpecifiedFields.firstOrNull { it.key == "fdsoc" }?.value

        val fieldsWithSensibleDefaults = userSpecifiedFields.map { field ->
            when {
                viewData.workMode == WorkModes.ForceCharge && field.key == "maxsoc" ->
                    field.copy(value = fdSocValue)

                viewData.workMode == WorkModes.ForceDischarge && field.key == "importlimit" ->
                    field.copy(value = 0.0)

                field.key == "maxsoc" ->
                    field.copy(value = 100.0)

                else ->
                    field
            }
        }

        val phase = SchedulePhaseV3(
            id = viewData.id,
            start = viewData.startTime,
            end = viewData.endTime,
            mode = viewData.workMode,
            extraParam = fieldsWithSensibleDefaults
                .mapNotNull { field ->
                    field.value?.let { value ->
                        keyAsExtraParamKey(field.key) to value
                    }
                }
                .toMap()
        )

        validate(context)

        val schedule = EditScheduleStore.shared.scheduleStream.value
        if (schedule != null) {
            val updatedSchedule = SchedulePhaseHelper.update(phase, schedule)
            if (updatedSchedule.isValid()) {
                EditScheduleStore.shared.scheduleStream.value = updatedSchedule
                navController.popBackStack()
            } else {
                alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.this_schedule_phase_contains_invalid_phases_please_correct_and_try_again))
            }
        } else {
            alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.this_schedule_phase_contains_invalid_phases_please_correct_and_try_again))
        }
    }

    private fun keyAsExtraParamKey(key: String): String {
        val fieldNames = setOf("fdSoc", "fdPwr", "maxSoc", "minSocOnGrid")
        return fieldNames.firstOrNull { it.lowercase() == key } ?: key
    }

    fun startTimeChanged(time: Time) {
        _viewDataStream.value = viewDataStream.value.copy(startTime = time)
    }

    fun endTimeChanged(time: Time) {
        _viewDataStream.value = viewDataStream.value.copy(endTime = time)
    }

    fun workModeChanged(value: WorkMode) {
        _viewDataStream.value = viewDataStream.value.copy(workMode = value)
        determineVisibleFields()
    }

    fun phaseFieldChanged(phaseFieldDefinition: SchedulePhaseFieldDefinition, value: String) {
        val fields = viewDataStream.value.fields.map {
            if (it.key == phaseFieldDefinition.key) {
                it.copy(value = value.toDoubleOrNull())
            } else {
                it
            }
        }
        _viewDataStream.value = viewDataStream.value.copy(fields = fields)
    }

    fun determineVisibleFields() {
        val phase = originalPhase ?: return
        val viewData = _viewDataStream.value
        val mode = viewData.workMode
        val builder = FieldDefinitionBuilder(properties = configManager.scheduleProperties, phase = phase)

        val hiddenFieldKeys = mutableSetOf("maxSoc")
        val standardField: SchedulePhaseFieldDefinition?

        when (mode) {
            WorkModes.SelfUse -> {
                hiddenFieldKeys.add("fdpwr")
                hiddenFieldKeys.add("fdsoc")
                standardField = builder.make(key = "minsocongrid", isStandard = true, title = "Min SoC", description = null, defaultValue = 10.0)
            }

            WorkModes.Feedin -> {
                hiddenFieldKeys.add("fdpwr")
                hiddenFieldKeys.add("fdsoc")
                standardField = builder.make(key = "minsocongrid", isStandard = true, title = "Min SoC", description = null, defaultValue = 10.0)
            }

            WorkModes.Backup -> {
                hiddenFieldKeys.add("fdpwr")
                hiddenFieldKeys.add("fdsoc")
                standardField = builder.make(key = "minsocongrid", isStandard = true, title = "Min SoC", description = null, defaultValue = 10.0)
            }

            WorkModes.ForceCharge -> {
                standardField = builder.make(
                    key = "fdsoc",
                    isStandard = true,
                    title = "Charge to SoC",
                    description = "When the battery reaches this level, charging will stop.",
                    defaultValue = 100.0
                )
            }

            WorkModes.ForceDischarge ->
                standardField = builder.make(
                    key = "fdsoc",
                    isStandard = true,
                    title = "Discharge to SoC",
                    description = "When the battery reaches this level, discharging will stop. If you wanted to save some battery power for later, perhaps set it to 50%.",
                    defaultValue = 10.0
                )

            else -> standardField = null
        }

        val standardFields = listOfNotNull(standardField)
        hiddenFieldKeys.addAll(standardFields.map { it.key.lowercase() })
        hiddenFieldKeys.addAll(configManager.scheduleProperties.mapNotNull {
            if (it.value.unit.isEmpty()) it.key.lowercase() else null
        })

        val advancedFields: List<SchedulePhaseFieldDefinition> =
            configManager.scheduleProperties
                .keys
                .filter { allKey -> hiddenFieldKeys.firstOrNull({ standardKey -> standardKey == allKey.lowercase() }) == null }
                .map { key ->
                    val defaultValue = defaultValue(mode, key)
                    val description = description(mode, key)

                    builder.make(key, isStandard = false, title = key, description = description, defaultValue = defaultValue)
                }

        _viewDataStream.value = viewData.copy(
            fields = standardFields + advancedFields,
            showAdvancedFields = !advancedFields.isEmpty()
        )
    }

    fun description(mode: WorkMode, key: String): String? {
        return when (mode) {
            WorkModes.ForceCharge if key == "fdpwr" -> "The input power to charge your battery."
            WorkModes.ForceDischarge if key == "fdpwr" -> "The output power level to be delivered, including your house load and grid export. E.g. If you have 5kW inverter then set this to 5000, then if the house load is 750W the other 4.25kW will be exported."
            else -> null
        }
    }

    private fun defaultValue(mode: WorkMode, key: String): Double? {
        when (key) {
            "fdpwr" -> {
                val capacity: Double? = configManager.currentDevice.value?.capacity
                return capacity?.let {
                    it * 1000.0
                }
            }
            "importlimit" if mode == WorkModes.ForceDischarge -> {
                return 0.0
            }
            else -> {
                return null
            }
        }
    }
}


class FieldDefinitionBuilder(
    val properties: Map<String, SchedulePropertyDefinition>,
    val phase: SchedulePhaseV3
) {
    fun make(
        key: String,
        isStandard: Boolean,
        title: String,
        description: String?,
        defaultValue: Double?,
    ): SchedulePhaseFieldDefinition {
        val property = properties[key]

        return SchedulePhaseFieldDefinition(
            key = key,
            isStandard = isStandard,
            title = title,
            precision = (property?.precision) ?: 0.0,
            range = property?.range,
            unit = property?.unit,
            value = phase.valueFor(key) ?: defaultValue,
            error = null,
            description = description
        )
    }
}