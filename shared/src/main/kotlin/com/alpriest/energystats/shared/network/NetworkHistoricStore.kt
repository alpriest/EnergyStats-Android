package com.alpriest.energystats.shared.network

import com.alpriest.energystats.shared.models.QueryDate
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.ScheduleV3
import com.alpriest.energystats.shared.models.network.ApiRequestCountResponse
import com.alpriest.energystats.shared.models.network.ApiVariable
import com.alpriest.energystats.shared.models.network.BatteryHeatingScheduleRequest
import com.alpriest.energystats.shared.models.network.BatteryHeatingScheduleResponse
import com.alpriest.energystats.shared.models.network.BatterySOCResponse
import com.alpriest.energystats.shared.models.network.ChargeTime
import com.alpriest.energystats.shared.models.network.DataLoggerResponse
import com.alpriest.energystats.shared.models.network.DeviceDetailResponse
import com.alpriest.energystats.shared.models.network.DeviceSettingsItem
import com.alpriest.energystats.shared.models.network.DeviceSummaryResponse
import com.alpriest.energystats.shared.models.network.FetchDeviceSettingsItemResponse
import com.alpriest.energystats.shared.models.network.FetchPeakShavingSettingsResponse
import com.alpriest.energystats.shared.models.network.GetSchedulerFlagResponse
import com.alpriest.energystats.shared.models.network.OpenHistoryResponse
import com.alpriest.energystats.shared.models.network.OpenRealQueryResponse
import com.alpriest.energystats.shared.models.network.OpenReportResponse
import com.alpriest.energystats.shared.models.network.PagedPowerStationListResponse
import com.alpriest.energystats.shared.models.network.PowerGenerationResponse
import com.alpriest.energystats.shared.models.network.PowerStationDetailResponse
import com.alpriest.energystats.shared.models.network.ReportType
import com.alpriest.energystats.shared.models.network.ScheduleResponse
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest
import java.time.LocalDate

class NetworkHistoricStore(
    private val api: FoxAPIServicing,
    private val cacheDirectory: File
) : FoxAPIServicing {
    private val reportCache = mutableMapOf<String, List<OpenReportResponse>>()

    override suspend fun fetchErrorMessages() {
        api.fetchErrorMessages()
    }

    override suspend fun openapi_fetchDeviceList(): List<DeviceSummaryResponse> =
        api.openapi_fetchDeviceList()

    override suspend fun openapi_fetchDevice(deviceSN: String): DeviceDetailResponse =
        api.openapi_fetchDevice(deviceSN)

    override suspend fun openapi_fetchRealData(
        deviceSN: String,
        variables: List<String>
    ): OpenRealQueryResponse =
        api.openapi_fetchRealData(deviceSN, variables)

    override suspend fun openapi_fetchHistory(
        deviceSN: String,
        variables: List<String>,
        start: Long,
        end: Long
    ): OpenHistoryResponse =
        api.openapi_fetchHistory(deviceSN, variables, start, end)

    override suspend fun openapi_fetchVariables(): List<ApiVariable> =
        api.openapi_fetchVariables()

    override suspend fun openapi_fetchReport(
        deviceSN: String,
        variables: List<ReportVariable>,
        queryDate: QueryDate,
        reportType: ReportType
    ): List<OpenReportResponse> {
        if (isCurrentReportPeriod(queryDate, reportType)) {
            return api.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
        }

        val key = makeReportCacheKey(deviceSN, variables, queryDate, reportType)

        reportCache[key]?.let { return it }

        val file = makeReportCacheFile(key)

        if (file.exists()) {
            val cached = Json.Default.decodeFromString<List<OpenReportResponse>>(file.readText())
            reportCache[key] = cached
            return cached
        }

        val fresh = api.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
        reportCache[key] = fresh

        file.parentFile?.mkdirs()
        file.writeText(Json.Default.encodeToString(fresh))

        return fresh
    }

    override suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse =
        api.openapi_fetchBatterySettings(deviceSN)

    override suspend fun openapi_setBatterySoc(
        deviceSN: String,
        minSOCOnGrid: Int,
        minSOC: Int
    ) {
        api.openapi_setBatterySoc(deviceSN, minSOCOnGrid, minSOC)
    }

    override suspend fun openapi_fetchBatteryTimes(deviceSN: String): List<ChargeTime> =
        api.openapi_fetchBatteryTimes(deviceSN)

    override suspend fun openapi_setBatteryTimes(
        deviceSN: String,
        times: List<ChargeTime>
    ) {
        api.openapi_setBatteryTimes(deviceSN, times)
    }

    override suspend fun openapi_fetchDataLoggers(): List<DataLoggerResponse> =
        api.openapi_fetchDataLoggers()

    override suspend fun openapi_fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse =
        api.openapi_fetchSchedulerFlag(deviceSN)

    override suspend fun openapi_fetchCurrentSchedule(deviceSN: String): ScheduleResponse =
        api.openapi_fetchCurrentSchedule(deviceSN)

    override suspend fun openapi_setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean) {
        api.openapi_setScheduleFlag(deviceSN, schedulerEnabled)
    }

    override suspend fun openapi_saveSchedule(deviceSN: String, schedule: ScheduleV3) {
        api.openapi_saveSchedule(deviceSN, schedule)
    }

    override suspend fun openapi_fetchPowerStationList(): PagedPowerStationListResponse =
        api.openapi_fetchPowerStationList()

    override suspend fun openapi_fetchPowerStationDetail(stationID: String): PowerStationDetailResponse =
        api.openapi_fetchPowerStationDetail(stationID)

    override suspend fun openapi_fetchRequestCount(): ApiRequestCountResponse =
        api.openapi_fetchRequestCount()

    override suspend fun openapi_fetchDeviceSettingsItem(
        deviceSN: String,
        item: DeviceSettingsItem
    ): FetchDeviceSettingsItemResponse =
        api.openapi_fetchDeviceSettingsItem(deviceSN, item)

    override suspend fun openapi_setDeviceSettingsItem(
        deviceSN: String,
        item: DeviceSettingsItem,
        value: String
    ) {
        api.openapi_setDeviceSettingsItem(deviceSN, item, value)
    }

    override suspend fun openapi_fetchPeakShavingSettings(deviceSN: String): FetchPeakShavingSettingsResponse =
        api.openapi_fetchPeakShavingSettings(deviceSN)

    override suspend fun openapi_setPeakShavingSettings(
        deviceSN: String,
        importLimit: Double,
        soc: Int
    ) {
        api.openapi_setPeakShavingSettings(deviceSN, importLimit, soc)
    }

    override suspend fun openapi_fetchPowerGeneration(deviceSN: String): PowerGenerationResponse =
        api.openapi_fetchPowerGeneration(deviceSN)

    override suspend fun openapi_getBatteryHeatingSchedule(deviceSN: String): BatteryHeatingScheduleResponse =
        api.openapi_getBatteryHeatingSchedule(deviceSN)

    override suspend fun openapi_setBatteryHeatingSchedule(
        schedule: BatteryHeatingScheduleRequest
    ) {
        api.openapi_setBatteryHeatingSchedule(schedule)
    }

    fun clear() {
        reportCache.clear()

        if (cacheDirectory.exists()) {
            cacheDirectory.deleteRecursively()
        }
    }

    private fun makeReportCacheFile(key: String): File =
        File(cacheDirectory, "${safeFileName(key)}.json")

    private fun makeReportCacheKey(
        deviceSN: String,
        variables: List<ReportVariable>,
        queryDate: QueryDate,
        reportType: ReportType
    ): String =
        listOf(
            deviceSN,
            variables.sorted().joinToString(",") { it.networkTitle() },
            "${queryDate.year}-${queryDate.month ?: 0}-${queryDate.day ?: 0}",
            reportType.name
        ).joinToString("_")

    private fun isCurrentReportPeriod(
        queryDate: QueryDate,
        reportType: ReportType
    ): Boolean {
        val now = QueryDate.from(LocalDate.now())

        return when (reportType) {
            ReportType.year ->
                queryDate.year == now.year

            ReportType.month ->
                queryDate.year == now.year &&
                        queryDate.month == now.month

            ReportType.day ->
                queryDate.year == now.year &&
                        queryDate.month == now.month &&
                        queryDate.day == now.day
        }
    }

    private fun safeFileName(value: String): String {
        val digest = MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray())

        return digest.joinToString("") { "%02x".format(it) }
    }
}