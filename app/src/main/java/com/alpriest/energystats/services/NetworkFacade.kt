package com.alpriest.energystats.services

import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.statsgraph.ReportType

class NetworkFacade(private val network: Networking, private val isDemoUser: () -> Boolean) : Networking {
    private val demoNetworking = DemoNetworking()

    override suspend fun fetchDeviceList(): PagedDeviceListResponse {
        return if (isDemoUser()) {
            demoNetworking.fetchDeviceList()
        } else {
            network.fetchDeviceList()
        }
    }

    override suspend fun ensureHasToken() {
        return if (isDemoUser()) {
            demoNetworking.ensureHasToken()
        } else {
            network.ensureHasToken()
        }
    }

    override suspend fun verifyCredentials(username: String, password: String) {
        return if (isDemoUser()) {
            demoNetworking.verifyCredentials(username, password)
        } else {
            network.verifyCredentials(username, password)
        }
    }

    override suspend fun fetchBattery(deviceID: String): BatteryResponse {
        return if (isDemoUser()) {
            demoNetworking.fetchBattery(deviceID)
        } else {
            network.fetchBattery(deviceID)
        }
    }

    override suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse {
        return if (isDemoUser()) {
            demoNetworking.fetchBatterySettings(deviceSN)
        } else {
            network.fetchBatterySettings(deviceSN)
        }
    }

    override suspend fun fetchReport(deviceID: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): ArrayList<ReportResponse> {
        return if (isDemoUser()) {
            demoNetworking.fetchReport(deviceID, variables, queryDate, reportType)
        } else {
            network.fetchReport(deviceID, variables, queryDate, reportType)
        }
    }

    override suspend fun fetchAddressBook(deviceID: String): AddressBookResponse {
        return if (isDemoUser()) {
            demoNetworking.fetchAddressBook(deviceID)
        } else {
            network.fetchAddressBook(deviceID)
        }
    }

    override suspend fun fetchRaw(deviceID: String, variables: List<RawVariable>, queryDate: QueryDate): ArrayList<RawResponse> {
        return if (isDemoUser()) {
            demoNetworking.fetchRaw(deviceID, variables, queryDate)
        } else {
            network.fetchRaw(deviceID, variables, queryDate)
        }
    }

    override suspend fun fetchVariables(deviceID: String): List<RawVariable> {
        return if (isDemoUser()) {
            demoNetworking.fetchVariables(deviceID)
        } else {
            network.fetchVariables(deviceID)
        }
    }

    override suspend fun fetchEarnings(deviceID: String): EarningsResponse {
        return if (isDemoUser()) {
            demoNetworking.fetchEarnings(deviceID)
        } else {
            network.fetchEarnings(deviceID)
        }
    }

    override suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String) {
        return if (isDemoUser()) {
            demoNetworking.setSoc(minGridSOC, minSOC, deviceSN)
        } else {
            network.setSoc(minGridSOC, minSOC, deviceSN)
        }
    }

    override suspend fun fetchBatteryTimes(deviceSN: String): BatteryTimesResponse {
        return if (isDemoUser()) {
            demoNetworking.fetchBatteryTimes(deviceSN)
        } else {
            network.fetchBatteryTimes(deviceSN)
        }
    }

    override suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        return if (isDemoUser()) {
            demoNetworking.setBatteryTimes(deviceSN, times)
        } else {
            network.setBatteryTimes(deviceSN, times)
        }
    }

    override suspend fun fetchWorkMode(deviceID: String): DeviceSettingsGetResponse {
        return if (isDemoUser()) {
            demoNetworking.fetchWorkMode(deviceID)
        } else {
            network.fetchWorkMode(deviceID)
        }
    }

    override suspend fun setWorkMode(deviceID: String, workMode: String) {
        return if (isDemoUser()) {
            demoNetworking.setWorkMode(deviceID, workMode)
        } else {
            network.setWorkMode(deviceID, workMode)
        }
    }

    override suspend fun fetchDataLoggers(): PagedDataLoggerListResponse {
        return if (isDemoUser()) {
            demoNetworking.fetchDataLoggers()
        } else {
            network.fetchDataLoggers()
        }
    }

    override suspend fun fetchErrorMessages() {
        if (isDemoUser()) {
            demoNetworking.fetchErrorMessages()
        } else {
            network.fetchErrorMessages()
        }
    }
}