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

interface FoxESSNetworking {
    suspend fun fetchDeviceList(): PagedDeviceListResponse
    suspend fun ensureHasToken()
    suspend fun verifyCredentials(username: String, password: String)
    suspend fun fetchBattery(deviceID: String): BatteryResponse
    suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse
    suspend fun fetchRaw(deviceID: String, variables: List<RawVariable>, queryDate: QueryDate): ArrayList<RawResponse>
    suspend fun fetchReport(deviceID: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): ArrayList<ReportResponse>
    suspend fun fetchAddressBook(deviceID: String): AddressBookResponse
    suspend fun fetchVariables(deviceID: String): List<RawVariable>
    suspend fun fetchEarnings(deviceID: String): EarningsResponse
    suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String)
    suspend fun fetchBatteryTimes(deviceSN: String): BatteryTimesResponse
    suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>)
    suspend fun fetchWorkMode(deviceID: String): DeviceSettingsGetResponse
    suspend fun setWorkMode(deviceID: String, workMode: String)
    suspend fun fetchDataLoggers(): PagedDataLoggerListResponse
    suspend fun fetchErrorMessages()

    suspend fun fetchSchedulerFlag(deviceSN: String): SchedulerFlagResponse
    suspend fun fetchScheduleModes(deviceID: String): List<SchedulerModeResponse>
    suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleListResponse
    suspend fun saveSchedule(deviceSN: String, schedule: Schedule)
    suspend fun deleteSchedule(deviceSN: String)
    suspend fun enableScheduleTemplate(deviceSN: String, templateID: String)
    suspend fun fetchScheduleTemplate(deviceSN: String, templateID: String): ScheduleTemplateResponse
    suspend fun deleteScheduleTemplate(templateID: String)
    suspend fun createScheduleTemplate(name: String, description: String)
    suspend fun fetchScheduleTemplates(): ScheduleTemplateListResponse
    suspend fun saveScheduleTemplate(deviceSN: String, scheduleTemplate: ScheduleTemplate)
}

data class NetworkResponse<T>(override val errno: Int, val result: T?) : NetworkResponseInterface
data class NetworkRawResponse(override val errno: Int, val result: ArrayList<RawResponse>?) : NetworkResponseInterface
data class NetworkReportResponse(override val errno: Int, val result: ArrayList<ReportResponse>?) : NetworkResponseInterface
