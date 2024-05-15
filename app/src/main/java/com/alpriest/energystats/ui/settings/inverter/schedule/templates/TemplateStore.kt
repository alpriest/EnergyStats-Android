package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import com.alpriest.energystats.ui.settings.inverter.schedule.SchedulePhase
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate

class TemplateStore {
    var templates: MutableList<ScheduleTemplate> = mutableListOf(
        ScheduleTemplate(
            id = "1",
            name = "First schedule",
            phases = listOf(
                SchedulePhase.preview()
            )
        )
    )
}