package com.alpriest.energystats.services

import com.alpriest.energystats.models.AddressBookResponse
import com.alpriest.energystats.models.BatteryResponse
import com.alpriest.energystats.models.BatterySettingsResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DeviceDetailResponse
import com.alpriest.energystats.models.DeviceSettingsGetResponse
import com.alpriest.energystats.models.OpenApiVariable
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.PagedDataLoggerListResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ScheduleListResponse
import com.alpriest.energystats.models.ScheduleTemplateListResponse
import com.alpriest.energystats.models.ScheduleTemplateResponse
import com.alpriest.energystats.models.SchedulerFlagResponse
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.statsgraph.ReportType
import java.lang.Math.abs
import java.util.Date

data class CachedItem(val item: Any) {
    val cacheTime: Date = Date()

    fun isFresherThan(seconds: Int): Boolean {
        return abs(Date().time - cacheTime.time) < (seconds * 1000L)
    }
}

class NetworkCache(private val network: FoxESSNetworking) : FoxESSNetworking {
    private var cache: MutableMap<String, CachedItem> = mutableMapOf()
    private val shortCacheDurationInSeconds = 5

    override suspend fun openapi_fetchDeviceList(): List<DeviceDetailResponse> {
        return network.openapi_fetchDeviceList()
    }

    override suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        return network.openapi_fetchHistory(deviceSN, variables, start, end)
    }

    override suspend fun openapi_fetchVariables(): List<OpenApiVariable> {
        return network.openapi_fetchVariables()
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        return network.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<Variable>): OpenQueryResponse {
        return network.openapi_fetchRealData(deviceSN, variables)
    }

    override suspend fun fetchBattery(deviceID: String): BatteryResponse {
        val key = makeKey("fetchBattery", deviceID)

        val cached = cache[key]
        return if (cached != null && cached.item is BatteryResponse && cached.isFresherThan(seconds = shortCacheDurationInSeconds)) {
            cached.item
        } else {
            val fresh = network.fetchBattery(deviceID)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse {
        val key = makeKey("fetchBatterySettings", deviceSN)

        val cached = cache[key]
        return if (cached != null && cached.item is BatterySettingsResponse && cached.isFresherThan(seconds = shortCacheDurationInSeconds)) {
            cached.item
        } else {
            val fresh = network.fetchBatterySettings(deviceSN)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun fetchAddressBook(deviceID: String): AddressBookResponse {
        val key = makeKey("fetchAddressBook", deviceID)

        val cached = cache[key]
        return if (cached != null && cached.item is AddressBookResponse && cached.isFresherThan(seconds = shortCacheDurationInSeconds)) {
            cached.item
        } else {
            val fresh = network.fetchAddressBook(deviceID)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String) {
        network.setSoc(minGridSOC, minSOC, deviceSN)
    }

    override suspend fun fetchBatteryTimes(deviceSN: String): BatteryTimesResponse {
        return network.fetchBatteryTimes(deviceSN)
    }

    override suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        network.setBatteryTimes(deviceSN, times)
    }

    override suspend fun fetchDataLoggers(): PagedDataLoggerListResponse {
        return network.fetchDataLoggers()
    }

    override suspend fun fetchErrorMessages() {
        network.fetchErrorMessages()
    }

    override suspend fun fetchSchedulerFlag(deviceSN: String): SchedulerFlagResponse {
        return network.fetchSchedulerFlag(deviceSN)
    }

    override suspend fun fetchScheduleModes(deviceID: String): List<SchedulerModeResponse> {
        return network.fetchScheduleModes(deviceID)
    }

    override suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleListResponse {
        return network.fetchCurrentSchedule(deviceSN)
    }

    override suspend fun saveSchedule(deviceSN: String, schedule: Schedule) {
        network.saveSchedule(deviceSN, schedule)
    }

    override suspend fun deleteSchedule(deviceSN: String) {
        network.deleteSchedule(deviceSN)
    }

    override suspend fun enableScheduleTemplate(deviceSN: String, templateID: String) {
        network.enableScheduleTemplate(deviceSN, templateID)
    }

    override suspend fun deleteScheduleTemplate(templateID: String) {
        network.deleteScheduleTemplate(templateID)
    }

    override suspend fun fetchScheduleTemplate(deviceSN: String, templateID: String): ScheduleTemplateResponse {
        return network.fetchScheduleTemplate(deviceSN, templateID)
    }

    override suspend fun createScheduleTemplate(name: String, description: String) {
        network.createScheduleTemplate(name, description)
    }

    override suspend fun fetchScheduleTemplates(): ScheduleTemplateListResponse {
        return network.fetchScheduleTemplates()
    }

    override suspend fun saveScheduleTemplate(deviceSN: String, scheduleTemplate: ScheduleTemplate) {
        network.saveScheduleTemplate(deviceSN, scheduleTemplate)
    }

    private fun makeKey(base: String, vararg arguments: String): String {
        return listOf(base, *arguments).joinToString(separator = "_")
    }
}