package com.alpriest.energystats.ui.settings.inverter.schedule

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class EditPhaseViewModelFactory(val navController: NavHostController, val configManager: ConfigManaging) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditPhaseViewModel(navController, configManager) as T
    }
}

class EditPhaseViewModel(val navController: NavHostController, configManager: ConfigManaging) : ViewModel(), AlertDialogMessageProviding {
    val modes: List<WorkMode>
    val startTimeStream = MutableStateFlow(Time.now())
    val endTimeStream = MutableStateFlow(Time.now())
    val workModeStream: MutableStateFlow<WorkMode> = MutableStateFlow(WorkMode.entries.first())
    val forceDischargePowerStream = MutableStateFlow("0")
    val forceDischargeSOCStream = MutableStateFlow("0")
    val minSOCStream = MutableStateFlow("0")
    val errorStream = MutableStateFlow(EditPhaseErrorData(minSOCError = null, fdSOCError = null, timeError = null, forceDischargePowerError = null, maxSOCError = null))
    private var originalPhaseId: String? = null
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val showMaxSocStream = MutableStateFlow(false)
    val maxSocStream = MutableStateFlow("100")

    init {
        EditScheduleStore.shared.scheduleStream.value?.let { schedule ->
            val originalPhase = schedule.phases.first { it.id == EditScheduleStore.shared.phaseId }
            originalPhaseId = originalPhase.id
            startTimeStream.value = originalPhase.start
            endTimeStream.value = originalPhase.end
            workModeStream.value = originalPhase.mode
            forceDischargePowerStream.value = originalPhase.forceDischargePower.toString()
            forceDischargeSOCStream.value = originalPhase.forceDischargeSOC.toString()
            minSOCStream.value = originalPhase.minSocOnGrid.toString()
            showMaxSocStream.value = originalPhase.maxSOC != null
            originalPhase.maxSOC?.let { maxSocStream.value = it.toString() }
        }

        modes = EditScheduleStore.modes(configManager)
    }

    fun load(context: Context) {
        viewModelScope.launch {
            startTimeStream.collect { validate(context) }
        }

        viewModelScope.launch {
            endTimeStream.collect { validate(context) }
        }

        viewModelScope.launch {
            workModeStream.collect { validate(context) }
        }

        viewModelScope.launch {
            forceDischargePowerStream.collect { validate(context) }
        }

        viewModelScope.launch {
            forceDischargeSOCStream.collect { validate(context) }
        }

        viewModelScope.launch {
            minSOCStream.collect { validate(context) }
        }
    }

    fun deletePhase() {
        val schedule = EditScheduleStore.shared.scheduleStream.value ?: return
        val originalPhaseID = originalPhaseId ?: return
        EditScheduleStore.shared.scheduleStream.value = SchedulePhaseHelper.delete(originalPhaseID, schedule)
        navController.popBackStack()
    }

    private fun validate(context: Context) {
        var minSOCError: String? = null
        var fdSOCError: String? = null
        var timeError: String? = null
        var forceDischargePowerError: String? = null
        var maxSOCError: String? = null

        minSOCStream.value.toIntOrNull()?.let {
            if (it < 10 || it > 100) {
                minSOCError = context.getString(R.string.please_enter_a_number_between_10_and_100)
            }
        }

        forceDischargeSOCStream.value.toIntOrNull()?.let {
            if (it < 10 || it > 100) {
                fdSOCError = context.getString(R.string.please_enter_a_number_between_10_and_100)
            }
        }

        minSOCStream.value.toIntOrNull()?.let { soc ->
            forceDischargeSOCStream.value.toIntOrNull()?.let { fdSOC ->
                if (soc > fdSOC) {
                    minSOCError = context.getString(R.string.min_soc_must_be_less_than_or_equal_to_force_discharge_soc)
                }
            }
        }

        if (startTimeStream.value >= endTimeStream.value) {
            timeError = context.getString(R.string.end_time_must_be_after_start_time)
        }

        if (workModeStream.value == WorkMode.ForceDischarge && forceDischargePowerStream.value.toIntOrNull() == 0) {
            forceDischargePowerError = context.getString(R.string.force_discharge_power_needs_to_be_greater_than_0_to_discharge)
        }

        maxSocStream.value.toIntOrNull()?.let {maxSoc ->
            minSOCStream.value.toIntOrNull()?.let { minSoc ->
                if (maxSoc < minSoc || maxSoc > 100) {
                    maxSOCError = context.getString(R.string.please_enter_a_number_between_10_and_100)
                }
            }
        }
        errorStream.value = EditPhaseErrorData(minSOCError, fdSOCError, timeError, forceDischargePowerError, maxSOCError)
    }

    fun save(context: Context) {
        val originalPhaseId = originalPhaseId ?: return

        val phase = SchedulePhase.create(
            id = originalPhaseId,
            start = startTimeStream.value,
            end = endTimeStream.value,
            mode = workModeStream.value,
            forceDischargePower = forceDischargePowerStream.value.toIntOrNull() ?: 0,
            forceDischargeSOC = forceDischargeSOCStream.value.toIntOrNull() ?: 0,
            batterySOC = minSOCStream.value.toIntOrNull() ?: 0,
            color = Color.scheduleColor(workModeStream.value),
            maxSOC = if (showMaxSocStream.value) maxSocStream.value.toIntOrNull() else null
        )

        validate(context)

        val schedule = EditScheduleStore.shared.scheduleStream.value
        if (phase != null && schedule != null) {
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
}