package com.alpriest.energystats.services

import com.alpriest.energystats.models.BatterySOCResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DataLoggerResponse
import com.alpriest.energystats.models.DeviceDetailResponse
import com.alpriest.energystats.models.DeviceSummaryResponse
import com.alpriest.energystats.models.GetSchedulerFlagResponse
import com.alpriest.energystats.models.OpenApiVariable
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ScheduleResponse
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.statsgraph.ReportType
import java.lang.Math.abs
import java.util.Date

data class CachedItem(val item: Any) {
    private val cacheTime: Date = Date()

    fun isFresherThan(seconds: Int): Boolean {
        return abs(Date().time - cacheTime.time) < (seconds * 1000L)
    }
}

class NetworkCache(private val network: FoxESSNetworking) : FoxESSNetworking {
    private var cache: MutableMap<String, CachedItem> = mutableMapOf()
    private val shortCacheDurationInSeconds = 5

    override suspend fun openapi_fetchDeviceList(): List<DeviceSummaryResponse> {
        return network.openapi_fetchDeviceList()
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenQueryResponse {
        val key = makeKey("openapi_fetchRealData", deviceSN, variables.joinToString { it })

        val cached = cache[key]
        return if (cached != null && cached.item is OpenQueryResponse && cached.isFresherThan(seconds = shortCacheDurationInSeconds)) {
            cached.item
        } else {
            val fresh = network.openapi_fetchRealData(deviceSN, variables)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        val key = makeKey("openapi_fetchHistory", deviceSN, variables.joinToString { it }, start.toString(), end.toString())

        val cached = cache[key]
        return if (cached != null && cached.item is OpenHistoryResponse && cached.isFresherThan(seconds = shortCacheDurationInSeconds)) {
            cached.item
        } else {
            val fresh = network.openapi_fetchHistory(deviceSN, variables, start, end)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun openapi_fetchVariables(): List<OpenApiVariable> {
        return network.openapi_fetchVariables()
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        val key = makeKey("openapi_fetchReport", deviceSN, variables.joinToString { it.networkTitle() }, queryDate.toString(), reportType.toString())

        val cached = cache[key]
        return if (cached != null && cached.isFresherThan(seconds = shortCacheDurationInSeconds) && isListOf<OpenReportResponse>(cached.item)) {
            cached.item as List<OpenReportResponse>
        } else {
            val fresh = network.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse {
        return network.openapi_fetchBatterySettings(deviceSN)
    }

    override suspend fun openapi_setBatterySoc(deviceSN: String, minSOCOnGrid: Int, minSOC: Int) {
        network.openapi_setBatterySoc(deviceSN, minSOCOnGrid, minSOC)
    }

    override suspend fun openapi_fetchDataLoggers(): List<DataLoggerResponse> {
        return network.openapi_fetchDataLoggers()
    }

    override suspend fun openapi_fetchBatteryTimes(deviceSN: String): List<ChargeTime> {
        return network.openapi_fetchBatteryTimes(deviceSN)
    }

    override suspend fun openapi_setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        network.openapi_setBatteryTimes(deviceSN, times)
    }

    override suspend fun openapi_fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse {
        return network.openapi_fetchSchedulerFlag(deviceSN)
    }

    override suspend fun openapi_fetchCurrentSchedule(deviceSN: String): ScheduleResponse {
        return network.openapi_fetchCurrentSchedule(deviceSN)
    }

    override suspend fun openapi_setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean) {
        network.openapi_setScheduleFlag(deviceSN, schedulerEnabled)
    }

    override suspend fun openapi_saveSchedule(deviceSN: String, schedule: Schedule) {
        network.openapi_saveSchedule(deviceSN, schedule)
    }

    override suspend fun openapi_fetchDevice(deviceSN: String): DeviceDetailResponse {
        return network.openapi_fetchDevice(deviceSN)
    }

    override suspend fun fetchErrorMessages() {
        network.fetchErrorMessages()
    }

//    override suspend fun fetchSchedulerFlag(deviceSN: String): SchedulerFlagResponse {
//        return network.fetchSchedulerFlag(deviceSN)
//    }

//    override suspend fun fetchScheduleModes(deviceID: String): List<SchedulerModeResponse> {
//        return network.fetchScheduleModes(deviceID)
//    }
//
//    override suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleListResponse {
//        return network.fetchCurrentSchedule(deviceSN)
//    }
//
//    override suspend fun saveSchedule(deviceSN: String, schedule: Schedule) {
//        network.saveSchedule(deviceSN, schedule)
//    }
//
//    override suspend fun deleteSchedule(deviceSN: String) {
//        network.deleteSchedule(deviceSN)
//    }

//    override suspend fun enableScheduleTemplate(deviceSN: String, templateID: String) {
//        network.enableScheduleTemplate(deviceSN, templateID)
//    }
//
//    override suspend fun deleteScheduleTemplate(templateID: String) {
//        network.deleteScheduleTemplate(templateID)
//    }
//
//    override suspend fun fetchScheduleTemplate(deviceSN: String, templateID: String): ScheduleTemplateResponse {
//        return network.fetchScheduleTemplate(deviceSN, templateID)
//    }
//
//    override suspend fun createScheduleTemplate(name: String, description: String) {
//        network.createScheduleTemplate(name, description)
//    }
//
//    override suspend fun fetchScheduleTemplates(): ScheduleTemplateListResponse {
//        return network.fetchScheduleTemplates()
//    }
//
//    override suspend fun saveScheduleTemplate(deviceSN: String, scheduleTemplate: ScheduleTemplate) {
//        network.saveScheduleTemplate(deviceSN, scheduleTemplate)
//    }

    private fun makeKey(base: String, vararg arguments: String): String {
        return listOf(base, *arguments).joinToString(separator = "_")
    }
}

inline fun <reified T> isListOf(obj: Any): Boolean {
    return obj is List<*> && obj.all { it is T }
}
