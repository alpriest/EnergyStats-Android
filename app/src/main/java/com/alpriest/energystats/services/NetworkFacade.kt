package com.alpriest.energystats.services

import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.statsgraph.ReportType

class NetworkFacade(private val network: Networking, private val config: ConfigInterface) : Networking {
    private val demoNetworking = DemoNetworking()

    override suspend fun fetchDeviceList(): PagedDeviceListResponse {
        return if (config.isDemoUser) {
            demoNetworking.fetchDeviceList()
        } else {
            network.fetchDeviceList()
        }
    }

    override suspend fun ensureHasToken() {
        if (config.isDemoUser) {
            demoNetworking.ensureHasToken()
        } else {
            network.ensureHasToken()
        }
    }

    override suspend fun verifyCredentials(username: String, password: String) {
        if (config.isDemoUser) {
            demoNetworking.verifyCredentials(username, password)
        } else {
            network.verifyCredentials(username, password)
        }
    }

    override suspend fun fetchBattery(deviceID: String): BatteryResponse {
        return if (config.isDemoUser) {
            demoNetworking.fetchBattery(deviceID)
        } else {
            network.fetchBattery(deviceID)
        }
    }

    override suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse {
        return if (config.isDemoUser) {
            demoNetworking.fetchBatterySettings(deviceSN)
        } else {
            network.fetchBatterySettings(deviceSN)
        }
    }

    override suspend fun fetchReport(deviceID: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): ArrayList<ReportResponse> {
        return if (config.isDemoUser) {
            demoNetworking.fetchReport(deviceID, variables, queryDate, reportType)
        } else {
            network.fetchReport(deviceID, variables, queryDate, reportType)
        }
    }

    override suspend fun fetchAddressBook(deviceID: String): AddressBookResponse {
        return if (config.isDemoUser) {
            demoNetworking.fetchAddressBook(deviceID)
        } else {
            network.fetchAddressBook(deviceID)
        }
    }

    override suspend fun fetchRaw(deviceID: String, variables: List<RawVariable>, queryDate: QueryDate): ArrayList<RawResponse> {
        return if (config.isDemoUser) {
            demoNetworking.fetchRaw(deviceID, variables, queryDate)
        } else {
            network.fetchRaw(deviceID, variables, queryDate)
        }
    }

    override suspend fun fetchVariables(deviceID: String): List<RawVariable> {
        return if (config.isDemoUser) {
            demoNetworking.fetchVariables(deviceID)
        } else {
            network.fetchVariables(deviceID)
        }
    }

    override suspend fun fetchEarnings(deviceID: String): EarningsResponse {
        return if (config.isDemoUser) {
            demoNetworking.fetchEarnings(deviceID)
        } else {
            network.fetchEarnings(deviceID)
        }
    }

    override suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String) {
        return if(config.isDemoUser) {
            demoNetworking.setSoc(minGridSOC, minSOC, deviceSN)
        } else {
            network.setSoc(minGridSOC, minSOC, deviceSN)
        }
    }

    override suspend fun fetchBatteryTimes(deviceSN: String): BatteryTimesResponse {
        return if(config.isDemoUser) {
            demoNetworking.fetchBatteryTimes(deviceSN)
        } else {
            network.fetchBatteryTimes(deviceSN)
        }
    }

    override suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        return if(config.isDemoUser) {
            demoNetworking.setBatteryTimes(deviceSN, times)
        } else {
            network.setBatteryTimes(deviceSN, times)
        }
    }

    override suspend fun fetchWorkMode(deviceID: String): DeviceSettingsGetResponse {
        return if(config.isDemoUser) {
            demoNetworking.fetchWorkMode(deviceID)
        } else {
            network.fetchWorkMode(deviceID)
        }
    }

    override suspend fun setWorkMode(deviceID: String, workMode: String) {
        return if(config.isDemoUser) {
            demoNetworking.setWorkMode(deviceID, workMode)
        } else {
            network.setWorkMode(deviceID, workMode)
        }
    }

    override suspend fun fetchDataLoggers(): PagedDataLoggerListResponse {
        return if(config.isDemoUser) {
            demoNetworking.fetchDataLoggers()
        } else {
            network.fetchDataLoggers()
        }
    }
}