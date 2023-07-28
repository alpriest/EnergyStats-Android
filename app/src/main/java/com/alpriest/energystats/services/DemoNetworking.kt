package com.alpriest.energystats.services

import com.alpriest.energystats.models.AddressBookResponse
import com.alpriest.energystats.models.BatteryResponse
import com.alpriest.energystats.models.BatterySettingsResponse
import com.alpriest.energystats.models.Earning
import com.alpriest.energystats.models.EarningsResponse
import com.alpriest.energystats.models.NetworkDevice
import com.alpriest.energystats.models.PagedDeviceListResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.models.ReportResponse
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.SoftwareVersion
import com.alpriest.energystats.models.VariablesResponse
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.alpriest.energystats.ui.statsgraph.ReportType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                data = ArrayList(response.data.map {
                    val simpleDate = SimpleDateFormat(dateFormat, Locale.getDefault()).parse(it.time)
                    val localDateTime = simpleDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        .withYear(now.year)
                        .withMonth(now.monthValue)
                        .withDayOfMonth(now.dayOfMonth)

                    RawData(time = localDateTime.format(formatter), value = it.value)
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
        return EarningsResponse(Earning(generation = 11.5, earnings = 54.2))
    }

    override suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String) {
    }
}
