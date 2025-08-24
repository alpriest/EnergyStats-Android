package com.alpriest.energystats.services

import com.alpriest.energystats.R
import com.alpriest.energystats.models.ApiRequestCountResponse
import com.alpriest.energystats.models.BatterySOCResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DataLoggerResponse
import com.alpriest.energystats.models.DataLoggerStatus
import com.alpriest.energystats.models.DataLoggerStatusDeserializer
import com.alpriest.energystats.models.DeviceDetailResponse
import com.alpriest.energystats.models.DeviceFunction
import com.alpriest.energystats.models.DeviceSettingsItem
import com.alpriest.energystats.models.DeviceSummaryResponse
import com.alpriest.energystats.models.FetchDeviceSettingsItemResponse
import com.alpriest.energystats.models.FetchPeakShavingSettingsResponse
import com.alpriest.energystats.models.GetSchedulerFlagResponse
import com.alpriest.energystats.models.OpenApiVariable
import com.alpriest.energystats.models.OpenApiVariableArray
import com.alpriest.energystats.models.OpenApiVariableDeserializer
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.OpenQueryResponseData
import com.alpriest.energystats.models.OpenRealQueryResponse
import com.alpriest.energystats.models.OpenRealQueryResponseDeserializer
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.OpenReportResponseDeserializer
import com.alpriest.energystats.models.PagedPowerStationListResponse
import com.alpriest.energystats.models.PowerGenerationResponse
import com.alpriest.energystats.models.PowerStationDetailResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.SchedulePhaseNetworkModel
import com.alpriest.energystats.models.ScheduleResponse
import com.alpriest.energystats.models.SettingItem
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.parseToLocalDateTime
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.WorkMode
import com.alpriest.energystats.ui.statsgraph.ReportType
import com.alpriest.energystats.ui.summary.PreviewContextHolder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DemoNetworking : NetworkService(DemoAPI())

class DemoAPI : FoxAPIServicing {
    override suspend fun openapi_fetchDeviceList(): List<DeviceSummaryResponse> {
        return arrayListOf(
            DeviceSummaryResponse(
                deviceSN = "998877",
                moduleSN = "mod111",
                stationID = "st123",
                stationName = "Bloggs Home",
                status = 1,
                deviceType = "h1",
                hasBattery = true,
                hasPV = true,
                productType = "produ1"
            ),
            DeviceSummaryResponse(
                deviceSN = "123123",
                moduleSN = "mod222",
                stationID = "st999",
                stationName = "Bloggs Shed",
                status = 1,
                deviceType = "f3000",
                hasBattery = true,
                hasPV = true,
                productType = "produ2"
            )
        )
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenRealQueryResponse {
        return OpenRealQueryResponse(
            time = LocalDateTime.now().toString().replace("T", " "),
            deviceSN = deviceSN,
            datas = listOf(
                OpenQueryResponseData(unit = "kW", variable = "feedinPower", value = 0.0, valueString = null),
                OpenQueryResponseData(unit = "kW", variable = "gridConsumptionPower", value = 2.634, valueString = null),
                OpenQueryResponseData(unit = "kW", variable = "loadsPower", value = 2.708, valueString = null),
                OpenQueryResponseData(unit = "kW", variable = "generationPower", value = 0.071, valueString = null),
                OpenQueryResponseData(unit = "kW", variable = "pvPower", value = 0.222, valueString = null),
                OpenQueryResponseData(unit = "kW", variable = "pv1Power", value = 0.111, valueString = null),
                OpenQueryResponseData(unit = "kW", variable = "pv2Power", value = 0.111, valueString = null),
                OpenQueryResponseData(unit = "kW", variable = "meterPower2", value = 0.222, valueString = null),
                OpenQueryResponseData(unit = "℃", variable = "ambientTemperation", value = 32.5, valueString = null),
                OpenQueryResponseData(unit = "℃", variable = "invTemperation", value = 23.2, valueString = null)
            )
        )
    }

    override suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        val fileContent = this::class.java.classLoader?.getResource("res/raw/history.json")?.readText()
        val formatter = DateTimeFormatter.ofPattern(dateFormat)
        val now = LocalDate.now()

        val data: NetworkResponse<List<OpenHistoryResponse>> = makeGson().fromJson(fileContent, object : TypeToken<NetworkResponse<List<OpenHistoryResponse>>>() {}.type)

        return data.result?.map { response ->
            response.copy(datas = response.datas.map { datas ->
                datas.copy(data = datas.data.map {
                    val simpleDate = parseToLocalDateTime(it.time)
                    val localDateTime = simpleDate
                        .withYear(now.year)
                        .withMonth(now.monthValue)
                        .withDayOfMonth(now.dayOfMonth)

                    it.copy(time = localDateTime.format(formatter))
                })
            })
        }?.firstOrNull() ?: throw InvalidTokenException()
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        val fileContent: String?
        val context = PreviewContextHolder.context

        fileContent = if (context != null) {
            when (reportType) {
                ReportType.day -> context.resources.openRawResource(R.raw.report_day).bufferedReader().use { it.readText() }
                ReportType.month -> context.resources.openRawResource(R.raw.report_month).bufferedReader().use { it.readText() }
                ReportType.year -> context.resources.openRawResource(R.raw.report_year).bufferedReader().use { it.readText() }
            }
        } else {
            when (reportType) {
                ReportType.day -> this::class.java.classLoader?.getResource("res/raw/report_day.json")?.readText()
                ReportType.month -> this::class.java.classLoader?.getResource("res/raw/report_month.json")?.readText()
                ReportType.year -> this::class.java.classLoader?.getResource("res/raw/report_month.json")?.readText()
            }
        }

        val data: NetworkResponse<List<OpenReportResponse>> = makeGson().fromJson(fileContent, object : TypeToken<NetworkResponse<List<OpenReportResponse>>>() {}.type)

        return data.result ?: throw InvalidTokenException()
    }

    override suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse {
        return BatterySOCResponse(minSoc = 20, minSocOnGrid = 20)
    }

    override suspend fun openapi_setBatterySoc(deviceSN: String, minSOCOnGrid: Int, minSOC: Int) {

    }

    override suspend fun openapi_fetchVariables(): List<OpenApiVariable> {
        val fileContent = this::class.java.classLoader?.getResource("res/raw/variables.json")?.readText()

        val data: NetworkResponse<OpenApiVariableArray> = makeGson().fromJson(fileContent, object : TypeToken<NetworkResponse<OpenApiVariableArray>>() {}.type)

        return data.result?.array ?: listOf()
    }

    override suspend fun openapi_fetchDataLoggers(): List<DataLoggerResponse> {
        return listOf(
            DataLoggerResponse(moduleSN = "ABC123DEF456", stationID = "W21", signal = 3, status = DataLoggerStatus.ONLINE),
            DataLoggerResponse(moduleSN = "123DEF456ABC", stationID = "W22", signal = 1, status = DataLoggerStatus.OFFLINE)
        )
    }

    override suspend fun openapi_fetchBatteryTimes(deviceSN: String): List<ChargeTime> {
        return listOf(
            ChargeTime(enable = true, startTime = Time(hour = 0, minute = 0), endTime = Time(hour = 0, minute = 0)),
            ChargeTime(enable = true, startTime = Time(hour = 0, minute = 0), endTime = Time(hour = 0, minute = 0))
        )
    }

    override suspend fun openapi_setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
    }

    override suspend fun openapi_fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse {
        return GetSchedulerFlagResponse(true, true)
    }

    override suspend fun openapi_fetchCurrentSchedule(deviceSN: String): ScheduleResponse {
        return ScheduleResponse(
            1,
            listOf(
                SchedulePhaseNetworkModel(
                    enable = 1,
                    startHour = 15,
                    startMinute = 0,
                    endHour = 17,
                    endMinute = 0,
                    workMode = WorkMode.ForceCharge,
                    minSocOnGrid = 20,
                    fdSoc = 100,
                    fdPwr = 0,
                    maxSoc = 100
                ),
                SchedulePhaseNetworkModel(
                    enable = 1,
                    startHour = 7,
                    startMinute = 0,
                    endHour = 18,
                    endMinute = 30,
                    workMode = WorkMode.ForceDischarge,
                    minSocOnGrid = 20,
                    fdSoc = 20,
                    fdPwr = 3500,
                    maxSoc = 100
                )
            ),
            workModes = listOf(
                WorkMode.SelfUse,
                WorkMode.Feedin,
                WorkMode.Backup,
                WorkMode.ForceCharge,
                WorkMode.ForceDischarge
            )
        )
    }

    override suspend fun openapi_setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean) {}
    override suspend fun openapi_saveSchedule(deviceSN: String, schedule: Schedule) {}
    override suspend fun openapi_fetchDevice(deviceSN: String): DeviceDetailResponse {
        return DeviceDetailResponse(
            deviceSN = "998877",
            moduleSN = "mod111",
            stationID = "st123",
            status = 1,
            deviceType = "h1",
            hasBattery = true,
            hasPV = true,
            productType = "produ1",
            masterVersion = "master1",
            managerVersion = "manager1",
            slaveVersion = "slave1",
            hardwareVersion = "hardware1",
            function = DeviceFunction(scheduler = true),
            stationName = "my station",
            batteryList = null
        )
    }

    override suspend fun openapi_fetchPowerStationList(): PagedPowerStationListResponse {
        return PagedPowerStationListResponse(1, 1, 0, listOf())
    }

    override suspend fun openapi_fetchPowerStationDetail(stationID: String): PowerStationDetailResponse {
        return PowerStationDetailResponse("", 0.0, "")
    }

    override suspend fun openapi_fetchRequestCount(): ApiRequestCountResponse {
        return ApiRequestCountResponse("10", "5")
    }

    override suspend fun openapi_fetchDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem): FetchDeviceSettingsItemResponse {
        return FetchDeviceSettingsItemResponse(
            value = "1.0",
            unit = "kWh",
            precision = 1.0,
            range = FetchDeviceSettingsItemResponse.Range(min = 0.0, max = 100.0)
        )
    }

    override suspend fun openapi_setDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem, value: String) {
    }

    override suspend fun openapi_fetchPeakShavingSettings(deviceSN: String): FetchPeakShavingSettingsResponse {
        return FetchPeakShavingSettingsResponse(
            importLimit = SettingItem(
                precision = 0.001,
                range = SettingItem.Range(min = 0.0, max = 10000.0),
                unit = "kW",
                value = "1000.000"
            ),
            soc = SettingItem(
                precision = 1.0,
                range = SettingItem.Range(min = 10.0, max = 100.0),
                unit = "%",
                value = "40"
            )
        )
    }

    override suspend fun openapi_setPeakShavingSettings(deviceSN: String, importLimit: Double, soc: Int) {
        // do nothing
    }

    override suspend fun openapi_fetchPowerGeneration(deviceSN: String): PowerGenerationResponse {
        return PowerGenerationResponse(
            today = 2.0,
            month = 12.0,
            cumulative = 244.0
        )
    }

    override suspend fun fetchErrorMessages() {}

    private fun makeGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(OpenApiVariableArray::class.java, OpenApiVariableDeserializer())
            .registerTypeAdapter(OpenReportResponse::class.java, OpenReportResponseDeserializer())
            .registerTypeAdapter(DataLoggerStatus::class.java, DataLoggerStatusDeserializer())
            .registerTypeAdapter(OpenRealQueryResponseDeserializer::class.java, OpenRealQueryResponseDeserializer())
            .create()
    }
}
