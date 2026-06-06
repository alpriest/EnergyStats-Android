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
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class NetworkThrottlerFacade(private val api: FoxAPIServicing) : FoxAPIServicing {
    private val throttler = ThrottleManager()
    private val writeAPIkey = "writeable-method"

    private suspend fun <T> throttled(
        method: String,
        seconds: Int = 1,
        block: suspend () -> T,
    ): T {
        try {
            throttler.throttle(method, seconds = seconds)
            return block()
        } finally {
            throttler.didInvoke(method)
        }
    }

    override suspend fun openapi_fetchDeviceList(): List<DeviceSummaryResponse> {
        return api.openapi_fetchDeviceList()
    }

    override suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        return throttled("openapi_fetchHistory") {
            api.openapi_fetchHistory(deviceSN, variables, start, end)
        }
    }

    override suspend fun openapi_fetchVariables(): List<ApiVariable> {
        return throttled("openapi_fetchVariables") {
            api.openapi_fetchVariables()
        }
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        return throttled("openapi_fetchReport") {
            api.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
        }
    }

    override suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse {
        return throttled("openapi_fetchBatterySettings") {
            api.openapi_fetchBatterySettings(deviceSN)
        }
    }

    override suspend fun openapi_setBatterySoc(deviceSN: String, minSOCOnGrid: Int, minSOC: Int) {
        return throttled(writeAPIkey, 2) {
            api.openapi_setBatterySoc(deviceSN, minSOCOnGrid, minSOC)
        }
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenRealQueryResponse {
        return throttled("openapi_fetchRealData") {
            api.openapi_fetchRealData(deviceSN, variables)
        }
    }

    override suspend fun openapi_fetchDataLoggers(): List<DataLoggerResponse> {
        return throttled("openapi_fetchDataLoggers") {
            api.openapi_fetchDataLoggers()
        }
    }

    override suspend fun openapi_fetchBatteryTimes(deviceSN: String): List<ChargeTime> {
        return throttled("openapi_fetchBatteryTimes") {
            api.openapi_fetchBatteryTimes(deviceSN)
        }
    }

    override suspend fun openapi_setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        return throttled(writeAPIkey) {
            api.openapi_setBatteryTimes(deviceSN, times)
        }
    }

    override suspend fun openapi_fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse {
        return throttled("openapi_fetchSchedulerFlag") {
            api.openapi_fetchSchedulerFlag(deviceSN)
        }
    }

    override suspend fun openapi_fetchCurrentSchedule(deviceSN: String): ScheduleResponse {
        return throttled("openapi_fetchCurrentSchedule") {
            api.openapi_fetchCurrentSchedule(deviceSN)
        }
    }

    override suspend fun openapi_setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean) {
        return throttled(writeAPIkey) {
            api.openapi_setScheduleFlag(deviceSN, schedulerEnabled)
        }
    }

    override suspend fun openapi_saveSchedule(deviceSN: String, schedule: ScheduleV3) {
        return throttled(writeAPIkey) {
            api.openapi_saveSchedule(deviceSN, schedule)
        }
    }

    override suspend fun openapi_fetchDevice(deviceSN: String): DeviceDetailResponse {
        return throttled("openapi_fetchDevice") {
            api.openapi_fetchDevice(deviceSN)
        }
    }

    override suspend fun openapi_fetchPowerStationList(): PagedPowerStationListResponse {
        return throttled("openapi_fetchPowerStationList") {
            api.openapi_fetchPowerStationList()
        }
    }

    override suspend fun openapi_fetchPowerStationDetail(stationID: String): PowerStationDetailResponse {
        return throttled("openapi_fetchPowerStationDetail") {
            api.openapi_fetchPowerStationDetail(stationID)
        }
    }

    override suspend fun openapi_fetchRequestCount(): ApiRequestCountResponse {
        return throttled("openapi_fetchRequestCount") {
            api.openapi_fetchRequestCount()
        }
    }

    override suspend fun openapi_fetchDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem): FetchDeviceSettingsItemResponse {
        return throttled("openapi_fetchDeviceSettingsItem") {
            api.openapi_fetchDeviceSettingsItem(deviceSN, item)
        }
    }

    override suspend fun openapi_setDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem, value: String) {
        return throttled(writeAPIkey) {
            api.openapi_setDeviceSettingsItem(deviceSN, item, value)
        }
    }

    override suspend fun fetchErrorMessages() {
        return throttled("fetchErrorMessages") {
            api.fetchErrorMessages()
        }
    }

    override suspend fun openapi_fetchPeakShavingSettings(deviceSN: String): FetchPeakShavingSettingsResponse {
        return throttled("openapi_fetchPeakShavingSettings") {
            api.openapi_fetchPeakShavingSettings(deviceSN)
        }
    }

    override suspend fun openapi_setPeakShavingSettings(deviceSN: String, importLimit: Double, soc: Int) {
        return throttled(writeAPIkey) {
            api.openapi_setPeakShavingSettings(deviceSN, importLimit, soc)
        }
    }

    override suspend fun openapi_fetchPowerGeneration(deviceSN: String): PowerGenerationResponse {
        return throttled("openapi_fetchPowerGeneration") {
            api.openapi_fetchPowerGeneration(deviceSN)
        }
    }

    override suspend fun openapi_getBatteryHeatingSchedule(deviceSN: String): BatteryHeatingScheduleResponse {
        return throttled("openapi_getBatteryHeatingSchedule") {
            api.openapi_getBatteryHeatingSchedule(deviceSN)
        }
    }

    override suspend fun openapi_setBatteryHeatingSchedule(schedule: BatteryHeatingScheduleRequest) {
        return throttled(writeAPIkey) {
            api.openapi_setBatteryHeatingSchedule(schedule)
        }
    }
}

class ThrottleManager {
    private val lastCallTimes: ConcurrentHashMap<String, Long> = ConcurrentHashMap()

    suspend fun throttle(method: String, seconds: Int = 1) {
        val now = System.nanoTime()
        val lastCallTime = lastCallTimes[method]

        if (lastCallTime != null) {
            val timeSinceLastCall = now - lastCallTime
            val waitTime = (seconds * 1_000_000_000) - timeSinceLastCall  // 1 second in nanoseconds

            if (waitTime > 0) {
                delay(waitTime / (seconds * 1_000_000))  // Convert nanoseconds to milliseconds for delay
            }
        }

        lastCallTimes[method] = now
    }

    fun didInvoke(method: String) {
        lastCallTimes[method] = System.nanoTime()
    }
}