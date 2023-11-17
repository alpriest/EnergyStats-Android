package com.alpriest.energystats.services

import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.statsgraph.ReportType

class NetworkFacade(private val network: FoxESSNetworking, private val isDemoUser: () -> Boolean) : FoxESSNetworking {
    private val demoFoxESSNetworking = DemoFoxESSNetworking()

    override suspend fun fetchDeviceList(): PagedDeviceListResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchDeviceList()
        } else {
            network.fetchDeviceList()
        }
    }

    override suspend fun ensureHasToken() {
        return if (isDemoUser()) {
            demoFoxESSNetworking.ensureHasToken()
        } else {
            network.ensureHasToken()
        }
    }

    override suspend fun verifyCredentials(username: String, password: String) {
        return if (isDemoUser()) {
            demoFoxESSNetworking.verifyCredentials(username, password)
        } else {
            network.verifyCredentials(username, password)
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

    override suspend fun fetchReport(deviceID: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): ArrayList<ReportResponse> {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchReport(deviceID, variables, queryDate, reportType)
        } else {
            network.fetchReport(deviceID, variables, queryDate, reportType)
        }
    }

    override suspend fun fetchAddressBook(deviceID: String): AddressBookResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchAddressBook(deviceID)
        } else {
            network.fetchAddressBook(deviceID)
        }
    }

    override suspend fun fetchRaw(deviceID: String, variables: List<RawVariable>, queryDate: QueryDate): ArrayList<RawResponse> {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchRaw(deviceID, variables, queryDate)
        } else {
            network.fetchRaw(deviceID, variables, queryDate)
        }
    }

    override suspend fun fetchVariables(deviceID: String): List<RawVariable> {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchVariables(deviceID)
        } else {
            network.fetchVariables(deviceID)
        }
    }

    override suspend fun fetchEarnings(deviceID: String): EarningsResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchEarnings(deviceID)
        } else {
            network.fetchEarnings(deviceID)
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

    override suspend fun fetchWorkMode(deviceID: String): DeviceSettingsGetResponse {
        return if (isDemoUser()) {
            demoFoxESSNetworking.fetchWorkMode(deviceID)
        } else {
            network.fetchWorkMode(deviceID)
        }
    }

    override suspend fun setWorkMode(deviceID: String, workMode: String) {
        return if (isDemoUser()) {
            demoFoxESSNetworking.setWorkMode(deviceID, workMode)
        } else {
            network.setWorkMode(deviceID, workMode)
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
}