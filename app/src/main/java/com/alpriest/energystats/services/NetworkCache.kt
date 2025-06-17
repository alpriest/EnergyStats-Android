package com.alpriest.energystats.services

import com.alpriest.energystats.models.ApiRequestCountResponse
import com.alpriest.energystats.models.BatterySOCResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DataLoggerResponse
import com.alpriest.energystats.models.DeviceDetailResponse
import com.alpriest.energystats.models.DeviceSettingsItem
import com.alpriest.energystats.models.DeviceSummaryResponse
import com.alpriest.energystats.models.FetchDeviceSettingsItemResponse
import com.alpriest.energystats.models.FetchPeakShavingSettingsResponse
import com.alpriest.energystats.models.GetSchedulerFlagResponse
import com.alpriest.energystats.models.OpenApiVariable
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.OpenRealQueryResponse
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.PagedPowerStationListResponse
import com.alpriest.energystats.models.PowerStationDetailResponse
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

class NetworkCache(private val api: FoxAPIServicing) : FoxAPIServicing {
    private var cache: MutableMap<String, CachedItem> = mutableMapOf()
    private val shortCacheDurationInSeconds = 5
    private val longCacheDurationInSeconds = 300

    override suspend fun openapi_fetchDeviceList(): List<DeviceSummaryResponse> {
        val key = makeKey(currentFunctionName())

        val cached = cache[key]
        return if (cached != null && cached.isFresherThan(seconds = longCacheDurationInSeconds) && isListOf<DeviceSummaryResponse>(cached)) {
            @Suppress("UNCHECKED_CAST")
            cached.item as List<DeviceSummaryResponse>
        } else {
            val fresh = api.openapi_fetchDeviceList()
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenRealQueryResponse {
        val key = makeKey(currentFunctionName(), deviceSN, variables.sorted().joinToString { it })

        val cached = cache[key]
        return if (cached != null && cached.isFresherThan(seconds = shortCacheDurationInSeconds) && cached.item is OpenRealQueryResponse) {
            cached.item
        } else {
            val fresh = api.openapi_fetchRealData(deviceSN, variables)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        val key = makeKey(currentFunctionName(), deviceSN, variables.sorted().joinToString { it }, start.toString(), end.toString())

        val cached = cache[key]
        return if (cached != null && cached.isFresherThan(seconds = shortCacheDurationInSeconds) && cached.item is OpenHistoryResponse) {
            cached.item
        } else {
            val fresh = api.openapi_fetchHistory(deviceSN, variables, start, end)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun openapi_fetchVariables(): List<OpenApiVariable> {
        val key = makeKey(currentFunctionName())

        val cached = cache[key]
        return if (cached != null && cached.isFresherThan(seconds = longCacheDurationInSeconds) && isListOf<OpenApiVariable>(cached)) {
            @Suppress("UNCHECKED_CAST")
            cached.item as List<OpenApiVariable>
        } else {
            val fresh = api.openapi_fetchVariables()
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        val key = makeKey(currentFunctionName(), deviceSN, variables.sorted().joinToString { it.networkTitle() }, queryDate.toString(), reportType.toString())

        val cached = cache[key]
        return if (cached != null && cached.isFresherThan(seconds = shortCacheDurationInSeconds) && isListOf<OpenReportResponse>(cached.item)) {
            @Suppress("UNCHECKED_CAST")
            cached.item as List<OpenReportResponse>
        } else {
            val fresh = api.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse {
        return api.openapi_fetchBatterySettings(deviceSN)
    }

    override suspend fun openapi_setBatterySoc(deviceSN: String, minSOCOnGrid: Int, minSOC: Int) {
        api.openapi_setBatterySoc(deviceSN, minSOCOnGrid, minSOC)
    }

    override suspend fun openapi_fetchDataLoggers(): List<DataLoggerResponse> {
        val key = makeKey(currentFunctionName())

        val cached = cache[key]
        return if (cached != null && cached.isFresherThan(seconds = shortCacheDurationInSeconds) && isListOf<DataLoggerResponse>(cached.item)) {
            @Suppress("UNCHECKED_CAST")
            cached.item as List<DataLoggerResponse>
        } else {
            val fresh = api.openapi_fetchDataLoggers()
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun openapi_fetchBatteryTimes(deviceSN: String): List<ChargeTime> {
        return api.openapi_fetchBatteryTimes(deviceSN)
    }

    override suspend fun openapi_setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        api.openapi_setBatteryTimes(deviceSN, times)
    }

    override suspend fun openapi_fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse {
        return api.openapi_fetchSchedulerFlag(deviceSN)
    }

    override suspend fun openapi_fetchCurrentSchedule(deviceSN: String): ScheduleResponse {
        return api.openapi_fetchCurrentSchedule(deviceSN)
    }

    override suspend fun openapi_setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean) {
        api.openapi_setScheduleFlag(deviceSN, schedulerEnabled)
    }

    override suspend fun openapi_saveSchedule(deviceSN: String, schedule: Schedule) {
        api.openapi_saveSchedule(deviceSN, schedule)
    }

    override suspend fun openapi_fetchDevice(deviceSN: String): DeviceDetailResponse {
        val key = makeKey(currentFunctionName(), deviceSN)

        val cached = cache[key]
        return if (cached != null && cached.isFresherThan(seconds = shortCacheDurationInSeconds) && cached.item is DeviceDetailResponse) {
            cached.item
        } else {
            val fresh = api.openapi_fetchDevice(deviceSN)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun openapi_fetchPowerStationList(): PagedPowerStationListResponse {
        val key = makeKey(currentFunctionName())

        val cached = cache[key]
        return if (cached != null && cached.isFresherThan(seconds = longCacheDurationInSeconds) && cached.item is PagedPowerStationListResponse) {
            cached.item
        } else {
            val fresh = api.openapi_fetchPowerStationList()
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun openapi_fetchPowerStationDetail(stationID: String): PowerStationDetailResponse {
        val key = makeKey(currentFunctionName())

        val cached = cache[key]
        return if (cached != null && cached.isFresherThan(seconds = longCacheDurationInSeconds) && cached.item is PowerStationDetailResponse) {
            cached.item
        } else {
            val fresh = api.openapi_fetchPowerStationDetail(stationID)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun openapi_fetchRequestCount(): ApiRequestCountResponse {
        return api.openapi_fetchRequestCount()
    }

    override suspend fun openapi_fetchDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem): FetchDeviceSettingsItemResponse {
        return api.openapi_fetchDeviceSettingsItem(deviceSN, item)
    }

    override suspend fun openapi_setDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem, value: String) {
        return api.openapi_setDeviceSettingsItem(deviceSN, item, value)
    }

    override suspend fun fetchErrorMessages() {
        api.fetchErrorMessages()
    }

    override suspend fun openapi_fetchPeakShavingSettings(deviceSN: String): FetchPeakShavingSettingsResponse {
        return api.openapi_fetchPeakShavingSettings(deviceSN)
    }

    override suspend fun openapi_setPeakShavingSettings(deviceSN: String, importLimit: Double, soc: Int) {
        return api.openapi_setPeakShavingSettings(deviceSN, importLimit, soc)
    }

    private fun makeKey(base: String, vararg arguments: String): String {
        return listOf(base, *arguments).joinToString(separator = "_")
    }
}

inline fun <reified T> isListOf(obj: Any): Boolean {
    return obj is List<*> && obj.all { it is T }
}

fun currentFunctionName(): String {
    return Throwable().stackTrace[1].methodName
}