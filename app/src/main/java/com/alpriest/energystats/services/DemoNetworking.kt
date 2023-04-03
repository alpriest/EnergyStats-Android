package com.alpriest.energystats.services

import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class DemoNetworking : Networking {
    override suspend fun ensureHasToken() {
        // Do nothing
    }

    override suspend fun verifyCredentials(username: String, password: String) {
        // Assume valid
    }

    override suspend fun fetchBattery(deviceID: String): BatteryResponse {
        return BatteryResponse(power = 0.131, soc = 25, residual = 2420.0, temperature = 13.6)
    }

    override suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse {
        return BatterySettingsResponse(20)
    }

    override suspend fun fetchReport(
        deviceID: String,
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

    override suspend fun fetchAddressBook(deviceID: String): AddressBookResponse {
        return AddressBookResponse(softVersion = SoftwareVersion(master = "1.54", slave = "1.09", manager = "1.49"))
    }

    override suspend fun fetchRaw(deviceID: String, variables: Array<RawVariable>): ArrayList<RawResponse> {
        val itemType = object : TypeToken<NetworkRawResponse>() {}.type
        val rawData = rawData()
        val gson = GsonBuilder().create()
        val result: NetworkRawResponse = gson.fromJson(rawData, itemType)
        return ArrayList(result.result!!.map { response ->
            RawResponse(
                variable = response.variable,
                data = ArrayList(response.data.map {
                    RawData(time = SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date()), value = it.value)
                }.toList())
            )
        }.toList())
    }

    override suspend fun fetchDeviceList(): PagedDeviceListResponse {
        return PagedDeviceListResponse(
            currentPage = 1, pageSize = 1, total = 1, devices = arrayListOf(
                NetworkDevice(plantName = "plant 1", deviceID = "abcdef", deviceSN = "123123", hasBattery = true, hasPV = true),
                NetworkDevice(plantName = "plant 2", deviceID = "ppplll", deviceSN = "998877", hasBattery = true, hasPV = true)
            )
        )
    }

    private fun rawData(): String {
        return DemoRawData
    }
}
