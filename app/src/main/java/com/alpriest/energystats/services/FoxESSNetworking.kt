package com.alpriest.energystats.services

import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.statsgraph.ReportType

class InvalidConfigurationException(message: String?) : Exception(message)
class InvalidTokenException : Exception("Invalid Token")
class BadCredentialsException : Exception("Bad Credentials")
class TryLaterException : Exception("Try Later")
class MaintenanceModeException : Exception("Fox servers are offline. Please try later.")
class MissingDataException : Exception("Missing data")
class UnknownNetworkException(errno: Int, message: String?) : Exception("$errno $message")
class UnacceptableException: Exception("Unacceptable")

interface FoxESSNetworking {
//    suspend fun fetchBattery(deviceID: String): BatteryResponse
//    suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse
//    suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String)
//    suspend fun fetchBatteryTimes(deviceSN: String): BatteryTimesResponse
//    suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>)
//    suspend fun fetchDataLoggers(): PagedDataLoggerListResponse
    suspend fun fetchErrorMessages()

//    suspend fun fetchSchedulerFlag(deviceSN: String): SchedulerFlagResponse
//    suspend fun fetchScheduleModes(deviceID: String): List<SchedulerModeResponse>
//    suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleListResponse
//    suspend fun saveSchedule(deviceSN: String, schedule: Schedule)
//    suspend fun deleteSchedule(deviceSN: String)
//    suspend fun enableScheduleTemplate(deviceSN: String, templateID: String)
//    suspend fun fetchScheduleTemplate(deviceSN: String, templateID: String): ScheduleTemplateResponse
//    suspend fun deleteScheduleTemplate(templateID: String)
//    suspend fun createScheduleTemplate(name: String, description: String)
//    suspend fun fetchScheduleTemplates(): ScheduleTemplateListResponse
//    suspend fun saveScheduleTemplate(deviceSN: String, scheduleTemplate: ScheduleTemplate)

    // OpenAPI
    suspend fun openapi_fetchDeviceList(): List<DeviceDetailResponse>
    suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenQueryResponse
    suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse
    suspend fun openapi_fetchVariables(): List<OpenApiVariable>
    suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse>
    suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse
}

data class NetworkResponse<T>(override val errno: Int, val result: T?) : NetworkResponseInterface
data class NetworkRawResponse(override val errno: Int, val result: ArrayList<RawResponse>?) : NetworkResponseInterface
data class NetworkReportResponse(override val errno: Int, val result: ArrayList<ReportResponse>?) : NetworkResponseInterface
