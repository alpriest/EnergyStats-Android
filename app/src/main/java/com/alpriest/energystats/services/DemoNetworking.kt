package com.alpriest.energystats.services

import android.os.Build
import androidx.annotation.RequiresApi
import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class DemoNetworking : Networking {
    override suspend fun ensureHasToken() {
        // Do nothing
    }

    override suspend fun verifyCredentials(username: String, password: String) {
        // Assume valid
    }

    override suspend fun fetchBattery(): BatteryResponse {
        return BatteryResponse(power = 0.131, soc = 25, residual = 2420.0, temperature = 13.6)
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

    @RequiresApi(Build.VERSION_CODES.O)
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
                    RawData(time = SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date()), value = it.value)
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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun write(out: com.google.gson.stream.JsonWriter?, value: LocalDate?) {
        out?.value(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun read(`in`: com.google.gson.stream.JsonReader?): LocalDate {
        return LocalDate.parse(`in`?.nextString(), DateTimeFormatter.ofPattern(dateFormat))
    }
}
