package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import com.alpriest.energystats.stores.ScheduleTemplateConfigManager
import com.alpriest.energystats.ui.settings.inverter.schedule.SchedulePhase
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import java.util.UUID

interface TemplateStoring {
    fun load(): List<ScheduleTemplate>
    fun save(template: ScheduleTemplate)
    fun delete(template: ScheduleTemplate)
    fun create(name: String)
}

class TemplateStore(
    private val config: ScheduleTemplateConfigManager
) : TemplateStoring {
    var templates: List<ScheduleTemplate> = listOf(
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
        val mutableTemplates = templates.toMutableList()
        val index = mutableTemplates.indexOfFirst { it.id == template.id }
        if (index != -1) {
            mutableTemplates[index] = template
        } else {
            mutableTemplates.add(template)
        }
        templates = mutableTemplates
    }

    override fun delete(template: ScheduleTemplate) {
        templates = templates.filter { it.id != template.id }.toMutableList()
    }

    override fun create(name: String) {
        val mutableTemplates = templates.toMutableList()
        mutableTemplates.add(
            ScheduleTemplate(
                id = UUID.randomUUID().toString(),
                name = name,
                phases = listOf()
            )
        )
        templates = mutableTemplates
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