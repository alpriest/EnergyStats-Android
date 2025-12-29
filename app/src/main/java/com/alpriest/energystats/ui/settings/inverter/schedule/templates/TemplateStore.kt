package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import com.alpriest.energystats.stores.ScheduleTemplateConfigManager
import com.alpriest.energystats.shared.models.SchedulePhase
import com.alpriest.energystats.shared.models.ScheduleTemplate
import java.util.UUID

interface TemplateStoring {
    fun load(): List<ScheduleTemplate>
    fun save(template: ScheduleTemplate)
    fun delete(template: ScheduleTemplate)
    fun create(name: String)
    fun duplicate(template: ScheduleTemplate, name: String)
    fun rename(template: ScheduleTemplate, name: String)
}

class TemplateStore(
    private val config: ScheduleTemplateConfigManager
) : TemplateStoring {
    override fun load(): List<ScheduleTemplate> {
        return config.scheduleTemplates
    }

    override fun save(template: ScheduleTemplate) {
        val mutableTemplates = config.scheduleTemplates.toMutableList()
        val index = mutableTemplates.indexOfFirst { it.id == template.id }
        if (index != -1) {
            mutableTemplates[index] = template
        } else {
            mutableTemplates.add(template)
        }
        config.scheduleTemplates = mutableTemplates
    }

    override fun delete(template: ScheduleTemplate) {
        config.scheduleTemplates = config.scheduleTemplates.filter { it.id != template.id }.toMutableList()
    }

    override fun create(name: String) {
        val mutableTemplates = config.scheduleTemplates.toMutableList()
        mutableTemplates.add(
            ScheduleTemplate(
                id = UUID.randomUUID().toString(),
                name = name,
                phases = listOf()
            )
        )
        config.scheduleTemplates = mutableTemplates
    }

    override fun duplicate(template: ScheduleTemplate, name: String) {
        val mutableTemplates = config.scheduleTemplates.toMutableList()
        mutableTemplates.add(
            ScheduleTemplate(
                id = UUID.randomUUID().toString(),
                name = name,
                phases = template.phases
            )
        )
        config.scheduleTemplates = mutableTemplates
    }

    override fun rename(template: ScheduleTemplate, name: String) {
        val renamed = template.copy(name = name)
        save(renamed)
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

    override fun duplicate(template: ScheduleTemplate, name: String) {
    }

    override fun rename(template: ScheduleTemplate, name: String) {
    }
}