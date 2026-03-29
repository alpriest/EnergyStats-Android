package com.alpriest.energystats.ui.settings.inverter.schedule.phase

import com.alpriest.energystats.shared.models.WorkMode
import com.alpriest.energystats.shared.models.network.Time

data class EditPhaseViewData(
    val id: String,
    val startTime: Time,
    val endTime: Time,
    val workMode: WorkMode,
    val modes: List<WorkMode>,
    val fields: List<SchedulePhaseFieldDefinition>,
    val showAdvancedFields: Boolean
)