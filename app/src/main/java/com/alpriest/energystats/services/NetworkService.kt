package com.alpriest.energystats.services

import com.alpriest.energystats.models.BatterySOCResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DataLoggerListRequest
import com.alpriest.energystats.models.DataLoggerResponse
import com.alpriest.energystats.models.DataLoggerStatus
import com.alpriest.energystats.models.DataLoggerStatusDeserializer
import com.alpriest.energystats.models.DeviceDetailResponse
import com.alpriest.energystats.models.DeviceListRequest
import com.alpriest.energystats.models.ErrorMessagesResponse
import com.alpriest.energystats.models.GetSchedulerFlagRequest
import com.alpriest.energystats.models.GetSchedulerFlagResponse
import com.alpriest.energystats.models.OpenApiVariable
import com.alpriest.energystats.models.OpenApiVariableArray
import com.alpriest.energystats.models.OpenApiVariableDeserializer
import com.alpriest.energystats.models.OpenHistoryRequest
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.OpenQueryRequest
import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.models.OpenReportRequest
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.OpenReportResponseDeserializer
import com.alpriest.energystats.models.PagedDeviceListResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ScheduleResponse
import com.alpriest.energystats.models.SetBatterySOCRequest
import com.alpriest.energystats.models.SetSchedulerFlagRequest
import com.alpriest.energystats.models.md5
import com.alpriest.energystats.stores.CredentialStore
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

class NetworkService(private val credentials: CredentialStore, private val store: InMemoryLoggingNetworkStore, interceptor: Interceptor? = null) : FoxESSNetworking {
    private fun makeSignature(encodedPath: String, token: String, timestamp: Long): String {
        return listOf(encodedPath, token, timestamp.toString()).joinToString("\\r\\n").md5() ?: ""
    }

    private val okHttpClient by lazy {
        val builder = OkHttpClient()
            .newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
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
                    .header("User-Agent", "Energy-Stats")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Language", "en-US;q=0.9,en;q=0.8,de;q=0.7,nl;q=0.6")
                    .header("Content-Type", "application/json")
                    .header("lang", languageCode)
                    .header("timezone", timezone)
                    .header("timestamp", timestamp.toString())
                    .header("signature", signature)

                chain.proceed(requestBuilder.build())
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

//    override suspend fun saveScheduleTemplate(deviceSN: String, scheduleTemplate: ScheduleTemplate) {
//        val body = Gson().toJson(
//            ScheduleSaveRequest(
//                pollcy = scheduleTemplate.phases.map { it.toPollcy() },
//                templateID = scheduleTemplate.id,
//                deviceSN
//            )
//        ).toRequestBody("application/json".toMediaTypeOrNull())
//
//        val request = Request.Builder()
//            .url(URLs.saveScheduleTemplate())
//            .method("POST", body)
//            .build()
//
//        val type = object : TypeToken<NetworkResponse<String>>() {}.type
//        fetch<NetworkResponse<String>>(request, type)
//    }
//
//    override suspend fun fetchScheduleTemplates(): ScheduleTemplateListResponse {
//        val request = Request.Builder().url(URLs.fetchScheduleTemplates()).build()
//
//        val type = object : TypeToken<NetworkResponse<ScheduleTemplateListResponse>>() {}.type
//        val response: NetworkTuple<NetworkResponse<ScheduleTemplateListResponse>> = fetch(request, type)
//        return response.item.result ?: throw MissingDataException()
//    }
//
//    override suspend fun createScheduleTemplate(name: String, description: String) {
//        val body = Gson().toJson(ScheduleTemplateCreateRequest(templateName = name, content = description))
//            .toRequestBody("application/json".toMediaTypeOrNull())
//
//        val request = Request.Builder()
//            .url(URLs.createScheduleTemplate())
//            .method("POST", body)
//            .build()
//
//        val type = object : TypeToken<NetworkResponse<String>>() {}.type
//        fetch<NetworkResponse<String>>(request, type)
//    }
//
//    override suspend fun fetchScheduleTemplate(deviceSN: String, templateID: String): ScheduleTemplateResponse {
//        val request = Request.Builder().url(URLs.getSchedule(deviceSN, templateID)).build()
//
//        val type = object : TypeToken<NetworkResponse<ScheduleTemplateResponse>>() {}.type
//        val response: NetworkTuple<NetworkResponse<ScheduleTemplateResponse>> = fetch(request, type)
//        return response.item.result ?: throw MissingDataException()
//    }
//
//    override suspend fun saveSchedule(deviceSN: String, schedule: Schedule) {
//        val body = Gson().toJson(ScheduleSaveRequest(schedule.phases.map { it.toPollcy() }, templateID = null, deviceSN = deviceSN))
//            .toRequestBody("application/json".toMediaTypeOrNull())
//
//        val request = Request.Builder()
//            .url(URLs.enableSchedule())
//            .method("POST", body)
//            .build()
//
//        val type = object : TypeToken<NetworkResponse<String>>() {}.type
//        fetch<NetworkResponse<String>>(request, type)
//    }
//
//    override suspend fun deleteScheduleTemplate(templateID: String) {
//        val request = Request.Builder().url(URLs.deleteScheduleTemplate(templateID)).build()
//
//        val type = object : TypeToken<NetworkResponse<String>>() {}.type
//        fetch<NetworkResponse<String>>(request, type)
//    }
//
//    override suspend fun enableScheduleTemplate(deviceSN: String, templateID: String) {
//        val body = Gson().toJson(ScheduleEnableRequest(templateID = templateID, deviceSN = deviceSN))
//            .toRequestBody("application/json".toMediaTypeOrNull())
//
//        val request = Request.Builder()
//            .url(URLs.enableSchedule())
//            .method("POST", body)
//            .build()
//
//        val type = object : TypeToken<NetworkResponse<String>>() {}.type
//        fetch<NetworkResponse<String>>(request, type)
//    }
//
//    override suspend fun deleteSchedule(deviceSN: String) {
//        val request = Request.Builder().url(URLs.getDeleteSchedule(deviceSN)).build()
//
//        val type = object : TypeToken<NetworkResponse<String>>() {}.type
//        fetch<NetworkResponse<String>>(request, type)
//    }
//
//    override suspend fun fetchSchedulerFlag(deviceSN: String): SchedulerFlagResponse {
//        val request = Request.Builder().url(URLs.getSchedulerFlag(deviceSN)).build()
//
//        val type = object : TypeToken<NetworkResponse<SchedulerFlagResponse>>() {}.type
//        val response: NetworkTuple<NetworkResponse<SchedulerFlagResponse>> = fetch(request, type)
//        return response.item.result ?: throw MissingDataException()
//    }
//
//    override suspend fun fetchScheduleModes(deviceID: String): List<SchedulerModeResponse> {
//        val request = Request.Builder().url(URLs.schedulerModes(deviceID)).build()
//
//        val type = object : TypeToken<NetworkResponse<SchedulerModesResponse>>() {}.type
//        val response: NetworkTuple<NetworkResponse<SchedulerModesResponse>> = fetch(request, type)
//        return response.item.result?.modes ?: throw MissingDataException()
//    }
//
//    override suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleListResponse {
//        val request = Request.Builder().url(URLs.getCurrentSchedule(deviceSN)).build()
//
//        val type = object : TypeToken<NetworkResponse<ScheduleListResponse>>() {}.type
//        val response: NetworkTuple<NetworkResponse<ScheduleListResponse>> = fetch(request, type)
//        return response.item.result ?: throw MissingDataException()
//    }

    override suspend fun openapi_fetchDeviceList(): List<DeviceDetailResponse> {
        val body = Gson().toJson(DeviceListRequest())
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.getOpenDeviceList())
            .build()

        val type = object : TypeToken<NetworkResponse<PagedDeviceListResponse>>() {}.type
        val deviceListResult: NetworkTuple<NetworkResponse<PagedDeviceListResponse>> = fetch(request, type)
        deviceListResult.item.result?.let { deviceSummaryList ->
            val devices = deviceSummaryList.data.map {
                openapi_getDeviceDetail(it.deviceSN)
            }

            store.deviceListResponseStream.value = NetworkOperation(
                description = "fetchDeviceList",
                value = deviceListResult.item,
                raw = deviceListResult.text,
                request
            )

            return devices
        } ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<String>): OpenQueryResponse {
        val body = Gson().toJson(OpenQueryRequest(deviceSN, variables))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.getOpenRealData())
            .build()

        val type = object : TypeToken<NetworkResponse<List<OpenQueryResponse>>>() {}.type
        val result: NetworkTuple<NetworkResponse<List<OpenQueryResponse>>> = fetch(request, type)

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
        val result: NetworkTuple<NetworkResponse<List<OpenReportResponse>>> = fetch(request, type)

        return result.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchBatterySOC(deviceSN: String): BatterySOCResponse {
        val request = Request.Builder().url(URLs.getOpenBatterySOC(deviceSN)).build()

        val type = object : TypeToken<NetworkResponse<BatterySOCResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<BatterySOCResponse>> = fetch(request, type)
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

    private suspend fun openapi_getDeviceDetail(deviceSN: String): DeviceDetailResponse {
        val request = Request.Builder().url(URLs.getOpenDeviceDetail(deviceSN)).build()

        val type = object : TypeToken<NetworkResponse<DeviceDetailResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<DeviceDetailResponse>> = fetch(request, type)
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

        val type = object : TypeToken<NetworkResponse<List<DataLoggerResponse>>>() {}.type
        val response: NetworkTuple<NetworkResponse<List<DataLoggerResponse>>> = fetch(request, type)
        store.dataLoggerListResponse.value = NetworkOperation(description = "DataLoggerResponse", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
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

                    try {
                        val text = response.body?.string()
                        val builder = GsonBuilder()
                            .registerTypeAdapter(OpenApiVariableArray::class.java, OpenApiVariableDeserializer())
                            .registerTypeAdapter(OpenReportResponse::class.java, OpenReportResponseDeserializer())
                            .registerTypeAdapter(DataLoggerStatus::class.java, DataLoggerStatusDeserializer())
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
            return Result.failure(UnknownNetworkException(item.errno, errorMessages[item.errno.toString()]))
        }

        return Result.success(item)
    }
}

data class NetworkTuple<T : NetworkResponseInterface>(
    val item: T,
    val text: String?
)
