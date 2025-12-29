package com.alpriest.energystats.services

import com.alpriest.energystats.shared.models.ApiRequestCountResponse
import com.alpriest.energystats.shared.models.BatterySOCResponse
import com.alpriest.energystats.shared.models.ChargeTime
import com.alpriest.energystats.shared.models.DataLoggerResponse
import com.alpriest.energystats.shared.models.DeviceDetailResponse
import com.alpriest.energystats.shared.models.DeviceSettingsItem
import com.alpriest.energystats.shared.models.DeviceSummaryResponse
import com.alpriest.energystats.shared.models.FetchDeviceSettingsItemResponse
import com.alpriest.energystats.shared.models.FetchPeakShavingSettingsResponse
import com.alpriest.energystats.shared.models.GetSchedulerFlagResponse
import com.alpriest.energystats.shared.models.OpenApiVariable
import com.alpriest.energystats.shared.models.OpenHistoryResponse
import com.alpriest.energystats.shared.models.OpenRealQueryResponse
import com.alpriest.energystats.shared.models.OpenReportResponse
import com.alpriest.energystats.shared.models.PowerGenerationResponse
import com.alpriest.energystats.shared.models.PowerStationDetail
import com.alpriest.energystats.shared.models.QueryDate
import com.alpriest.energystats.shared.models.ReportType
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.Schedule
import com.alpriest.energystats.shared.models.ScheduleResponse
import com.alpriest.energystats.shared.services.FoxAPIServicing

interface Networking {
    suspend fun fetchErrorMessages()

    suspend fun fetchDeviceList(): List<DeviceSummaryResponse>
    suspend fun fetchRealData(deviceSN: String, variables: List<String>): OpenRealQueryResponse
    suspend fun fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse
    suspend fun fetchVariables(): List<OpenApiVariable>
    suspend fun fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse>
    suspend fun fetchBatterySettings(deviceSN: String): BatterySOCResponse
    suspend fun setBatterySoc(deviceSN: String, minSOCOnGrid: Int, minSOC: Int)
    suspend fun fetchBatteryTimes(deviceSN: String): List<ChargeTime>
    suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>)
    suspend fun fetchDataLoggers(): List<DataLoggerResponse>
    suspend fun fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse
    suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleResponse
    suspend fun setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean)
    suspend fun saveSchedule(deviceSN: String, schedule: Schedule)
    suspend fun fetchDevice(deviceSN: String): DeviceDetailResponse
    suspend fun fetchPowerStationDetail(): PowerStationDetail?
    suspend fun fetchRequestCount(): ApiRequestCountResponse
    suspend fun fetchDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem): FetchDeviceSettingsItemResponse
    suspend fun setDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem, value: String)
    suspend fun fetchPeakShavingSettings(deviceSN: String): FetchPeakShavingSettingsResponse
    suspend fun setPeakShavingSettings(deviceSN: String, importLimit: Double, soc: Int)
    suspend fun fetchPowerGeneration(deviceSN: String): PowerGenerationResponse
}

open class NetworkService(val api: FoxAPIServicing) : Networking {
    override suspend fun fetchErrorMessages() {
        api.fetchErrorMessages()
    }

    override suspend fun fetchDeviceList(): List<DeviceSummaryResponse> {
        return api.openapi_fetchDeviceList()
    }

    override suspend fun fetchRealData(deviceSN: String, variables: List<String>): OpenRealQueryResponse {
        return api.openapi_fetchRealData(deviceSN, variables)
    }

    override suspend fun fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        return api.openapi_fetchHistory(deviceSN, variables, start, end)
    }

    override suspend fun fetchVariables(): List<OpenApiVariable> {
        return api.openapi_fetchVariables()
    }

    override suspend fun fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        return api.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
    }

    override suspend fun fetchBatterySettings(deviceSN: String): BatterySOCResponse {
        return api.openapi_fetchBatterySettings(deviceSN)
    }

    override suspend fun setBatterySoc(deviceSN: String, minSOCOnGrid: Int, minSOC: Int) {
        api.openapi_setBatterySoc(deviceSN, minSOCOnGrid, minSOC)
    }

    override suspend fun fetchBatteryTimes(deviceSN: String): List<ChargeTime> {
        return api.openapi_fetchBatteryTimes(deviceSN)
    }

    override suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        return api.openapi_setBatteryTimes(deviceSN, times)
    }

    override suspend fun fetchDataLoggers(): List<DataLoggerResponse> {
        return api.openapi_fetchDataLoggers()
    }

    override suspend fun fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse {
        return api.openapi_fetchSchedulerFlag(deviceSN)
    }

    override suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleResponse {
        return api.openapi_fetchCurrentSchedule(deviceSN)
    }

    override suspend fun setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean) {
        api.openapi_setScheduleFlag(deviceSN, schedulerEnabled)
    }

    override suspend fun saveSchedule(deviceSN: String, schedule: Schedule) {
        api.openapi_saveSchedule(deviceSN, schedule)
    }

    override suspend fun fetchDevice(deviceSN: String): DeviceDetailResponse {
        return api.openapi_fetchDevice(deviceSN)
    }

    override suspend fun fetchPowerStationDetail(): PowerStationDetail? {
        val list = api.openapi_fetchPowerStationList()
        return if (list.data.count() == 1) {
            api.openapi_fetchPowerStationDetail(list.data.first().stationID).toPowerStationDetail()
        } else {
            null
        }
    }

    override suspend fun fetchRequestCount(): ApiRequestCountResponse {
        return api.openapi_fetchRequestCount()
    }

    override suspend fun fetchDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem): FetchDeviceSettingsItemResponse {
        return api.openapi_fetchDeviceSettingsItem(deviceSN, item)
    }

    override suspend fun setDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem, value: String) {
        return  api.openapi_setDeviceSettingsItem(deviceSN, item, value)
    }

    override suspend fun fetchPeakShavingSettings(deviceSN: String): FetchPeakShavingSettingsResponse {
        return api.openapi_fetchPeakShavingSettings(deviceSN)
    }

    override suspend fun setPeakShavingSettings(deviceSN: String, importLimit: Double, soc: Int) {
        return api.openapi_setPeakShavingSettings(deviceSN, importLimit, soc)
    }

    override suspend fun fetchPowerGeneration(deviceSN: String): PowerGenerationResponse {
        return api.openapi_fetchPowerGeneration(deviceSN)
    }
}