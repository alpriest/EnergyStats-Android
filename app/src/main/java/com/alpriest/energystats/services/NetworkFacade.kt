package com.alpriest.energystats.services

import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.statsgraph.ReportType
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class NetworkFacade(private val network: FoxESSNetworking, private val isDemoUser: () -> Boolean) : FoxESSNetworking {
    private val demoFoxESSNetworking = DemoFoxESSNetworking()
    private val throttler = ThrottleManager()

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
            val method = "openapi_fetchHistory"
            try {
                throttler.throttle(method)
                return network.openapi_fetchHistory(deviceSN, variables, start, end)
            } finally {
                throttler.didInvoke(method)
            }
        }
    }

    override suspend fun openapi_fetchVariables(): List<OpenApiVariable> {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchVariables()
        } else {
            val method = "openapi_fetchVariables"
            try {
                throttler.throttle(method)
                return network.openapi_fetchVariables()
            } finally {
                throttler.didInvoke(method)
            }
        }
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
        } else {
            val method = "openapi_fetchReport"
            try {
                throttler.throttle(method)
                return network.openapi_fetchReport(deviceSN, variables, queryDate, reportType)
            } finally {
                throttler.didInvoke(method)
            }
        }
    }

    override suspend fun openapi_fetchBatterySOC(deviceSN: String): BatterySOCResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchBatterySOC(deviceSN)
        } else {
            return network.openapi_fetchBatterySOC(deviceSN)
        }
    }

    override suspend fun openapi_setBatterySoc(deviceSN: String, minSOCOnGrid: Int, minSOC: Int) {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_setBatterySoc(deviceSN, minSOCOnGrid, minSOC)
        } else {
            return network.openapi_setBatterySoc(deviceSN, minSOCOnGrid, minSOC)
        }
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenQueryResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchRealData(deviceSN, variables)
        } else {
            val method = "openapi_fetchRealData"
            try {
                throttler.throttle(method)
                network.openapi_fetchRealData(deviceSN, variables)
            } finally {
                throttler.didInvoke(method)
            }
        }
    }

    override suspend fun openapi_fetchDataLoggers(): List<DataLoggerResponse> {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchDataLoggers()
        } else {
            network.openapi_fetchDataLoggers()
        }
    }

    override suspend fun openapi_fetchBatteryTimes(deviceSN: String): List<ChargeTime> {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchBatteryTimes(deviceSN)
        } else {
            network.openapi_fetchBatteryTimes(deviceSN)
        }
    }

    override suspend fun openapi_fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchSchedulerFlag(deviceSN)
        } else {
            network.openapi_fetchSchedulerFlag(deviceSN)
        }
    }

    override suspend fun openapi_fetchCurrentSchedule(deviceSN: String): ScheduleResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.openapi_fetchCurrentSchedule(deviceSN)
        } else {
            val method = "openapi_fetchCurrentSchedule"
            try {
                throttler.throttle(method)
                network.openapi_fetchCurrentSchedule(deviceSN)
            } finally {
                throttler.didInvoke(method)
            }
        }
    }

    override suspend fun openapi_setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean) {
        if (isDemoUser()) {
            demoFoxESSNetworking.openapi_setScheduleFlag(deviceSN, schedulerEnabled)
        } else {
            return network.openapi_setScheduleFlag(deviceSN, schedulerEnabled)
        }
    }

    override suspend fun openapi_saveSchedule(deviceSN: String, schedule: Schedule) {
        if (isDemoUser()) {
            demoFoxESSNetworking.openapi_saveSchedule(deviceSN, schedule)
        } else {
            return network.openapi_saveSchedule(deviceSN, schedule)
        }
    }

    override suspend fun fetchErrorMessages() {
        if (isDemoUser()) {
            demoFoxESSNetworking.fetchErrorMessages()
        } else {
            network.fetchErrorMessages()
        }
    }

//    override suspend fun fetchSchedulerFlag(deviceSN: String): SchedulerFlagResponse {
//        return if (isDemoUser()) {
//            demoFoxESSNetworking.fetchSchedulerFlag(deviceSN)
//        } else {
//            network.fetchSchedulerFlag(deviceSN)
//        }
//    }
//
//    override suspend fun fetchScheduleModes(deviceID: String): List<SchedulerModeResponse> {
//        return if (isDemoUser()) {
//            demoFoxESSNetworking.fetchScheduleModes(deviceID)
//        } else {
//            network.fetchScheduleModes(deviceID)
//        }
//    }
//
//    override suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleListResponse {
//        return if (isDemoUser()) {
//            demoFoxESSNetworking.fetchCurrentSchedule(deviceSN)
//        } else {
//            network.fetchCurrentSchedule(deviceSN)
//        }
//    }
//
//    override suspend fun saveSchedule(deviceSN: String, schedule: Schedule) {
//        if (isDemoUser()) {
//            demoFoxESSNetworking.saveSchedule(deviceSN, schedule)
//        } else {
//            network.saveSchedule(deviceSN, schedule)
//        }
//    }
//
//    override suspend fun deleteSchedule(deviceSN: String) {
//        if (isDemoUser()) {
//            demoFoxESSNetworking.deleteSchedule(deviceSN)
//        } else {
//            network.deleteSchedule(deviceSN)
//        }
//    }
//
//    override suspend fun enableScheduleTemplate(deviceSN: String, templateID: String) {
//        if (isDemoUser()) {
//            demoFoxESSNetworking.enableScheduleTemplate(deviceSN, templateID)
//        } else {
//            network.enableScheduleTemplate(deviceSN, templateID)
//        }
//    }
//
//    override suspend fun deleteScheduleTemplate(templateID: String) {
//        if (isDemoUser()) {
//            demoFoxESSNetworking.deleteScheduleTemplate(templateID)
//        } else {
//            network.deleteScheduleTemplate(templateID)
//        }
//    }
//
//    override suspend fun fetchScheduleTemplate(deviceSN: String, templateID: String): ScheduleTemplateResponse {
//        return if (isDemoUser()) {
//            demoFoxESSNetworking.fetchScheduleTemplate(deviceSN, templateID)
//        } else {
//            network.fetchScheduleTemplate(deviceSN, templateID)
//        }
//    }
//
//    override suspend fun createScheduleTemplate(name: String, description: String) {
//        if (isDemoUser()) {
//            demoFoxESSNetworking.createScheduleTemplate(name, description)
//        } else {
//            network.createScheduleTemplate(name, description)
//        }
//    }

//    override suspend fun fetchScheduleTemplates(): ScheduleTemplateListResponse {
//        return if (isDemoUser()) {
//            demoFoxESSNetworking.fetchScheduleTemplates()
//        } else {
//            network.fetchScheduleTemplates()
//        }
//    }
//
//    override suspend fun saveScheduleTemplate(deviceSN: String, scheduleTemplate: ScheduleTemplate) {
//        if (isDemoUser()) {
//            demoFoxESSNetworking.saveScheduleTemplate(deviceSN, scheduleTemplate)
//        } else {
//            network.saveScheduleTemplate(deviceSN, scheduleTemplate)
//        }
//    }
}

class ThrottleManager {
    private val lastCallTimes: ConcurrentHashMap<String, Long> = ConcurrentHashMap()

    suspend fun throttle(method: String) {
        val now = System.nanoTime()
        val lastCallTime = lastCallTimes[method]

        if (lastCallTime != null) {
            val timeSinceLastCall = now - lastCallTime
            val waitTime = 1_000_000_000 - timeSinceLastCall  // 1 second in nanoseconds

            if (waitTime > 0) {
                delay(waitTime / 1_000_000)  // Convert nanoseconds to milliseconds for delay
            }
        }

        lastCallTimes[method] = now
    }

    fun didInvoke(method: String) {
        lastCallTimes[method] = System.nanoTime()
    }
}