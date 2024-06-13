package com.alpriest.energystats.services

import com.alpriest.energystats.models.ApiRequestCountResponse
import com.alpriest.energystats.models.BatterySOCResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DataLoggerResponse
import com.alpriest.energystats.models.DeviceDetailResponse
import com.alpriest.energystats.models.DeviceSummaryResponse
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

class InvalidConfigurationException(message: String?) : Exception(message)
class InvalidTokenException : Exception("Invalid Token")
class BadCredentialsException : Exception("Bad Credentials")
class TryLaterException : Exception("Try Later")
class MaintenanceModeException : Exception("Fox servers are offline. Please try later.")
class MissingDataException : Exception("Missing data")
class UnknownNetworkException(errno: Int, message: String?) : Exception("$errno $message")
class UnacceptableException: Exception("Unacceptable")
class ProhibitedActionException: Exception("Schedules")

interface FoxAPIServicing {
    suspend fun fetchErrorMessages()

    suspend fun openapi_fetchDeviceList(): List<DeviceSummaryResponse>
    suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenRealQueryResponse
    suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse
    suspend fun openapi_fetchVariables(): List<OpenApiVariable>
    suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse>
    suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse
    suspend fun openapi_setBatterySoc(deviceSN: String, minSOCOnGrid: Int, minSOC: Int)
    suspend fun openapi_fetchBatteryTimes(deviceSN: String): List<ChargeTime>
    suspend fun openapi_setBatteryTimes(deviceSN: String, times: List<ChargeTime>)
    suspend fun openapi_fetchDataLoggers(): List<DataLoggerResponse>
    suspend fun openapi_fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse
    suspend fun openapi_fetchCurrentSchedule(deviceSN: String): ScheduleResponse
    suspend fun openapi_setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean)
    suspend fun openapi_saveSchedule(deviceSN: String, schedule: Schedule)
    suspend fun openapi_fetchDevice(deviceSN: String): DeviceDetailResponse
    suspend fun openapi_fetchPowerStationList(): PagedPowerStationListResponse
    suspend fun openapi_fetchPowerStationDetail(stationID: String): PowerStationDetailResponse
    suspend fun openapi_fetchRequestCount(): ApiRequestCountResponse
}

data class NetworkResponse<T>(override val errno: Int, val result: T?) : NetworkResponseInterface
