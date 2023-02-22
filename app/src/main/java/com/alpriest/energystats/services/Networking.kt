package com.alpriest.energystats.services

import com.alpriest.energystats.models.*

class InvalidTokenException : Exception("Invalid Token")
class BadCredentialsException : Exception("Bad Credentials")
class TryLaterException : Exception("Try Later")

open class InvalidConfiguration(message: String) : Exception(message)

class MissingDataException : Exception("Missing data")
interface Networking {
    suspend fun fetchDeviceList(): PagedDeviceListResponse
    suspend fun ensureHasToken()
    suspend fun verifyCredentials(username: String, password: String)
    suspend fun fetchBattery(): BatteryResponse
    suspend fun fetchBatterySettings(): BatterySettingsResponse
    suspend fun fetchReport(variables: Array<ReportVariable>, queryDate: QueryDate): ArrayList<ReportResponse>
    suspend fun fetchRaw(variables: Array<RawVariable>): List<RawResponse>
}

data class NetworkResponse<T>(override val errno: Int, val result: T?) : NetworkResponseInterface
data class NetworkRawResponse(override val errno: Int, val result: ArrayList<RawResponse>?) : NetworkResponseInterface
data class NetworkReportResponse(override val errno: Int, val result: ArrayList<ReportResponse>?) : NetworkResponseInterface
