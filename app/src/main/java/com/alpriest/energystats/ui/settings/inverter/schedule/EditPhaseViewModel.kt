package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.models.Time
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class EditPhaseViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditPhaseViewModel() as T
    }
}

class EditPhaseViewModel : ViewModel() {
    val modes = listOf(
        SchedulerModeResponse(color = "#00ff00", name = "Force charge", key = "ForceCharge"),
        SchedulerModeResponse(color = "#ff0000", name = "Force discharge", key = "ForceDischarge"),
        SchedulerModeResponse(color = "#ff0000", name = "Self Use", key = "SelfUse"),
    )
    val startTimeStream = MutableStateFlow(Time.now())
    val endTimeStream = MutableStateFlow(Time.now())
    val workModeStream = MutableStateFlow(modes.first())
    val forceDischargePowerStream = MutableStateFlow("0")
    val forceDischargeSOCStream = MutableStateFlow("0")
    val minSOCStream = MutableStateFlow("0")
    val errorStream = MutableStateFlow(EditPhaseErrorData(minSOCError = null, fdSOCError = null))
    var originalPhase: SchedulePhase? = null

    init {
        viewModelScope.launch {
            combine(
                startTimeStream,
                endTimeStream,
                workModeStream,
                forceDischargePowerStream,
                forceDischargeSOCStream,
                minSOCStream
            ) {
                onChange()
            }
        }

        EditScheduleStore.shared.schedule?.let { schedule ->
            originalPhase = schedule.phases.first { it.id == EditScheduleStore.shared.phaseId }
        }
    }

    private fun onChange() {
        val originalPhaseId = originalPhase?.id ?: return

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

        if (phase != null) {
            val schedule = EditScheduleStore.shared.schedule ?: return
            EditScheduleStore.shared.schedule = SchedulePhaseHelper.update(phase, schedule)
        }

        validate()
    }

    fun deletePhase() {
        val schedule = EditScheduleStore.shared.schedule ?: return
        val originalPhaseID = originalPhase?.id ?: return
        EditScheduleStore.shared.schedule = SchedulePhaseHelper.delete(originalPhaseID, schedule)
    }

    private fun validate() {
        var minSOCError: String? = null
        var fdSOCError: String? = null

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

        errorStream.value = EditPhaseErrorData(minSOCError, fdSOCError)
    }
}