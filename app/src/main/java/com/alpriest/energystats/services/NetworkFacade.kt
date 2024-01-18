package com.alpriest.energystats.services

import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.statsgraph.ReportType

class NetworkFacade(private val network: FoxESSNetworking, private val isDemoUser: () -> Boolean) : FoxESSNetworking {
    private val demoFoxESSNetworking = DemoFoxESSNetworking()

    override suspend fun openapi_fetchDeviceList(): List<DeviceDetailResponse> {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchDeviceList()
        } else {
            network.openapi_fetchDeviceList()
        }
    }

    override suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchHistory(deviceSN, variables, start, end)
        } else {
            return network.openapi_fetchHistory(deviceSN, variables, start, end)
        }
    }

    override suspend fun openapi_fetchVariables(): List<OpenApiVariable> {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchVariables()
        } else {
            return network.openapi_fetchVariables()
        }
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
        } else {
            return network.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
        }    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<Variable>): OpenQueryResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchRealData(deviceSN, variables)
        } else {
            network.openapi_fetchRealData(deviceSN, variables)
        }
    }

    override suspend fun fetchBattery(deviceID: String): BatteryResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchBattery(deviceID)
        } else {
            network.fetchBattery(deviceID)
        }
    }

    override suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchBatterySettings(deviceSN)
        } else {
            network.fetchBatterySettings(deviceSN)
        }
    }

    override suspend fun fetchAddressBook(deviceID: String): AddressBookResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchAddressBook(deviceID)
        } else {
            network.fetchAddressBook(deviceID)
        }
    }

    override suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String) {
        return if (isDemoUser()) {
            demoFoxESSNetworking.setSoc(minGridSOC, minSOC, deviceSN)
        } else {
            network.setSoc(minGridSOC, minSOC, deviceSN)
        }
    }

    override suspend fun fetchBatteryTimes(deviceSN: String): BatteryTimesResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchBatteryTimes(deviceSN)
        } else {
            network.fetchBatteryTimes(deviceSN)
        }
    }

    override suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        return if (isDemoUser()) {
            demoFoxESSNetworking.setBatteryTimes(deviceSN, times)
        } else {
            network.setBatteryTimes(deviceSN, times)
        }
    }

    override suspend fun fetchDataLoggers(): PagedDataLoggerListResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchDataLoggers()
        } else {
            network.fetchDataLoggers()
        }
    }

    override suspend fun fetchErrorMessages() {
        if (isDemoUser()) {
            demoFoxESSNetworking.fetchErrorMessages()
        } else {
            network.fetchErrorMessages()
        }
    }

    override suspend fun fetchSchedulerFlag(deviceSN: String): SchedulerFlagResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchSchedulerFlag(deviceSN)
        } else {
            network.fetchSchedulerFlag(deviceSN)
        }
    }

    override suspend fun fetchScheduleModes(deviceID: String): List<SchedulerModeResponse> {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchScheduleModes(deviceID)
        } else {
            network.fetchScheduleModes(deviceID)
        }
    }

    override suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleListResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchCurrentSchedule(deviceSN)
        } else {
            network.fetchCurrentSchedule(deviceSN)
        }
    }

    override suspend fun saveSchedule(deviceSN: String, schedule: Schedule) {
        if (isDemoUser()) {
            demoFoxESSNetworking.saveSchedule(deviceSN, schedule)
        } else {
            network.saveSchedule(deviceSN, schedule)
        }
    }

    override suspend fun deleteSchedule(deviceSN: String) {
        if (isDemoUser()) {
            demoFoxESSNetworking.deleteSchedule(deviceSN)
        } else {
            network.deleteSchedule(deviceSN)
        }
    }

    override suspend fun enableScheduleTemplate(deviceSN: String, templateID: String) {
        if (isDemoUser()) {
            demoFoxESSNetworking.enableScheduleTemplate(deviceSN, templateID)
        } else {
            network.enableScheduleTemplate(deviceSN, templateID)
        }
    }

    override suspend fun deleteScheduleTemplate(templateID: String) {
        if (isDemoUser()) {
            demoFoxESSNetworking.deleteScheduleTemplate(templateID)
        } else {
            network.deleteScheduleTemplate(templateID)
        }
    }

    override suspend fun fetchScheduleTemplate(deviceSN: String, templateID: String): ScheduleTemplateResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchScheduleTemplate(deviceSN, templateID)
        } else {
            network.fetchScheduleTemplate(deviceSN, templateID)
        }
    }

    override suspend fun createScheduleTemplate(name: String, description: String) {
        if (isDemoUser()) {
            demoFoxESSNetworking.createScheduleTemplate(name, description)
        } else {
            network.createScheduleTemplate(name, description)
        }
    }

    override suspend fun fetchScheduleTemplates(): ScheduleTemplateListResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchScheduleTemplates()
        } else {
            network.fetchScheduleTemplates()
        }
    }

    override suspend fun saveScheduleTemplate(deviceSN: String, scheduleTemplate: ScheduleTemplate) {
        if (isDemoUser()) {
            demoFoxESSNetworking.saveScheduleTemplate(deviceSN, scheduleTemplate)
        } else {
            network.saveScheduleTemplate(deviceSN, scheduleTemplate)
        }
    }
}