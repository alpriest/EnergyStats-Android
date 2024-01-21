package com.alpriest.energystats.services

import com.alpriest.energystats.models.AddressBookResponse
import com.alpriest.energystats.models.BatteryResponse
import com.alpriest.energystats.models.BatterySOCResponse
import com.alpriest.energystats.models.BatterySettingsResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DeviceDetailResponse
import com.alpriest.energystats.models.DeviceFunction
import com.alpriest.energystats.models.OpenApiVariable
import com.alpriest.energystats.models.OpenApiVariableArray
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.models.OpenQueryResponseData
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.PagedDataLoggerListResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ScheduleListResponse
import com.alpriest.energystats.models.SchedulePollcy
import com.alpriest.energystats.models.ScheduleTemplateListResponse
import com.alpriest.energystats.models.ScheduleTemplateResponse
import com.alpriest.energystats.models.ScheduleTemplateSummaryResponse
import com.alpriest.energystats.models.SchedulerFlagResponse
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.models.SoftwareVersion
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.statsgraph.ReportType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime


class DemoFoxESSNetworking : FoxESSNetworking {
//    override suspend fun createScheduleTemplate(name: String, description: String) {
//        // Do nothing
//    }
//
//    override suspend fun enableScheduleTemplate(deviceSN: String, templateID: String) {
//        // Do nothing
//    }
//
//    override suspend fun deleteSchedule(deviceSN: String) {
//        // Do nothing
//    }
//
//    override suspend fun saveSchedule(deviceSN: String, schedule: Schedule) {
//        // Do nothing
//    }
//
//    override suspend fun saveScheduleTemplate(deviceSN: String, scheduleTemplate: ScheduleTemplate) {
//        // Do nothing
//    }

//    override suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleListResponse {
//        return ScheduleListResponse(
//            data = listOf(
//                ScheduleTemplateSummaryResponse(templateName = "Winter charging", enable = false, templateID = "123"),
//                ScheduleTemplateSummaryResponse(templateName = "", enable = true, templateID = "")
//            ),
//            enable = true,
//            pollcy = listOf(
//                SchedulePollcy(startH = 15, startM = 0, endH = 17, endM = 0, fdpwr = 0, workMode = "ForceCharge", fdsoc = 100, minsocongrid = 100),
//                SchedulePollcy(startH = 17, startM = 0, endH = 18, endM = 30, fdpwr = 3500, workMode = "ForceDischarge", fdsoc = 20, minsocongrid = 20)
//            )
//        )
//    }
//
//    override suspend fun fetchBattery(deviceID: String): BatteryResponse {
//        return when (deviceID) {
//            "f3000-deviceid" -> BatteryResponse(power = 0.28, soc = 76, residual = 7550.0, temperature = 17.3)
//            else -> BatteryResponse(power = 0.78, soc = 46, residual = 17510.0, temperature = 19.3)
//        }
//    }
//
//    override suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse {
//        return BatterySettingsResponse(20, minSoc = 30)
//    }

    override suspend fun openapi_fetchDeviceList(): List<DeviceDetailResponse> {
        return arrayListOf(
            DeviceDetailResponse(
                deviceSN = "998877",
                moduleSN = "mod111",
                stationID = "st123",
                stationName = "h1_deviceid",
                function = DeviceFunction(scheduler = true),
                hardwareVersion = "1",
                managerVersion = "2",
                masterVersion = "3",
                slaveVersion = "4",
                status = 1,
                deviceType = "h1",
                hasBattery = true,
                hasPV = true,
                productType = "produ1"
            ),
            DeviceDetailResponse(
                deviceSN = "123123",
                moduleSN = "mod222",
                stationID = "st999",
                stationName = "f3000_deviceid",
                function = DeviceFunction(scheduler = true),
                hardwareVersion = "1",
                managerVersion = "2",
                masterVersion = "3",
                slaveVersion = "4",
                status = 1,
                deviceType = "f3000",
                hasBattery = true,
                hasPV = true,
                productType = "produ2"
            )
        )
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenQueryResponse {
        return OpenQueryResponse(time = "now",
            deviceSN = deviceSN,
            datas = listOf(
                OpenQueryResponseData(unit = "kW", variable = "feedinPower", value = 0.0),
                OpenQueryResponseData(unit = "kW", variable = "gridConsumptionPower", value = 2.634),
                OpenQueryResponseData(unit = "kW", variable = "loadsPower", value = 2.708),
                OpenQueryResponseData(unit = "kW", variable = "generationPower", value = 0.071),
                OpenQueryResponseData(unit = "kW", variable = "pvPower", value = 0.111),
                OpenQueryResponseData(unit = "kW", variable = "meterPower2", value = 0.0),
                OpenQueryResponseData(unit = "℃", variable = "ambientTemperation", value = 32.5),
                OpenQueryResponseData(unit = "℃", variable = "invTemperation", value = 23.2)
            )
        )
    }

    override suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        return OpenHistoryResponse(
            deviceSN = "",
            datas = listOf()
        )
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        return listOf()
    }

    override suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse {
        TODO("Not yet implemented")
    }

    override suspend fun openapi_fetchVariables(): List<OpenApiVariable> {
        val fileContent = this::class.java.classLoader?.getResource("res/raw/variables.json")?.readText()

        val data: NetworkResponse<OpenApiVariableArray> = Gson().fromJson(fileContent, object : TypeToken<NetworkResponse<OpenApiVariableArray>>() {}.type)

        return data.result?.array ?: listOf()
    }

//    override suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String) {
//    }
//
//    override suspend fun fetchBatteryTimes(deviceSN: String): BatteryTimesResponse {
//        return BatteryTimesResponse(
//            sn = deviceSN,
//            times = listOf(
//                ChargeTime(enableGrid = true, startTime = Time(hour = 0, minute = 0), endTime = Time(hour = 0, minute = 0)),
//                ChargeTime(enableGrid = true, startTime = Time(hour = 0, minute = 0), endTime = Time(hour = 0, minute = 0)),
//            )
//        )
//    }
//
//    override suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
//    }
//
//    override suspend fun fetchDataLoggers(): PagedDataLoggerListResponse {
//        return PagedDataLoggerListResponse(
//            1, 10, 1, listOf(
//                PagedDataLoggerListResponse.DataLogger(moduleSN = "ABC123DEF456", moduleType = "W2", plantName = "John Doe", version = "3.08", signal = 3, communication = 1),
//                PagedDataLoggerListResponse.DataLogger(moduleSN = "123DEF456ABC", moduleType = "W2", plantName = "Jane Doe", version = "3.08", signal = 1, communication = 0)
//            )
//        )
//    }

    override suspend fun fetchErrorMessages() {}

//    override suspend fun fetchSchedulerFlag(deviceSN: String): SchedulerFlagResponse {
//        return SchedulerFlagResponse(enable = true, support = true)
//    }

//    override suspend fun fetchScheduleModes(deviceID: String): List<SchedulerModeResponse> {
//        return listOf(
//            SchedulerModeResponse(color = "#80F6BD16", name = "Back Up", key = "Backup"),
//            SchedulerModeResponse(color = "#805B8FF9", name = "Feed-in Priority", key = "Feedin"),
//            SchedulerModeResponse(color = "#80BBE9FB", name = "Force Charge", key = "ForceCharge"),
//            SchedulerModeResponse(color = "#8065789B", name = "Force Discharge", key = "ForceDischarge"),
//            SchedulerModeResponse(color = "#8061DDAA", name = "Self-Use", key = "SelfUse")
//        )
//    }
//
//    override suspend fun fetchScheduleTemplate(deviceSN: String, templateID: String): ScheduleTemplateResponse {
//        return ScheduleTemplateResponse(
//            templateName = "Template-1",
//            enable = false,
//            pollcy = listOf(
//                SchedulePollcy(startH = 15, startM = 0, endH = 17, endM = 0, fdpwr = 0, workMode = "ForceCharge", fdsoc = 100, minsocongrid = 100),
//                SchedulePollcy(startH = 17, startM = 0, endH = 18, endM = 30, fdpwr = 3500, workMode = "ForceDischarge", fdsoc = 20, minsocongrid = 20)
//            ),
//            content = "Description of template 1"
//        )
//    }

//    override suspend fun deleteScheduleTemplate(templateID: String) {
//    }
//
//    override suspend fun fetchScheduleTemplates(): ScheduleTemplateListResponse {
//        return ScheduleTemplateListResponse(
//            data = listOf(
//                ScheduleTemplateSummaryResponse(templateName = "Winter charging", enable = false, templateID = "1"),
//                ScheduleTemplateSummaryResponse(templateName = "Summer charging", enable = false, templateID = "2"),
//                ScheduleTemplateSummaryResponse(templateName = "Saving session", enable = false, templateID = "3")
//            )
//        )
//    }
}
