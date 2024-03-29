package com.alpriest.energystats.services

import com.alpriest.energystats.models.BatterySOCResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DataLoggerResponse
import com.alpriest.energystats.models.DeviceDetailResponse
import com.alpriest.energystats.models.DeviceSummaryResponse
import com.alpriest.energystats.models.GetSchedulerFlagResponse
import com.alpriest.energystats.models.OpenApiVariable
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.OpenHistoryResponseData
import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.models.OpenQueryResponseData
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.OpenReportResponseData
import com.alpriest.energystats.models.PagedPowerStationListResponse
import com.alpriest.energystats.models.PowerStationDetailResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ScheduleResponse
import com.alpriest.energystats.models.UnitData
import com.alpriest.energystats.models.rounded
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.statsgraph.ReportType
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

class NetworkValueCleaner(private val api: FoxAPIServicing, private val themeStream: MutableStateFlow<AppTheme>) : FoxAPIServicing {
    override suspend fun openapi_fetchDeviceList(): List<DeviceSummaryResponse> {
        return api.openapi_fetchDeviceList()
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenQueryResponse {
        val original = api.openapi_fetchRealData(deviceSN, variables)
        return OpenQueryResponse(
            time = original.time,
            deviceSN = original.deviceSN,
            datas = original.datas.map {
                OpenQueryResponseData(
                    it.unit,
                    it.variable,
                    it.value.capped(themeStream.value.dataCeiling)
                )
            }
        )
    }

    override suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        val original = api.openapi_fetchHistory(deviceSN, variables, start, end)
        return OpenHistoryResponse(
            original.deviceSN,
            original.datas.map {
                OpenHistoryResponseData(
                    name = it.name,
                    unit = it.unit,
                    variable = it.variable,
                    data = it.data.map { data ->
                        UnitData(data.time, data.value.capped(themeStream.value.dataCeiling))
                    }
                )
            }
        )
    }

    override suspend fun openapi_fetchVariables(): List<OpenApiVariable> {
        return api.openapi_fetchVariables()
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        val original = api.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
        return original.map {
            OpenReportResponse(
                variable = it.variable,
                unit = it.unit,
                values = it.values.map { value ->
                    OpenReportResponseData(value.index, value.value.capped(themeStream.value.dataCeiling))
                }
            )
        }
    }

    override suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse {
        return api.openapi_fetchBatterySettings(deviceSN)
    }

    override suspend fun openapi_setBatterySoc(deviceSN: String, minSOCOnGrid: Int, minSOC: Int) {
        return api.openapi_setBatterySoc(deviceSN, minSOCOnGrid, minSOC)
    }

    override suspend fun openapi_fetchDataLoggers(): List<DataLoggerResponse> {
        return api.openapi_fetchDataLoggers()
    }

    override suspend fun openapi_fetchBatteryTimes(deviceSN: String): List<ChargeTime> {
        return api.openapi_fetchBatteryTimes(deviceSN)
    }

    override suspend fun openapi_setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        api.openapi_setBatteryTimes(deviceSN, times)
    }

    override suspend fun openapi_fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse {
        return api.openapi_fetchSchedulerFlag(deviceSN)
    }

    override suspend fun openapi_fetchCurrentSchedule(deviceSN: String): ScheduleResponse {
        return api.openapi_fetchCurrentSchedule(deviceSN)
    }

    override suspend fun openapi_setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean) {
        api.openapi_setScheduleFlag(deviceSN, schedulerEnabled)
    }

    override suspend fun openapi_saveSchedule(deviceSN: String, schedule: Schedule) {
        api.openapi_saveSchedule(deviceSN, schedule)
    }

    override suspend fun openapi_fetchDevice(deviceSN: String): DeviceDetailResponse {
        return api.openapi_fetchDevice(deviceSN)
    }

    override suspend fun openapi_fetchPowerStationList(): PagedPowerStationListResponse {
        return api.openapi_fetchPowerStationList()
    }

    override suspend fun openapi_fetchPowerStationDetail(stationID: String): PowerStationDetailResponse {
        return api.openapi_fetchPowerStationDetail(stationID)
    }

    override suspend fun fetchErrorMessages() {
        api.fetchErrorMessages()
    }

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