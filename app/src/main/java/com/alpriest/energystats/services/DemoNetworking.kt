package com.alpriest.energystats.services

import android.content.res.AssetManager
import android.util.JsonReader
import android.util.JsonWriter
import com.alpriest.energystats.models.*
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DemoNetworking : Networking {
    override suspend fun ensureHasToken() {
        // Do nothing
    }

    override suspend fun verifyCredentials(username: String, password: String) {
        // Assume valid
    }

    override suspend fun fetchBattery(): BatteryResponse {
        return BatteryResponse(power = 0.131, soc = 25, residual = 2420.0)
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
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
            .create()
        val result: NetworkRawResponse = gson.fromJson(rawData, itemType)
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

class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {
    override fun write(out: com.google.gson.stream.JsonWriter?, value: LocalDate?) {
        out?.value(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value))
    }

    override fun read(`in`: com.google.gson.stream.JsonReader?): LocalDate {
        return LocalDate.parse(`in`?.nextString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zZ"))
    }
}
