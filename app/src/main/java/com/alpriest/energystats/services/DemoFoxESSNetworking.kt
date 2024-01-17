package com.alpriest.energystats.services

import com.alpriest.energystats.models.AddressBookResponse
import com.alpriest.energystats.models.BatteryResponse
import com.alpriest.energystats.models.BatterySettingsResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DeviceDetailResponse
import com.alpriest.energystats.models.DeviceFunction
import com.alpriest.energystats.models.DeviceSettingsGetResponse
import com.alpriest.energystats.models.DeviceSettingsValues
import com.alpriest.energystats.models.Earning
import com.alpriest.energystats.models.EarningsResponse
import com.alpriest.energystats.models.PagedDataLoggerListResponse
import com.alpriest.energystats.models.PagedDeviceListResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.models.ReportResponse
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
import com.alpriest.energystats.models.VariablesResponse
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.statsgraph.ReportType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


class DemoFoxESSNetworking : FoxESSNetworking {
    override suspend fun createScheduleTemplate(name: String, description: String) {
        // Do nothing
    }

    override suspend fun enableScheduleTemplate(deviceSN: String, templateID: String) {
        // Do nothing
    }

    override suspend fun deleteSchedule(deviceSN: String) {
        // Do nothing
    }

    override suspend fun saveSchedule(deviceSN: String, schedule: Schedule) {
        // Do nothing
    }

    override suspend fun saveScheduleTemplate(deviceSN: String, scheduleTemplate: ScheduleTemplate) {
        // Do nothing
    }

    override suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleListResponse {
        return ScheduleListResponse(
            data = listOf(
                ScheduleTemplateSummaryResponse(templateName = "Winter charging", enable = false, templateID = "123"),
                ScheduleTemplateSummaryResponse(templateName = "", enable = true, templateID = "")
            ),
            enable = true,
            pollcy = listOf(
                SchedulePollcy(startH = 15, startM = 0, endH = 17, endM = 0, fdpwr = 0, workMode = "ForceCharge", fdsoc = 100, minsocongrid = 100),
                SchedulePollcy(startH = 17, startM = 0, endH = 18, endM = 30, fdpwr = 3500, workMode = "ForceDischarge", fdsoc = 20, minsocongrid = 20)
            )
        )
    }

    override suspend fun fetchBattery(deviceID: String): BatteryResponse {
        return when (deviceID) {
            "f3000-deviceid" -> BatteryResponse(power = 0.28, soc = 76, residual = 7550.0, temperature = 17.3)
            else -> BatteryResponse(power = 0.78, soc = 46, residual = 17510.0, temperature = 19.3)
        }
    }

    override suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse {
        return BatterySettingsResponse(20, minSoc = 30)
    }

    override suspend fun fetchReport(
        deviceID: String,
        variables: List<ReportVariable>,
        queryDate: QueryDate,
        reportType: ReportType
    ): ArrayList<ReportResponse> {
        val filename = when (reportType) {
            ReportType.day -> "res/raw/report_day.json"
            ReportType.month -> "res/raw/report_month.json"
            ReportType.year -> "res/raw/report_year.json"
        }
        val fileContent = this::class.java.classLoader?.getResource(filename)?.readText()

        val data: NetworkReportResponse = Gson().fromJson(fileContent, object : TypeToken<NetworkReportResponse>() {}.type)

        return data.result ?: ArrayList()
    }

    override suspend fun fetchAddressBook(deviceID: String): AddressBookResponse {
        if (deviceID == "f3000_deviceid") {
            return AddressBookResponse(softVersion = SoftwareVersion(master = "1.54", slave = "1.09", manager = "1.49"))
        } else {
            return AddressBookResponse(softVersion = SoftwareVersion(master = "2.54", slave = "1.09", manager = "1.56"))
        }
    }

    override suspend fun fetchRaw(deviceID: String, variables: List<RawVariable>, queryDate: QueryDate): ArrayList<RawResponse> {
        val result = rawData(deviceID)
        val formatter = DateTimeFormatter.ofPattern(dateFormat)
        val now = LocalDate.now()

        return ArrayList(result.map { response ->
            RawResponse(
                variable = response.variable,
                data = response.data.map {
                    val simpleDate = SimpleDateFormat(dateFormat, Locale.getDefault()).parse(it.time)
                    val localDateTime = simpleDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        .withYear(now.year)
                        .withMonth(now.monthValue)
                        .withDayOfMonth(now.dayOfMonth)

                    RawData(time = localDateTime.format(formatter), value = it.value)
                }.toTypedArray()
            )
        }.toList())
    }

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
                status = 1
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
                status = 1
            )
        )
    }

    override suspend fun fetchVariables(deviceID: String): List<RawVariable> {
        val fileContent = this::class.java.classLoader?.getResource("res/raw/variables.json")?.readText()

        val data: NetworkResponse<VariablesResponse> = Gson().fromJson(fileContent, object : TypeToken<NetworkResponse<VariablesResponse>>() {}.type)

        return data.result?.variables ?: listOf()
    }

    private fun rawData(deviceID: String): List<RawResponse> {
        val fileContent = this::class.java.classLoader?.getResource("res/raw/raw_$deviceID.json")?.readText()

        val data: NetworkRawResponse = Gson().fromJson(fileContent, object : TypeToken<NetworkRawResponse>() {}.type)

        return data.result?.toList() ?: listOf()
    }

    override suspend fun fetchEarnings(deviceID: String): EarningsResponse {
        return EarningsResponse(
            today = Earning(
                generation = 11.5,
                earnings = 54.2
            ),
            currency = "GBP (£)",
            month = Earning(
                generation = 31.5,
                earnings = 154.2
            ),
            year = Earning(
                generation = 81.5,
                earnings = 254.2
            ),
            cumulate = Earning(
                generation = 121.5,
                earnings = 354.2
            ),
        )
    }

    override suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String) {
    }

    override suspend fun fetchBatteryTimes(deviceSN: String): BatteryTimesResponse {
        return BatteryTimesResponse(
            sn = deviceSN,
            times = listOf(
                ChargeTime(enableGrid = true, startTime = Time(hour = 0, minute = 0), endTime = Time(hour = 0, minute = 0)),
                ChargeTime(enableGrid = true, startTime = Time(hour = 0, minute = 0), endTime = Time(hour = 0, minute = 0)),
            )
        )
    }

    override suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
    }

    override suspend fun fetchWorkMode(deviceID: String): DeviceSettingsGetResponse {
        return DeviceSettingsGetResponse(protocol = "H11300", values = DeviceSettingsValues("SelfUse"))
    }

    override suspend fun setWorkMode(deviceID: String, workMode: String) {
    }

    override suspend fun fetchDataLoggers(): PagedDataLoggerListResponse {
        return PagedDataLoggerListResponse(
            1, 10, 1, listOf(
                PagedDataLoggerListResponse.DataLogger(moduleSN = "ABC123DEF456", moduleType = "W2", plantName = "John Doe", version = "3.08", signal = 3, communication = 1),
                PagedDataLoggerListResponse.DataLogger(moduleSN = "123DEF456ABC", moduleType = "W2", plantName = "Jane Doe", version = "3.08", signal = 1, communication = 0)
            )
        )
    }

    override suspend fun fetchErrorMessages() {}

    override suspend fun fetchSchedulerFlag(deviceSN: String): SchedulerFlagResponse {
        return SchedulerFlagResponse(enable = true, support = true)
    }

    override suspend fun fetchScheduleModes(deviceID: String): List<SchedulerModeResponse> {
        return listOf(
            SchedulerModeResponse(color = "#80F6BD16", name = "Back Up", key = "Backup"),
            SchedulerModeResponse(color = "#805B8FF9", name = "Feed-in Priority", key = "Feedin"),
            SchedulerModeResponse(color = "#80BBE9FB", name = "Force Charge", key = "ForceCharge"),
            SchedulerModeResponse(color = "#8065789B", name = "Force Discharge", key = "ForceDischarge"),
            SchedulerModeResponse(color = "#8061DDAA", name = "Self-Use", key = "SelfUse")
        )
    }

    override suspend fun fetchScheduleTemplate(deviceSN: String, templateID: String): ScheduleTemplateResponse {
        return ScheduleTemplateResponse(
            templateName = "Template-1",
            enable = false,
            pollcy = listOf(
                SchedulePollcy(startH = 15, startM = 0, endH = 17, endM = 0, fdpwr = 0, workMode = "ForceCharge", fdsoc = 100, minsocongrid = 100),
                SchedulePollcy(startH = 17, startM = 0, endH = 18, endM = 30, fdpwr = 3500, workMode = "ForceDischarge", fdsoc = 20, minsocongrid = 20)
            ),
            content = "Description of template 1"
        )
    }

    override suspend fun deleteScheduleTemplate(templateID: String) {
    }

    override suspend fun fetchScheduleTemplates(): ScheduleTemplateListResponse {
        return ScheduleTemplateListResponse(
            data = listOf(
                ScheduleTemplateSummaryResponse(templateName = "Winter charging", enable = false, templateID = "1"),
                ScheduleTemplateSummaryResponse(templateName = "Summer charging", enable = false, templateID = "2"),
                ScheduleTemplateSummaryResponse(templateName = "Saving session", enable = false, templateID = "3")
            )
        )
    }
}
