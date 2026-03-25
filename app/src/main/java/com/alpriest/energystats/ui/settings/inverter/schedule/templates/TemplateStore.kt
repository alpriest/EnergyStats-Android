package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import com.alpriest.energystats.shared.config.ScheduleTemplateConfigManager
import com.alpriest.energystats.shared.models.SchedulePhaseV3
import com.alpriest.energystats.shared.models.ScheduleTemplateV3
import java.util.UUID

interface TemplateStoring {
    fun load(): List<ScheduleTemplateV3>
    fun save(template: ScheduleTemplateV3)
    fun delete(template: ScheduleTemplateV3)
    fun create(name: String)
    fun duplicate(template: ScheduleTemplateV3, name: String)
    fun rename(template: ScheduleTemplateV3, name: String)
}

class TemplateStore(
    private val config: ScheduleTemplateConfigManager
) : TemplateStoring {
    override fun load(): List<ScheduleTemplateV3> {
        return config.scheduleTemplates
    }

    override fun save(template: ScheduleTemplateV3) {
        val mutableTemplates = config.scheduleTemplates.toMutableList()
        val index = mutableTemplates.indexOfFirst { it.id == template.id }
        if (index != -1) {
            mutableTemplates[index] = template
        } else {
            mutableTemplates.add(template)
        }
        config.scheduleTemplates = mutableTemplates
    }

    override fun delete(template: ScheduleTemplateV3) {
        config.scheduleTemplates = config.scheduleTemplates.filter { it.id != template.id }.toMutableList()
    }

    override fun create(name: String) {
        val mutableTemplates = config.scheduleTemplates.toMutableList()
        mutableTemplates.add(
            ScheduleTemplateV3(
                id = UUID.randomUUID().toString(),
                name = name,
                phases = listOf()
            )
        )
        config.scheduleTemplates = mutableTemplates
    }

    override fun duplicate(template: ScheduleTemplateV3, name: String) {
        val mutableTemplates = config.scheduleTemplates.toMutableList()
        mutableTemplates.add(
            ScheduleTemplateV3(
                id = UUID.randomUUID().toString(),
                name = name,
                phases = template.phases
            )
        )
        config.scheduleTemplates = mutableTemplates
    }

    override fun rename(template: ScheduleTemplateV3, name: String) {
        val renamed = template.copy(name = name)
        save(renamed)
    }
}

class PreviewTemplateStore : TemplateStoring {
    override fun load(): List<ScheduleTemplateV3> {
        return listOf(
            ScheduleTemplateV3(
                id = "1",
                name = "First schedule",
                phases = listOf(
                    SchedulePhaseV3.preview()
                )
            )
        )
    }

    override fun save(template: ScheduleTemplateV3) {
    }

    override fun delete(template: ScheduleTemplateV3) {
    }

    override fun create(name: String) {
    }

    override fun duplicate(template: ScheduleTemplateV3, name: String) {
    }

    override fun rename(template: ScheduleTemplateV3, name: String) {
    }
}