package com.alpriest.energystats.services

import com.alpriest.energystats.models.AddressBookResponse
import com.alpriest.energystats.models.BatteryResponse
import com.alpriest.energystats.models.BatterySOCResponse
import com.alpriest.energystats.models.BatterySettingsResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DeviceDetailResponse
import com.alpriest.energystats.models.OpenApiVariable
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.PagedDataLoggerListResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ScheduleListResponse
import com.alpriest.energystats.models.ScheduleTemplateListResponse
import com.alpriest.energystats.models.ScheduleTemplateResponse
import com.alpriest.energystats.models.SchedulerFlagResponse
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.models.rounded
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.statsgraph.ReportType
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

class NetworkValueCleaner(private val network: FoxESSNetworking, private val themeStream: MutableStateFlow<AppTheme>) : FoxESSNetworking {
    override suspend fun openapi_fetchDeviceList(): List<DeviceDetailResponse> {
        return network.openapi_fetchDeviceList()
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenQueryResponse {
        return network.openapi_fetchRealData(deviceSN, variables)
    }

    override suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        return network.openapi_fetchHistory(deviceSN, variables, start, end)
    }

//    override suspend fun fetchBattery(deviceID: String): BatteryResponse {
//        return network.fetchBattery(deviceID)
//    }
//
//    override suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse {
//        return network.fetchBatterySettings(deviceSN)
//    }

    override suspend fun openapi_fetchVariables(): List<OpenApiVariable> {
        return network.openapi_fetchVariables()
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        return network.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
    }

    override suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse {
        return network.openapi_fetchBatterySettings(deviceSN)
    }

//    override suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String) {
//        network.setSoc(minGridSOC, minSOC, deviceSN)
//    }
//
//    override suspend fun fetchBatteryTimes(deviceSN: String): BatteryTimesResponse {
//        return network.fetchBatteryTimes(deviceSN)
//    }

//    override suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
//        network.setBatteryTimes(deviceSN, times)
//    }
//
//    override suspend fun fetchDataLoggers(): PagedDataLoggerListResponse {
//        return network.fetchDataLoggers()
//    }

    override suspend fun fetchErrorMessages() {
        network.fetchErrorMessages()
    }

//    override suspend fun fetchSchedulerFlag(deviceSN: String): SchedulerFlagResponse {
//        return network.fetchSchedulerFlag(deviceSN)
//    }
//
//    override suspend fun fetchScheduleModes(deviceID: String): List<SchedulerModeResponse> {
//        return network.fetchScheduleModes(deviceID)
//    }
//
//    override suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleListResponse {
//        return network.fetchCurrentSchedule(deviceSN)
//    }
//
//    override suspend fun deleteSchedule(deviceSN: String) {
//        return network.deleteSchedule(deviceSN)
//    }
//
//    override suspend fun saveSchedule(deviceSN: String, schedule: Schedule) {
//        return network.saveSchedule(deviceSN, schedule)
//    }
//
//    override suspend fun enableScheduleTemplate(deviceSN: String, templateID: String) {
//        network.enableScheduleTemplate(deviceSN, templateID)
//    }
//
//    override suspend fun fetchScheduleTemplate(deviceSN: String, templateID: String): ScheduleTemplateResponse {
//        return network.fetchScheduleTemplate(deviceSN, templateID)
//    }
//
//    override suspend fun deleteScheduleTemplate(templateID: String) {
//        network.deleteScheduleTemplate(templateID)
//    }
//
//    override suspend fun createScheduleTemplate(name: String, description: String) {
//        network.createScheduleTemplate(name, description)
//    }
//
//    override suspend fun fetchScheduleTemplates(): ScheduleTemplateListResponse {
//        return network.fetchScheduleTemplates()
//    }
//
//    override suspend fun saveScheduleTemplate(deviceSN: String, scheduleTemplate: ScheduleTemplate) {
//        network.saveScheduleTemplate(deviceSN, scheduleTemplate)
//    }

    private fun Double.capped(dataCeiling: DataCeiling): Double {
        return if (this > 0) {
            val mask = when (dataCeiling) {
                DataCeiling.None -> 0x0
                DataCeiling.Mild -> 0xFFF00000
                DataCeiling.Enhanced -> 0xFFFF0000
            }
            val register = (this * 10).toLong()
            val masked = register and mask

            if (masked == 0L) {
                this
            } else {
                (this - (masked.toDouble() / 10.0)).rounded(3)
            }
        } else {
            this
        }
    }
}