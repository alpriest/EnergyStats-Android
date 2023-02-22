package com.alpriest.energystats.services

import android.content.Context
import com.alpriest.energystats.models.*

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

    override suspend fun fetchBattery(): BatteryResponse {
        return if (config.isDemoUser) {
            demoNetworking.fetchBattery()
        } else {
            network.fetchBattery()
        }
    }

    override suspend fun fetchBatterySettings(): BatterySettingsResponse {
        return if (config.isDemoUser) {
            demoNetworking.fetchBatterySettings()
        } else {
            network.fetchBatterySettings()
        }
    }

    override suspend fun fetchReport(variables: Array<ReportVariable>, queryDate: QueryDate): ArrayList<ReportResponse> {
        return if (config.isDemoUser) {
            demoNetworking.fetchReport(variables, queryDate)
        } else {
            network.fetchReport(variables, queryDate)
        }
    }

    override suspend fun fetchRaw(variables: Array<RawVariable>): List<RawResponse> {
        return if (config.isDemoUser) {
            demoNetworking.fetchRaw(variables)
        } else {
            network.fetchRaw(variables)
        }
    }
}