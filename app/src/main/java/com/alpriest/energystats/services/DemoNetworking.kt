package com.alpriest.energystats.services

import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
        val fileContent = this::class.java.classLoader?.getResource("res/raw/report_day.json")?.readText()

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

    override suspend fun fetchRaw(deviceID: String, variables: List<RawVariable>): ArrayList<RawResponse> {
        val result = rawData(deviceID)
        return ArrayList(result.map { response ->
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
                NetworkDevice(plantName = "plant 1", deviceID = "f3000_deviceid", deviceSN = "123123", hasBattery = true, hasPV = true, deviceType = "F3000"),
                NetworkDevice(plantName = "plant 2", deviceID = "h1_deviceid", deviceSN = "998877", hasBattery = true, hasPV = true, deviceType = "H1-3.7-E")
            )
        )
    }

    override suspend fun fetchVariables(deviceID: String): List<RawVariable> {
        return listOf(
            RawVariable("PV1Volt", "pv1Volt", "V"),
            RawVariable("PV1Current", "pv1Current", "A"),
            RawVariable("PV1Power", "pv1Power", "kW"),
            RawVariable("PVPower", "pvPower", "kW"),
            RawVariable("PV2Volt", "pv2Volt", "V"),
            RawVariable("PV2Current", "pv2Current", "A"),
            RawVariable("aPV1Current", "pv1Current", "A"),
            RawVariable("aPV1Power", "pv1Power", "kW"),
            RawVariable("aPVPower", "pvPower", "kW"),
            RawVariable("aPV2Volt", "pv2Volt", "V"),
            RawVariable("aPV2Current", "pv2Current", "A"),
            RawVariable("bPV1Current", "pv1Current", "A"),
            RawVariable("bPV1Power", "pv1Power", "kW"),
            RawVariable("bPVPower", "pvPower", "kW"),
            RawVariable("bPV2Volt", "pv2Volt", "V"),
            RawVariable("cPV2Current", "pv2Current", "A"),
            RawVariable("cPV1Current", "pv1Current", "A"),
            RawVariable("cPV1Power", "pv1Power", "kW"),
            RawVariable("cPVPower", "pvPower", "kW"),
            RawVariable("cPV2Volt", "pv2Volt", "V"),
            RawVariable("dPV2Current", "pv2Current", "A"),
            RawVariable("dPV2Power", "pv2Power", "kW")
        )
    }

    private fun rawData(deviceID: String): List<RawResponse> {
        val fileContent = this::class.java.classLoader?.getResource("res/raw/raw_$deviceID.json")?.readText()

        val data: NetworkRawResponse = Gson().fromJson(fileContent, object : TypeToken<NetworkRawResponse>() {}.type)

        return data.result?.toList() ?: listOf()
    }

    override suspend fun fetchEarnings(deviceID: String): EarningsResponse {
        return EarningsResponse(Earning(generation = 11.5, earnings = 54.2))
    }
}
