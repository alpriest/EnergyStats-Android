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

class EditPhaseViewModel(val navController: NavHostController, configManager: ConfigManaging) : ViewModel(), AlertDialogMessageProviding {
    val modes: List<WorkMode>

    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val showMaxSocStream = MutableStateFlow(false)
    val maxSocStream = MutableStateFlow("100")
    private val _viewDataStream = MutableStateFlow(EditPhaseViewData(id = "", Time.now(), Time.now(), WorkModes.SelfUse, listOf(), listOf(), false))
    val viewDataStream: StateFlow<EditPhaseViewData> = _viewDataStream

    private val _errorStream = MutableStateFlow<Map<String, String>>(emptyMap())
    val errorStream: StateFlow<Map<String, String>> = _errorStream

    init {
        EditScheduleStore.shared.scheduleStream.value?.let { schedule ->
            val originalPhase = schedule.phases.first { it.id == EditScheduleStore.shared.phaseId }
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

}