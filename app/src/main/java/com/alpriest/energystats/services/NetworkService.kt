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
import com.alpriest.energystats.models.PowerStationDetail
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ScheduleResponse
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.statsgraph.ReportType

interface Networking {
    suspend fun fetchErrorMessages()

    suspend fun fetchDeviceList(): List<DeviceSummaryResponse>
    suspend fun fetchRealData(deviceSN: String, variables: List<String>): OpenQueryResponse
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
}

open class NetworkService(val api: FoxAPIServicing) : Networking {
    override suspend fun fetchErrorMessages() {
        api.fetchErrorMessages()
    }

    override suspend fun fetchDeviceList(): List<DeviceSummaryResponse> {
        return api.openapi_fetchDeviceList()
    }

    override suspend fun fetchRealData(deviceSN: String, variables: List<String>): OpenQueryResponse {
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
}