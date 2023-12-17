package com.alpriest.energystats.services

import com.alpriest.energystats.models.AddressBookResponse
import com.alpriest.energystats.models.BatteryResponse
import com.alpriest.energystats.models.BatterySettingsResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DeviceSettingsGetResponse
import com.alpriest.energystats.models.EarningsResponse
import com.alpriest.energystats.models.PagedDataLoggerListResponse
import com.alpriest.energystats.models.PagedDeviceListResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.models.ReportResponse
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ScheduleListResponse
import com.alpriest.energystats.models.SchedulerFlagResponse
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.statsgraph.ReportType
import java.lang.Math.abs

import java.util.Date

data class CachedItem(val item: Any) {
    val cacheTime: Date = Date()

    fun isFresherThan(seconds: Int): Boolean {
        return abs(Date().time - cacheTime.time) < (seconds * 1000L)
    }
}

class NetworkCache(private val network: FoxESSNetworking) : FoxESSNetworking {
    private var cache: MutableMap<String, CachedItem> = mutableMapOf()
    private val shortCacheDurationInSeconds = 3

    override suspend fun fetchDeviceList(): PagedDeviceListResponse {
        return network.fetchDeviceList()
    }

    override suspend fun ensureHasToken() {
        network.ensureHasToken()
    }

    override suspend fun verifyCredentials(username: String, password: String) {
        network.verifyCredentials(username, password)
    }

    override suspend fun fetchBattery(deviceID: String): BatteryResponse {
        val key = makeKey("fetchBattery", deviceID)

        val cached = cache[key]
        return if (cached != null && cached.item is BatteryResponse && cached.isFresherThan(seconds = shortCacheDurationInSeconds)) {
            cached.item
        } else {
            val fresh = network.fetchBattery(deviceID)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse {
        val key = makeKey("fetchBatterySettings", deviceSN)

        val cached = cache[key]
        return if (cached != null && cached.item is BatterySettingsResponse && cached.isFresherThan(seconds = shortCacheDurationInSeconds)) {
            cached.item
        } else {
            val fresh = network.fetchBatterySettings(deviceSN)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun fetchRaw(deviceID: String, variables: List<RawVariable>, queryDate: QueryDate): ArrayList<RawResponse> {
        return network.fetchRaw(deviceID, variables, queryDate)
    }

    override suspend fun fetchReport(deviceID: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): ArrayList<ReportResponse> {
        return network.fetchReport(deviceID, variables, queryDate, reportType)
    }

    override suspend fun fetchAddressBook(deviceID: String): AddressBookResponse {
        val key = makeKey("fetchAddressBook", deviceID)

        val cached = cache[key]
        return if (cached != null && cached.item is AddressBookResponse && cached.isFresherThan(seconds = shortCacheDurationInSeconds)) {
            cached.item
        } else {
            val fresh = network.fetchAddressBook(deviceID)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun fetchVariables(deviceID: String): List<RawVariable> {
        return network.fetchVariables(deviceID)
    }

    override suspend fun fetchEarnings(deviceID: String): EarningsResponse {
        val key = makeKey("fetchEarnings", deviceID)

        val cached = cache[key]
        return if (cached != null && cached.item is EarningsResponse && cached.isFresherThan(seconds = shortCacheDurationInSeconds)) {
            cached.item
        } else {
            val fresh = network.fetchEarnings(deviceID)
            cache[key] = CachedItem(fresh)
            fresh
        }
    }

    override suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String) {
        network.setSoc(minGridSOC, minSOC, deviceSN)
    }

    override suspend fun fetchBatteryTimes(deviceSN: String): BatteryTimesResponse {
        return network.fetchBatteryTimes(deviceSN)
    }

    override suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        network.setBatteryTimes(deviceSN, times)
    }

    override suspend fun fetchWorkMode(deviceID: String): DeviceSettingsGetResponse {
        return network.fetchWorkMode(deviceID)
    }

    override suspend fun setWorkMode(deviceID: String, workMode: String) {
        network.setWorkMode(deviceID, workMode)
    }

    override suspend fun fetchDataLoggers(): PagedDataLoggerListResponse {
        return network.fetchDataLoggers()
    }

    override suspend fun fetchErrorMessages() {
        network.fetchErrorMessages()
    }

    override suspend fun fetchSchedulerFlag(deviceSN: String): SchedulerFlagResponse {
        return network.fetchSchedulerFlag(deviceSN)
    }

    override suspend fun fetchScheduleModes(deviceID: String): List<SchedulerModeResponse> {
        return network.fetchScheduleModes(deviceID)
    }

    override suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleListResponse {
        return network.fetchCurrentSchedule(deviceSN)
    }

    override suspend fun saveSchedule(deviceSN: String, schedule: Schedule) {
        network.saveSchedule(deviceSN, schedule)
    }

    override suspend fun deleteSchedule(deviceSN: String) {
        network.deleteSchedule(deviceSN)
    }

    private fun makeKey(base: String, vararg arguments: String): String {
        return listOf(base, *arguments).joinToString(separator = "_")
    }
}