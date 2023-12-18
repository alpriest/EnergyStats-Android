package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.models.Time
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class EditPhaseViewModelFactory(val navController: NavHostController) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditPhaseViewModel(navController) as T
    }
}

class EditPhaseViewModel(val navController: NavHostController) : ViewModel() {
    val modes: List<SchedulerModeResponse> = EditScheduleStore.shared.modes
    val startTimeStream = MutableStateFlow(Time.now())
    val endTimeStream = MutableStateFlow(Time.now())
    val workModeStream: MutableStateFlow<SchedulerModeResponse>
    val forceDischargePowerStream = MutableStateFlow("0")
    val forceDischargeSOCStream = MutableStateFlow("0")
    val minSOCStream = MutableStateFlow("0")
    val errorStream = MutableStateFlow(EditPhaseErrorData(minSOCError = null, fdSOCError = null, timeError = null))
    private var originalPhaseId: String? = null

    init {
        workModeStream = MutableStateFlow(modes.first())

        EditScheduleStore.shared.scheduleStream.value?.let { schedule ->
            val originalPhase = schedule.phases.first { it.id == EditScheduleStore.shared.phaseId }
            originalPhaseId = originalPhase.id
            startTimeStream.value = originalPhase.start
            endTimeStream.value = originalPhase.end
            workModeStream.value = originalPhase.mode
            forceDischargePowerStream.value = originalPhase.forceDischargePower.toString()
            forceDischargeSOCStream.value = originalPhase.forceDischargeSOC.toString()
            minSOCStream.value = originalPhase.batterySOC.toString()
        }

        viewModelScope.launch {
            startTimeStream.collect { validate() }
        }

        viewModelScope.launch {
            endTimeStream.collect { validate() }
        }

        viewModelScope.launch {
            workModeStream.collect { validate() }
        }

        viewModelScope.launch {
            forceDischargePowerStream.collect { validate() }
        }

        viewModelScope.launch {
            forceDischargeSOCStream.collect { validate() }
        }

        viewModelScope.launch {
            minSOCStream.collect { validate() }
        }
    }

    fun deletePhase() {
        val schedule = EditScheduleStore.shared.scheduleStream.value ?: return
        val originalPhaseID = originalPhaseId ?: return
        EditScheduleStore.shared.scheduleStream.value = SchedulePhaseHelper.delete(originalPhaseID, schedule)
        navController.popBackStack()
    }

    private fun validate() {
        var minSOCError: String? = null
        var fdSOCError: String? = null
        var timeError: String? = null

        minSOCStream.value.toIntOrNull()?.let {
            if (it < 10 || it > 100) {
                minSOCError = "Please enter a number between 10 and 100"
            }
        }

        forceDischargeSOCStream.value.toIntOrNull()?.let {
            if (it < 10 || it > 100) {
                fdSOCError = "Please enter a number between 10 and 100"
            }
        }

        minSOCStream.value.toIntOrNull()?.let { soc ->
            forceDischargeSOCStream.value.toIntOrNull()?.let { fdSOC ->
                if (soc > fdSOC) {
                    minSOCError = "Min SoC must be less than or equal to Force Discharge SoC"
                }
            }
        }

        if (startTimeStream.value >= endTimeStream.value) {
            timeError = "End time must be after start time"
        }

        errorStream.value = EditPhaseErrorData(minSOCError, fdSOCError, timeError)
    }

    fun save() {
        val originalPhaseId = originalPhaseId ?: return

        val phase = SchedulePhase.create(
            id = originalPhaseId,
            start = startTimeStream.value,
            end = endTimeStream.value,
            mode = workModeStream.value,
            forceDischargePower = forceDischargePowerStream.value.toIntOrNull() ?: 0,
            forceDischargeSOC = forceDischargeSOCStream.value.toIntOrNull() ?: 0,
            batterySOC = minSOCStream.value.toIntOrNull() ?: 0,
            color = Color.scheduleColor(workModeStream.value.key)
        )

        validate()

        val schedule = EditScheduleStore.shared.scheduleStream.value
        if (phase != null && schedule != null && schedule.isValid()) {
            EditScheduleStore.shared.scheduleStream.value = SchedulePhaseHelper.update(phase, schedule)
            navController.popBackStack()
        }
    }
}