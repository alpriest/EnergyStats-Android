package com.alpriest.energystats.shared.network

import com.alpriest.energystats.shared.helpers.truncated
import com.alpriest.energystats.shared.models.DataCeiling
import com.alpriest.energystats.shared.models.QueryDate
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.Schedule
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
import com.alpriest.energystats.shared.models.network.OpenHistoryResponseData
import com.alpriest.energystats.shared.models.network.OpenQueryResponseData
import com.alpriest.energystats.shared.models.network.OpenRealQueryResponse
import com.alpriest.energystats.shared.models.network.OpenReportResponse
import com.alpriest.energystats.shared.models.network.OpenReportResponseData
import com.alpriest.energystats.shared.models.network.PagedPowerStationListResponse
import com.alpriest.energystats.shared.models.network.PowerGenerationResponse
import com.alpriest.energystats.shared.models.network.PowerStationDetailResponse
import com.alpriest.energystats.shared.models.network.ReportType
import com.alpriest.energystats.shared.models.network.ScheduleResponse
import com.alpriest.energystats.shared.models.network.UnitData

class NetworkValueCleaner(private val api: FoxAPIServicing, private val dataCeilingProvider: () -> DataCeiling) : FoxAPIServicing {
    override suspend fun openapi_fetchDeviceList(): List<DeviceSummaryResponse> {
        return api.openapi_fetchDeviceList()
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenRealQueryResponse {
        val original = api.openapi_fetchRealData(deviceSN, variables)
        return OpenRealQueryResponse(
            time = original.time,
            deviceSN = original.deviceSN,
            datas = original.datas.map {
                OpenQueryResponseData(
                    it.unit,
                    it.variable,
                    it.value?.capped(dataCeilingProvider()),
                    it.valueString
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
                        UnitData(data.time, data.value.capped(dataCeilingProvider()))
                    }
                )
            }
        )
    }

    override suspend fun openapi_fetchVariables(): List<ApiVariable> {
        return api.openapi_fetchVariables()
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        val original = api.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
        return original.mapIndexed { index, original ->
            OpenReportResponse(
                variable = variables[index].networkTitle(),
                unit = original.unit,
                values = original.values.map { value ->
                    OpenReportResponseData(value.index, value.value.capped(dataCeilingProvider()))
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

    override suspend fun openapi_fetchRequestCount(): ApiRequestCountResponse {
        return api.openapi_fetchRequestCount()
    }

    override suspend fun openapi_fetchDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem): FetchDeviceSettingsItemResponse {
        return api.openapi_fetchDeviceSettingsItem(deviceSN, item)
    }

    override suspend fun openapi_setDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem, value: String) {
        return api.openapi_setDeviceSettingsItem(deviceSN, item, value)
    }

    override suspend fun openapi_fetchPeakShavingSettings(deviceSN: String): FetchPeakShavingSettingsResponse {
        return api.openapi_fetchPeakShavingSettings(deviceSN)
    }

    override suspend fun openapi_setPeakShavingSettings(deviceSN: String, importLimit: Double, soc: Int) {
        return api.openapi_setPeakShavingSettings(deviceSN, importLimit, soc)
    }

    override suspend fun fetchErrorMessages() {
        api.fetchErrorMessages()
    }

    override suspend fun openapi_fetchPowerGeneration(deviceSN: String): PowerGenerationResponse {
        return api.openapi_fetchPowerGeneration(deviceSN)
    }

    override suspend fun openapi_getBatteryHeatingSchedule(deviceSN: String): BatteryHeatingScheduleResponse {
        return api.openapi_getBatteryHeatingSchedule(deviceSN)
    }

    override suspend fun openapi_setBatteryHeatingSchedule(schedule: BatteryHeatingScheduleRequest) {
        api.openapi_setBatteryHeatingSchedule(schedule)
    }

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
                (this - (masked.toDouble() / 10.0)).truncated(3)
            }
        } else {
            this
        }
    }
}