package com.alpriest.energystats.shared.network

import com.alpriest.energystats.shared.models.network.ApiRequestCountResponse
import com.alpriest.energystats.shared.models.network.BatterySOCResponse
import com.alpriest.energystats.shared.models.network.ChargeTime
import com.alpriest.energystats.shared.models.network.DataLoggerResponse
import com.alpriest.energystats.shared.models.network.DeviceDetailResponse
import com.alpriest.energystats.shared.models.network.DeviceSettingsItem
import com.alpriest.energystats.shared.models.network.DeviceSummaryResponse
import com.alpriest.energystats.shared.models.network.FetchDeviceSettingsItemResponse
import com.alpriest.energystats.shared.models.network.FetchPeakShavingSettingsResponse
import com.alpriest.energystats.shared.models.network.GetSchedulerFlagResponse
import com.alpriest.energystats.shared.models.network.ApiVariable
import com.alpriest.energystats.shared.models.network.OpenHistoryResponse
import com.alpriest.energystats.shared.models.network.OpenRealQueryResponse
import com.alpriest.energystats.shared.models.network.OpenReportResponse
import com.alpriest.energystats.shared.models.network.PagedPowerStationListResponse
import com.alpriest.energystats.shared.models.network.PowerGenerationResponse
import com.alpriest.energystats.shared.models.network.PowerStationDetailResponse
import com.alpriest.energystats.shared.models.QueryDate
import com.alpriest.energystats.shared.models.network.ReportType
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.Schedule
import com.alpriest.energystats.shared.models.network.ScheduleResponse

class InvalidConfigurationException(message: String?) : Exception(message)
class InvalidTokenException : Exception("Invalid Token")
class BadCredentialsException : Exception("Bad Credentials")
class TryLaterException : Exception("You have exceeded your free daily limit of requests. Please try tomorrow.")
class MaintenanceModeException : Exception("Fox servers are offline. Please try later.")
class MissingDataException : Exception("Missing data")
class FoxServerError(val errno: Int, message: String?) : Exception("Fox OpenAPI servers returned error code: $errno $message.")
class UnacceptableException: Exception("Unacceptable")
class ProhibitedActionException: Exception("Schedules")
class UnknownServerError(responseCode: Int): Exception("Fox servers failed with HTTP code $responseCode")

interface FoxAPIServicing {
    suspend fun fetchErrorMessages()

    suspend fun openapi_fetchDeviceList(): List<DeviceSummaryResponse>
    suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenRealQueryResponse
    suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse
    suspend fun openapi_fetchVariables(): List<ApiVariable>
    suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse>
    suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse
    suspend fun openapi_setBatterySoc(deviceSN: String, minSOCOnGrid: Int, minSOC: Int)
    suspend fun openapi_fetchBatteryTimes(deviceSN: String): List<ChargeTime>
    suspend fun openapi_setBatteryTimes(deviceSN: String, times: List<ChargeTime>)
    suspend fun openapi_fetchDataLoggers(): List<DataLoggerResponse>
    suspend fun openapi_fetchDevice(deviceSN: String): DeviceDetailResponse
    suspend fun openapi_fetchPowerStationList(): PagedPowerStationListResponse
    suspend fun openapi_fetchPowerStationDetail(stationID: String): PowerStationDetailResponse
    suspend fun openapi_fetchRequestCount(): ApiRequestCountResponse
    suspend fun openapi_fetchDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem): FetchDeviceSettingsItemResponse
    suspend fun openapi_setDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem, value: String)
    suspend fun openapi_fetchPeakShavingSettings(deviceSN: String): FetchPeakShavingSettingsResponse
    suspend fun openapi_setPeakShavingSettings(deviceSN: String, importLimit: Double, soc: Int)
    suspend fun openapi_fetchPowerGeneration(deviceSN: String): PowerGenerationResponse
    suspend fun openapi_fetchCurrentSchedule(deviceSN: String): ScheduleResponse
    suspend fun openapi_fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse
    suspend fun openapi_setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean)
    suspend fun openapi_saveSchedule(deviceSN: String, schedule: Schedule)
}

data class NetworkResponse<T>(override val errno: Int, val result: T?) : NetworkResponseInterface