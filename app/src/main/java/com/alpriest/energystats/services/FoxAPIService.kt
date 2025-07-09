package com.alpriest.energystats.services

import com.alpriest.energystats.BuildConfig
import com.alpriest.energystats.models.ApiRequestCountResponse
import com.alpriest.energystats.models.BatterySOCResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DataLoggerListRequest
import com.alpriest.energystats.models.DataLoggerResponse
import com.alpriest.energystats.models.DataLoggerStatus
import com.alpriest.energystats.models.DataLoggerStatusDeserializer
import com.alpriest.energystats.models.DeviceDetailResponse
import com.alpriest.energystats.models.DeviceListRequest
import com.alpriest.energystats.models.DeviceSettingsItem
import com.alpriest.energystats.models.DeviceSummaryResponse
import com.alpriest.energystats.models.ErrorMessagesResponse
import com.alpriest.energystats.models.FetchDeviceSettingsItemRequest
import com.alpriest.energystats.models.FetchDeviceSettingsItemResponse
import com.alpriest.energystats.models.FetchPeakShavingSettingsRequest
import com.alpriest.energystats.models.FetchPeakShavingSettingsResponse
import com.alpriest.energystats.models.GetSchedulerFlagRequest
import com.alpriest.energystats.models.GetSchedulerFlagResponse
import com.alpriest.energystats.models.OpenApiVariable
import com.alpriest.energystats.models.OpenApiVariableArray
import com.alpriest.energystats.models.OpenApiVariableDeserializer
import com.alpriest.energystats.models.OpenHistoryRequest
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.OpenRealQueryRequest
import com.alpriest.energystats.models.OpenRealQueryResponse
import com.alpriest.energystats.models.OpenRealQueryResponseDeserializer
import com.alpriest.energystats.models.OpenReportRequest
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.OpenReportResponseDeserializer
import com.alpriest.energystats.models.PagedDataLoggerListResponse
import com.alpriest.energystats.models.PagedDeviceListResponse
import com.alpriest.energystats.models.PagedPowerStationListResponse
import com.alpriest.energystats.models.PowerStationDetailResponse
import com.alpriest.energystats.models.PowerStationListRequest
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ScheduleResponse
import com.alpriest.energystats.models.SetBatterySOCRequest
import com.alpriest.energystats.models.SetBatteryTimesRequest
import com.alpriest.energystats.models.SetCurrentScheduleRequest
import com.alpriest.energystats.models.SetDeviceSettingsItemRequest
import com.alpriest.energystats.models.SetPeakShavingSettingsRequest
import com.alpriest.energystats.models.SetSchedulerFlagRequest
import com.alpriest.energystats.models.md5
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.solcast.UserAgent
import com.alpriest.energystats.ui.statsgraph.ReportType
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val Boolean.intValue: Int
    get() {
        return if (this) 1 else 0
    }

interface NetworkResponseInterface {
    val errno: Int
}

class FoxAPIService(private val credentials: CredentialStore, private val store: InMemoryLoggingNetworkStore, interceptor: Interceptor? = null) : FoxAPIServicing {
    private fun makeSignature(encodedPath: String, token: String, timestamp: Long): String {
        return listOf(encodedPath, token, timestamp.toString()).joinToString("\\r\\n").md5() ?: ""
    }

    private val okHttpClient by lazy {
        val builder = OkHttpClient()
            .newBuilder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val languageCode = Locale.getDefault().toLanguageTag().split("-")[0].ifEmpty { "en" }
                val timezone = TimeZone.getDefault().id
                val token = credentials.getApiKey() ?: ""
                val timestamp = System.currentTimeMillis()
                val signature = makeSignature(original.url.encodedPath, token, timestamp)

                val requestBuilder = original.newBuilder()
                    .header("token", credentials.getApiKey() ?: "")
                    .header("User-Agent", UserAgent.description())
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Language", "en-US;q=0.9,en;q=0.8,de;q=0.7,nl;q=0.6")
                    .header("Content-Type", "application/json")
                    .header("lang", languageCode)
                    .header("timezone", timezone)
                    .header("timestamp", timestamp.toString())
                    .header("signature", signature)

                chain.proceed(requestBuilder.build())
            }
            .addInterceptor { chain ->
                InMemoryLoggingNetworkStore.shared.latestRequest = chain.request().toString()
                chain.proceed(chain.request())
            }

        interceptor?.let {
            builder.addInterceptor(it)
        }

        builder.build()
    }

    private var errorMessages = mutableMapOf<String, String>()

    override suspend fun fetchErrorMessages() {
        val request = Request.Builder().url(URLs.getErrorMessages()).build()

        val type = object : TypeToken<NetworkResponse<ErrorMessagesResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<ErrorMessagesResponse>> = fetch(request, type)
        response.item.result?.messages?.let {
            val language = Locale.getDefault().toLanguageTag().split("-")[0].ifEmpty { "en" }
            this.errorMessages = it[language] ?: mutableMapOf()
        }
    }

    override suspend fun openapi_fetchDeviceList(): List<DeviceSummaryResponse> {
        val body = Gson().toJson(DeviceListRequest())
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.getOpenDeviceList())
            .build()

        val type = object : TypeToken<NetworkResponse<PagedDeviceListResponse>>() {}.type
        val result: NetworkTuple<NetworkResponse<PagedDeviceListResponse>> = fetch(request, type)
        store.deviceListResponseStream.value = NetworkOperation(description = "fetchDeviceList", value = result.item, raw = result.text, request)
        return result.item.result?.data ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenRealQueryResponse {
        val body = Gson().toJson(OpenRealQueryRequest(deviceSN, variables))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.getOpenRealData())
            .build()

        val type = object : TypeToken<NetworkResponse<List<OpenRealQueryResponse>>>() {}.type
        val result: NetworkTuple<NetworkResponse<List<OpenRealQueryResponse>>> = fetch(request, type)
        store.realQueryResponseStream.value = NetworkOperation(description = "fetchRealData", value = result.item, raw = result.text, request)

        return result.item.result?.let { list ->
            list.firstOrNull { it.deviceSN == deviceSN }
        } ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        val body = Gson().toJson(OpenHistoryRequest(deviceSN, variables, start, end))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.getOpenHistoryData())
            .build()

        val type = object : TypeToken<NetworkResponse<List<OpenHistoryResponse>>>() {}.type
        val result: NetworkTuple<NetworkResponse<List<OpenHistoryResponse>>> = fetch(request, type)

        return result.item.result?.let { list ->
            list.firstOrNull { it.deviceSN == deviceSN }
        } ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        val body = Gson().toJson(OpenReportRequest(deviceSN, variables.map { it.networkTitle() }, reportType, queryDate.year, queryDate.month, queryDate.day))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.getOpenReportData())
            .build()

        val type = object : TypeToken<NetworkResponse<List<OpenReportResponse>>>() {}.type
        val response: NetworkTuple<NetworkResponse<List<OpenReportResponse>>> = fetch(request, type)
        store.reportResponseStream.value = NetworkOperation("fetchReport", response.item, response.text, request)

        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchBatterySettings(deviceSN: String): BatterySOCResponse {
        val request = Request.Builder().url(URLs.getOpenBatterySOC(deviceSN)).build()

        val type = object : TypeToken<NetworkResponse<BatterySOCResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<BatterySOCResponse>> = fetch(request, type)
        store.batterySOCResponseStream.value = NetworkOperation("fetchBatterySettings", response.item, response.text, request)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_setBatterySoc(deviceSN: String, minSOCOnGrid: Int, minSOC: Int) {
        val body = Gson().toJson(SetBatterySOCRequest(minSocOnGrid = minSOCOnGrid, minSoc = minSOC, sn = deviceSN))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.setOpenBatterySOC())
            .build()

        executeWithoutResponse(request)
    }

    override suspend fun openapi_fetchDevice(deviceSN: String): DeviceDetailResponse {
        val request = Request.Builder().url(URLs.getOpenDeviceDetail(deviceSN)).build()

        val type = object : TypeToken<NetworkResponse<DeviceDetailResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<DeviceDetailResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchPowerStationList(): PagedPowerStationListResponse {
        val body = Gson().toJson(PowerStationListRequest())
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.getOpenPlantList())
            .build()

        val type = object : TypeToken<NetworkResponse<PagedPowerStationListResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<PagedPowerStationListResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchPowerStationDetail(stationID: String): PowerStationDetailResponse {
        val request = Request.Builder().url(URLs.getOpenPlantDetail(stationID)).build()

        val type = object : TypeToken<NetworkResponse<PowerStationDetailResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<PowerStationDetailResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchRequestCount(): ApiRequestCountResponse {
        val request = Request.Builder().url(URLs.getRequestCount()).build()

        val type = object : TypeToken<NetworkResponse<ApiRequestCountResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<ApiRequestCountResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchVariables(): List<OpenApiVariable> {
        val request = Request.Builder().url(URLs.getOpenVariables()).build()

        val type = object : TypeToken<NetworkResponse<OpenApiVariableArray>>() {}.type
        val response: NetworkTuple<NetworkResponse<OpenApiVariableArray>> = fetch(request, type)
        store.variablesResponseStream.value = NetworkOperation(description = "fetchVariables", value = response.item, raw = response.text, request)
        return response.item.result?.array ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchDataLoggers(): List<DataLoggerResponse> {
        val body = Gson().toJson(DataLoggerListRequest())
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder().url(URLs.getOpenModuleList()).post(body).build()

        val type = object : TypeToken<NetworkResponse<PagedDataLoggerListResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<PagedDataLoggerListResponse>> = fetch(request, type)
        store.dataLoggerListResponse.value = NetworkOperation(description = "DataLoggerResponse", value = response.item, raw = response.text, request)
        return response.item.result?.data ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchBatteryTimes(deviceSN: String): List<ChargeTime> {
        val request = Request.Builder().url(URLs.getOpenBatteryChargeTimes(deviceSN)).build()

        val type = object : TypeToken<NetworkResponse<BatteryTimesResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<BatteryTimesResponse>> = fetch(request, type)
        store.batteryTimesResponseStream.value = NetworkOperation(description = "BatteryLoggerResponse", value = response.item, raw = response.text, request)
        val result = response.item.result ?: throw MissingDataException()

        return listOf(
            ChargeTime(enable = result.enable1, startTime = result.startTime1, endTime = result.endTime1),
            ChargeTime(enable = result.enable2, startTime = result.startTime2, endTime = result.endTime2)
        )
    }

    override suspend fun openapi_setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        if (times.count() < 2) {
            return
        }

        val body = Gson().toJson(
            SetBatteryTimesRequest(
                sn = deviceSN,
                enable1 = times[0].enable,
                startTime1 = times[0].startTime,
                endTime1 = times[0].endTime,
                enable2 = times[1].enable,
                startTime2 = times[1].startTime,
                endTime2 = times[1].endTime
            )
        ).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(URLs.setOpenBatteryChargeTimes())
            .post(body)
            .build()

        executeWithoutResponse(request)
    }

    override suspend fun openapi_fetchSchedulerFlag(deviceSN: String): GetSchedulerFlagResponse {
        val body = Gson().toJson(GetSchedulerFlagRequest(deviceSN))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder().url(URLs.getOpenSchedulerFlag()).post(body).build()

        val type = object : TypeToken<NetworkResponse<GetSchedulerFlagResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<GetSchedulerFlagResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchCurrentSchedule(deviceSN: String): ScheduleResponse {
        val body = Gson().toJson(GetSchedulerFlagRequest(deviceSN))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder().url(URLs.getOpenCurrentSchedule()).post(body).build()

        val type = object : TypeToken<NetworkResponse<ScheduleResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<ScheduleResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_setScheduleFlag(deviceSN: String, schedulerEnabled: Boolean) {
        val body = Gson().toJson(SetSchedulerFlagRequest(deviceSN, schedulerEnabled.intValue))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.setOpenSchedulerFlag())
            .build()

        executeWithoutResponse(request)
    }

    override suspend fun openapi_saveSchedule(deviceSN: String, schedule: Schedule) {
        val body = Gson().toJson(SetCurrentScheduleRequest(deviceSN, schedule.phases.map { it.toPhaseResponse() }))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.setOpenCurrentSchedule())
            .build()

        executeWithoutResponse(request)
    }

    override suspend fun openapi_fetchDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem): FetchDeviceSettingsItemResponse {
        val body = Gson().toJson(FetchDeviceSettingsItemRequest(deviceSN, item.rawValue))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.fetchDeviceSettingsItem())
            .build()

        val type = object : TypeToken<NetworkResponse<FetchDeviceSettingsItemResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<FetchDeviceSettingsItemResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_setDeviceSettingsItem(deviceSN: String, item: DeviceSettingsItem, value: String) {
        val body = Gson().toJson(SetDeviceSettingsItemRequest(deviceSN, item.rawValue, value))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.setDeviceSettingsItem())
            .build()

        executeWithoutResponse(request)
    }

    override suspend fun openapi_fetchPeakShavingSettings(deviceSN: String): FetchPeakShavingSettingsResponse {
        val body = Gson().toJson(FetchPeakShavingSettingsRequest(deviceSN))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.getDevicePeakShavingSettings())
            .build()

        val type = object : TypeToken<NetworkResponse<FetchPeakShavingSettingsResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<FetchPeakShavingSettingsResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_setPeakShavingSettings(deviceSN: String, importLimit: Double, soc: Int) {
        val body = Gson().toJson(SetPeakShavingSettingsRequest(deviceSN, importLimit, soc))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.getDevicePeakShavingSettings())
            .build()

        executeWithoutResponse(request)
    }

    private suspend fun executeWithoutResponse(request: Request) {
        val type = object : TypeToken<NetworkResponse<String>>() {}.type
        fetch<NetworkResponse<String>>(request, type)
    }

    private suspend fun <T : NetworkResponseInterface> fetch(
        request: Request,
        type: Type
    ): NetworkTuple<T> {
        return suspendCoroutine { continuation ->
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 406) {
                        continuation.resumeWithException(UnacceptableException())
                        return
                    }

                    if (response.code !in 200 .. 299) {
                        continuation.resumeWithException(UnknownServerError(response.code))
                        return
                    }

                    InMemoryLoggingNetworkStore.shared.latestResponse = response.toString()

                    try {
                        val text = response.body?.string()
                        InMemoryLoggingNetworkStore.shared.latestResponseText = text
                        val builder = GsonBuilder()
                            .registerTypeAdapter(OpenApiVariableArray::class.java, OpenApiVariableDeserializer())
                            .registerTypeAdapter(OpenReportResponse::class.java, OpenReportResponseDeserializer())
                            .registerTypeAdapter(DataLoggerStatus::class.java, DataLoggerStatusDeserializer())
                            .registerTypeAdapter(OpenRealQueryResponse::class.java, OpenRealQueryResponseDeserializer())
                            .registerTypeAdapter(ScheduleResponse::class.java, ScheduleResponse.Deserializer())
                            .create()
                        val body: T = builder.fromJson(text, type)
                        val result: Result<T> = check(body)

                        result.fold(
                            onSuccess = { continuation.resume(NetworkTuple(it, text)) },
                            onFailure = { continuation.resumeWithException(it) }
                        )
                    } catch (ex: Exception) {
                        continuation.resumeWithException(ex)
                    }
                }
            })
        }
    }

    private fun <T : NetworkResponseInterface> check(item: T): Result<T> {
        when (item.errno) {
            41808, 41809, 41810 -> {
                return Result.failure(InvalidTokenException())
            }

            41807 -> {
                return Result.failure(BadCredentialsException())
            }

            40401 -> {
                return Result.failure(TryLaterException())
            }

            30000 -> {
                return Result.failure(MaintenanceModeException())
            }

            44096 -> {
                return Result.failure(ProhibitedActionException())
            }
        }

        if (item.errno > 0) {
            return Result.failure(FoxServerError(item.errno, errorMessages[item.errno.toString()]))
        }

        return Result.success(item)
    }
}

data class NetworkTuple<T : NetworkResponseInterface>(
    val item: T,
    val text: String?
)
