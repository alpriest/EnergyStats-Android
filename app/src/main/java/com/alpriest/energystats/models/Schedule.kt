package com.alpriest.energystats.models

data class SchedulerFlagResponse(
    val enable: Boolean,
    val support: Boolean
)

data class SchedulerModesResponse(
    val modes: List<SchedulerModeResponse>
)

data class SchedulerModeResponse(
    val color: String,
    val name: String,
    val key: String
)

data class ScheduleListResponse(
    val data: List<ScheduleTemplateSummaryResponse>,
    val enable: Boolean,
    val pollcy: List<SchedulePollcy>
)

data class ScheduleTemplateSummaryResponse(
    val templateName: String,
    val enable: Boolean,
    val templateID: String
)

data class ScheduleTemplateResponse(
    val templateName: String,
    val enable: Boolean,
    val pollcy: List<SchedulePollcy>,
    val content: String
)

data class SchedulePollcy(
    val startH: Int,
    val startM: Int,
    val endH: Int,
    val endM: Int,
    val fdpwr: Int? = null,
    val workMode: String,
    val fdsoc: Int,
    val minsocongrid: Int
)

data class ScheduleSaveRequest(
    val pollcy: List<SchedulePollcy>? = null,
    val templateID: String? = null,
    val deviceSN: String
)

data class ScheduleEnableRequest(
    val templateID: String? = null,
    val deviceSN: String
)

data class ScheduleTemplateListResponse(
    val data: List<ScheduleTemplateSummaryResponse>
)

data class ScheduleTemplateCreateRequest(
    val templateType: Int = 2, // user template type
    val templateName: String,
    val content: String
)
