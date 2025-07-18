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
import com.alpriest.energystats.models.PowerGenerationResponse
import com.alpriest.energystats.models.PowerStationDetailResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ScheduleResponse
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.statsgraph.ReportType
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class NetworkFacade(private val api: FoxAPIServicing, private val isDemoUser: () -> Boolean) : FoxAPIServicing {
    private val demoAPI = DemoAPI()
    private val throttler = ThrottleManager()
    private val writeAPIkey = "writeable-method"

    override suspend fun openapi_fetchDeviceList(): List<DeviceSummaryResponse> {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchDeviceList()
        } else {
            api.openapi_fetchDeviceList()
        }
    }

    override suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchHistory(deviceSN, variables, start, end)
        } else {
            val method = "openapi_fetchHistory"
            try {
                throttler.throttle(method)
                return api.openapi_fetchHistory(deviceSN, variables, start, end)
            } finally {
                throttler.didInvoke(method)
            }
        }
    }

    override suspend fun openapi_fetchVariables(): List<OpenApiVariable> {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchVariables()
        } else {
            val method = "openapi_fetchVariables"
            try {
                throttler.throttle(method)
                return api.openapi_fetchVariables()
            } finally {
                throttler.didInvoke(method)
            }
        }
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
        } else {
            val method = "openapi_fetchReport"
            try {
                throttler.throttle(method)
                return api.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
            } finally {
                throttler.didInvoke(method)
            }
        }
    }

    override suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchBatterySettings(deviceSN)
        } else {
            return api.openapi_fetchBatterySettings(deviceSN)
        }
    }

    override suspend fun openapi_setBatterySoc(deviceSN: String, minSOCOnGrid: Int, minSOC: Int) {
        return if (isDemoUser()) {
            demoAPI.openapi_setBatterySoc(deviceSN, minSOCOnGrid, minSOC)
        } else {
            try {
                throttler.throttle(writeAPIkey, seconds = 2)
                return api.openapi_setBatterySoc(deviceSN, minSOCOnGrid, minSOC)
            } finally {
                throttler.didInvoke(writeAPIkey)
            }
        }
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenRealQueryResponse {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchRealData(deviceSN, variables)
        } else {
            val method = "openapi_fetchRealData"
            try {
                throttler.throttle(method)
                api.openapi_fetchRealData(deviceSN, variables)
            } finally {
                throttler.didInvoke(method)
            }
        }
    }

    override suspend fun openapi_fetchDataLoggers(): List<DataLoggerResponse> {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchDataLoggers()
        } else {
            api.openapi_fetchDataLoggers()
        }
    }

    override suspend fun openapi_fetchBatteryTimes(deviceSN: String): List<ChargeTime> {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchBatteryTimes(deviceSN)
        } else {
            api.openapi_fetchBatteryTimes(deviceSN)
        }
    }

    override suspend fun openapi_setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        return if (isDemoUser()) {
            demoAPI.openapi_setBatteryTimes(deviceSN, times)
        } else {
            try {
                throttler.throttle(writeAPIkey, seconds = 2)
                api.openapi_setBatteryTimes(deviceSN, times)
            } finally {
                throttler.didInvoke(writeAPIkey)
            }
        }
    }

    override suspend fun openapi_fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchSchedulerFlag(deviceSN)
        } else {
            api.openapi_fetchSchedulerFlag(deviceSN)
        }
    }

    override suspend fun openapi_fetchCurrentSchedule(deviceSN: String): ScheduleResponse {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchCurrentSchedule(deviceSN)
        } else {
            val method = "openapi_fetchCurrentSchedule"
            try {
                throttler.throttle(method)
                api.openapi_fetchCurrentSchedule(deviceSN)
            } finally {
                throttler.didInvoke(method)
            }
        }
    }

    override suspend fun openapi_setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean) {
        if (isDemoUser()) {
            demoAPI.openapi_setScheduleFlag(deviceSN, schedulerEnabled)
        } else {
            try {
                throttler.throttle(writeAPIkey, seconds = 2)
                return api.openapi_setScheduleFlag(deviceSN, schedulerEnabled)
            } finally {
                throttler.didInvoke(writeAPIkey)
            }
        }
    }

    override suspend fun openapi_saveSchedule(deviceSN: String, schedule: Schedule) {
        if (isDemoUser()) {
            demoAPI.openapi_saveSchedule(deviceSN, schedule)
        } else {
            try {
                throttler.throttle(writeAPIkey, seconds = 2)
                return api.openapi_saveSchedule(deviceSN, schedule)
            } finally {
                throttler.didInvoke(writeAPIkey)
            }
        }
    }

    override suspend fun openapi_fetchDevice(deviceSN: String): DeviceDetailResponse {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchDevice(deviceSN)
        } else {
            api.openapi_fetchDevice(deviceSN)
        }
    }

    override suspend fun openapi_fetchPowerStationList(): PagedPowerStationListResponse {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchPowerStationList()
        } else {
            api.openapi_fetchPowerStationList()
        }
    }

    override suspend fun openapi_fetchPowerStationDetail(stationID: String): PowerStationDetailResponse {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchPowerStationDetail(stationID)
        } else {
            api.openapi_fetchPowerStationDetail(stationID)
        }
    }

    override suspend fun openapi_fetchRequestCount(): ApiRequestCountResponse {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchRequestCount()
        } else {
            api.openapi_fetchRequestCount()
        }
    }

    override suspend fun openapi_fetchDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem): FetchDeviceSettingsItemResponse {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchDeviceSettingsItem(deviceSN, item)
        } else {
            api.openapi_fetchDeviceSettingsItem(deviceSN, item)
        }
    }

    override suspend fun openapi_setDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem, value: String) {
        return if (isDemoUser()) {
            demoAPI.openapi_setDeviceSettingsItem(deviceSN, item, value)
        } else {
            api.openapi_setDeviceSettingsItem(deviceSN, item, value)
        }
    }

    override suspend fun fetchErrorMessages() {
        if (isDemoUser()) {
            demoAPI.fetchErrorMessages()
        } else {
            api.fetchErrorMessages()
        }
    }

    override suspend fun openapi_fetchPeakShavingSettings(deviceSN: String): FetchPeakShavingSettingsResponse {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchPeakShavingSettings(deviceSN)
        } else {
            api.openapi_fetchPeakShavingSettings(deviceSN)
        }
    }

    override suspend fun openapi_setPeakShavingSettings(deviceSN: String, importLimit: Double, soc: Int) {
        if (isDemoUser()) {
            demoAPI.openapi_setPeakShavingSettings(deviceSN, importLimit, soc)
        } else {
            api.openapi_setPeakShavingSettings(deviceSN, importLimit, soc)
        }
    }

    override suspend fun openapi_fetchPowerGeneration(deviceSN: String): PowerGenerationResponse {
        return if (isDemoUser()) {
            demoAPI.openapi_fetchPowerGeneration(deviceSN)
        } else {
            api.openapi_fetchPowerGeneration(deviceSN)
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