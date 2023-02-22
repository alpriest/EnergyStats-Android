package com.alpriest.energystats.services

import android.content.res.AssetManager
import com.alpriest.energystats.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DemoNetworking : Networking {
    override suspend fun ensureHasToken() {
        // Do nothing
    }

    override suspend fun verifyCredentials(username: String, password: String) {
        // Assume valid
    }

    override suspend fun fetchBattery(): BatteryResponse {
        return BatteryResponse(power = 0.27, soc = 20, residual = 2420.0)
    }

    override suspend fun fetchBatterySettings(): BatterySettingsResponse {
        return BatterySettingsResponse(20)
    }

    override suspend fun fetchReport(
        variables: Array<ReportVariable>,
        queryDate: QueryDate
    ): ArrayList<ReportResponse> {
        return ArrayList(
            arrayOf(
                ReportResponse(
                    variable = "feedin",
                    data = arrayOf(ReportData(14, 1.5))
                )
            ).asList()
        )
    }

    override suspend fun fetchRaw(variables: Array<RawVariable>): ArrayList<RawResponse> {
        val itemType = object : TypeToken<NetworkRawResponse>() {}.type
        val rawData = rawData()
        val result: NetworkRawResponse = Gson().fromJson(rawData, itemType)
        return ArrayList(result.result!!.map { response ->
            RawResponse(
                variable = response.variable,
                data = ArrayList(response.data.map {
                    RawData(time = it.time, value = it.value)
                }.toList())
            )
        }.toList())
    }

    override suspend fun fetchDeviceList(): PagedDeviceListResponse {
        return PagedDeviceListResponse(
            currentPage = 1, pageSize = 1, total = 1, devices = arrayListOf(
                Device(deviceID = "abcdef", deviceSN = "123123", hasBattery = true, hasPV = true)
            )
        )
    }

    private fun rawData(): String {
        return DemoRawData
    }
}

fun AssetManager.readFile(fileName: String) = open(fileName)
    .bufferedReader()
    .use { it.readText() }