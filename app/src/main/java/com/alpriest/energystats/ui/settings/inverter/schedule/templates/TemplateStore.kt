package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import com.alpriest.energystats.ui.settings.inverter.schedule.SchedulePhase
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import java.util.UUID

interface TemplateStoring {
    fun load(): List<ScheduleTemplate>
    fun save(template: ScheduleTemplate)
    fun delete(template: ScheduleTemplate)
    fun create(name: String)
}

class TemplateStore : TemplateStoring {
    var templates: MutableList<ScheduleTemplate> = mutableListOf(
        ScheduleTemplate(
            id = "1",
            name = "First schedule",
            phases = listOf(
                SchedulePhase.preview()
            )
        )
    )

    override fun load(): List<ScheduleTemplate> {
        return templates
    }

    override fun save(template: ScheduleTemplate) {
        TODO("Not yet implemented")
    }

    override fun delete(template: ScheduleTemplate) {
        TODO("Not yet implemented")
    }

    override fun create(name: String) {
        templates.add(
            ScheduleTemplate(
                id = UUID.randomUUID().toString(),
                name = name,
                phases = listOf()
            )
        )
    }
}

class PreviewTemplateStore : TemplateStoring {
    override fun load(): List<ScheduleTemplate> {
        return listOf(
            ScheduleTemplate(
                id = "1",
                name = "First schedule",
                phases = listOf(
                    SchedulePhase.preview()
                )
            )
        )
    }

    override fun save(template: ScheduleTemplate) {
    }

    override fun delete(template: ScheduleTemplate) {
    }

    override fun create(name: String) {
    }

}