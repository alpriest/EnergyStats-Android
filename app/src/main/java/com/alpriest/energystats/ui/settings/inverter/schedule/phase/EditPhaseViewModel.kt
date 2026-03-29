package com.alpriest.energystats.ui.settings.inverter.schedule.phase

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
import com.alpriest.energystats.ui.settings.inverter.schedule.EditScheduleStore
import com.alpriest.energystats.ui.settings.inverter.schedule.SchedulePhaseHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SchedulePhaseValidationReason {
    object InvalidNumber : SchedulePhaseValidationReason()
    data class InvalidRange(val min: Double, val max: Double) : SchedulePhaseValidationReason()
    object MinSocLessThanFdSoc : SchedulePhaseValidationReason()
}

data class SchedulePhaseFieldDefinition(
    val key: String,
    val isStandard: Boolean,
    val precision: Double,
    val range: SchedulePropertyDefinitionRange?,
    val unit: String?,
    val value: Double?
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
    private val _viewDataStream = MutableStateFlow(EditPhaseViewData(id = "", Time.Companion.now(), Time.Companion.now(), WorkModes.SelfUse, listOf(), listOf(), false))
    val viewDataStream: StateFlow<EditPhaseViewData> = _viewDataStream

    private val _errorStream = MutableStateFlow<Map<String, SchedulePhaseValidationReason>>(emptyMap())
    val errorStream: StateFlow<Map<String, SchedulePhaseValidationReason>> = _errorStream

    private val _timeErrorStream = MutableStateFlow<String?>(null)
    val timeErrorStream: StateFlow<String?> = _timeErrorStream

    private var originalPhase: SchedulePhaseV3? = null

    init {
        EditScheduleStore.Companion.shared.scheduleStream.value?.let { schedule ->
            val originalPhase = schedule.phases.first { it.id == EditScheduleStore.Companion.shared.phaseId }
            this.originalPhase = originalPhase
            _viewDataStream.value = EditPhaseViewData(
                originalPhase.id,
                originalPhase.start,
                originalPhase.end,
                originalPhase.mode,
                EditScheduleStore.Companion.modes(configManager),
                emptyList(),
                false
            )
            determineVisibleFields()
        }

        modes = EditScheduleStore.Companion.modes(configManager)
    }

    fun load(context: Context) {
        viewModelScope.launch {
            _viewDataStream.collect { validate(context) }
        }
    }

    fun deletePhase() {
        val schedule = EditScheduleStore.Companion.shared.scheduleStream.value ?: return
        val originalPhaseID = viewDataStream.value.id ?: return
        EditScheduleStore.Companion.shared.scheduleStream.value = SchedulePhaseHelper.Companion.delete(originalPhaseID, schedule)
        navController.popBackStack()
    }

    private fun failedValidationReason(field: SchedulePhaseFieldDefinition): SchedulePhaseValidationReason? {
        return if (field.value == null) {
            SchedulePhaseValidationReason.InvalidNumber
        } else {
            if (field.range != null &&
                (field.value < field.range.min || field.value > field.range.max)
            ) {
                SchedulePhaseValidationReason.InvalidRange(field.range.min, field.range.max)
            } else {
                null
            }
        }
    }

    private fun validate(context: Context) {
        val fieldErrors = mutableMapOf<String, SchedulePhaseValidationReason>()
        val viewData = viewDataStream.value

        for (field in viewData.fields) {
            failedValidationReason(field)?.let { fieldErrors[field.key] = it }
        }

        if (viewData.workMode == WorkModes.ForceDischarge) {
            val minSoc = viewData.fields.firstOrNull { it.key == "minsocongrid" }?.value
            val fdSoc = viewData.fields.firstOrNull { it.key == "fdsoc" }?.value

            if (minSoc != null && fdSoc != null && minSoc > fdSoc) {
                fieldErrors["minsocongrid"] = SchedulePhaseValidationReason.MinSocLessThanFdSoc
            }
        }

        if (viewData.startTime >= viewData.endTime) {
            _timeErrorStream.value = context.getString(R.string.end_time_must_be_after_start_time)
        }

        _errorStream.value = fieldErrors
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

        val schedule = EditScheduleStore.Companion.shared.scheduleStream.value
        if (schedule != null) {
            val updatedSchedule = SchedulePhaseHelper.Companion.update(phase, schedule)
            if (updatedSchedule.isValid()) {
                EditScheduleStore.Companion.shared.scheduleStream.value = updatedSchedule
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

        val hiddenFieldKeys = mutableSetOf("maxsoc")
        val standardField: SchedulePhaseFieldDefinition?

        when (mode) {
            WorkModes.SelfUse -> {
                hiddenFieldKeys.add("fdpwr")
                hiddenFieldKeys.add("fdsoc")
                standardField = builder.make(key = "minsocongrid", isStandard = true, defaultValue = 10.0)
            }

            WorkModes.Feedin -> {
                hiddenFieldKeys.add("fdpwr")
                hiddenFieldKeys.add("fdsoc")
                standardField = builder.make(key = "minsocongrid", isStandard = true, defaultValue = 10.0)
            }

            WorkModes.Backup -> {
                hiddenFieldKeys.add("fdpwr")
                hiddenFieldKeys.add("fdsoc")
                standardField = builder.make(key = "minsocongrid", isStandard = true, defaultValue = 10.0)
            }

            WorkModes.ForceCharge -> {
                standardField = builder.make(
                    key = "fdsoc",
                    isStandard = true,
                    defaultValue = 100.0
                )
            }

            WorkModes.ForceDischarge ->
                standardField = builder.make(
                    key = "fdsoc",
                    isStandard = true,
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

                    builder.make(key, isStandard = false, defaultValue = defaultValue)
                }

        _viewDataStream.value = viewData.copy(
            fields = standardFields + advancedFields,
            showAdvancedFields = !advancedFields.isEmpty()
        )
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
                return 0.0
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
        defaultValue: Double?,
    ): SchedulePhaseFieldDefinition {
        val property = properties[key]

        return SchedulePhaseFieldDefinition(
            key = key,
            isStandard = isStandard,
            precision = (property?.precision) ?: 0.0,
            range = property?.range,
            unit = property?.unit,
            value = phase.valueFor(key) ?: defaultValue
        )
    }
}