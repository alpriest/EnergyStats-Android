package com.alpriest.energystats.services

import com.alpriest.energystats.models.AddressBookResponse
import com.alpriest.energystats.models.BatteryResponse
import com.alpriest.energystats.models.BatterySettingsResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DataLoggerListRequest
import com.alpriest.energystats.models.DeviceDetailResponse
import com.alpriest.energystats.models.DeviceListRequest
import com.alpriest.energystats.models.DeviceSettingsGetResponse
import com.alpriest.energystats.models.DeviceSettingsSetRequest
import com.alpriest.energystats.models.DeviceSettingsValues
import com.alpriest.energystats.models.EarningsResponse
import com.alpriest.energystats.models.ErrorMessagesResponse
import com.alpriest.energystats.models.OpenApiVariable
import com.alpriest.energystats.models.OpenApiVariableArray
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.PagedDataLoggerListResponse
import com.alpriest.energystats.models.PagedDeviceListResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportRequest
import com.alpriest.energystats.models.ReportResponse
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ScheduleEnableRequest
import com.alpriest.energystats.models.ScheduleListResponse
import com.alpriest.energystats.models.ScheduleSaveRequest
import com.alpriest.energystats.models.ScheduleTemplateCreateRequest
import com.alpriest.energystats.models.ScheduleTemplateListResponse
import com.alpriest.energystats.models.ScheduleTemplateResponse
import com.alpriest.energystats.models.SchedulerFlagResponse
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.models.SchedulerModesResponse
import com.alpriest.energystats.models.SetBatteryTimesRequest
import com.alpriest.energystats.models.SetSOCRequest
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.models.md5
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.statsgraph.ReportType
import com.google.gson.Gson
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
                    .header("User-Agent", "EnergyStats")
                    .header("Accept", "application/json, text/plain, */*")
                    .header(
                        "Referrer",
                        "https://www.foxesscloud.com/"
                    )
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

    override suspend fun saveScheduleTemplate(deviceSN: String, scheduleTemplate: ScheduleTemplate) {
        val body = Gson().toJson(
            ScheduleSaveRequest(
                pollcy = scheduleTemplate.phases.map { it.toPollcy() },
                templateID = scheduleTemplate.id,
                deviceSN
            )
        ).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(URLs.saveScheduleTemplate())
            .method("POST", body)
            .build()

        val type = object : TypeToken<NetworkResponse<String>>() {}.type
        fetch<NetworkResponse<String>>(request, type)
    }

    override suspend fun fetchScheduleTemplates(): ScheduleTemplateListResponse {
        val request = Request.Builder().url(URLs.fetchScheduleTemplates()).build()

        val type = object : TypeToken<NetworkResponse<ScheduleTemplateListResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<ScheduleTemplateListResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun createScheduleTemplate(name: String, description: String) {
        val body = Gson().toJson(ScheduleTemplateCreateRequest(templateName = name, content = description))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(URLs.createScheduleTemplate())
            .method("POST", body)
            .build()

        val type = object : TypeToken<NetworkResponse<String>>() {}.type
        fetch<NetworkResponse<String>>(request, type)
    }

    override suspend fun fetchScheduleTemplate(deviceSN: String, templateID: String): ScheduleTemplateResponse {
        val request = Request.Builder().url(URLs.getSchedule(deviceSN, templateID)).build()

        val type = object : TypeToken<NetworkResponse<ScheduleTemplateResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<ScheduleTemplateResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun saveSchedule(deviceSN: String, schedule: Schedule) {
        val body = Gson().toJson(ScheduleSaveRequest(schedule.phases.map { it.toPollcy() }, templateID = null, deviceSN = deviceSN))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(URLs.enableSchedule())
            .method("POST", body)
            .build()

        val type = object : TypeToken<NetworkResponse<String>>() {}.type
        fetch<NetworkResponse<String>>(request, type)
    }

    override suspend fun deleteScheduleTemplate(templateID: String) {
        val request = Request.Builder().url(URLs.deleteScheduleTemplate(templateID)).build()

        val type = object : TypeToken<NetworkResponse<String>>() {}.type
        fetch<NetworkResponse<String>>(request, type)
    }

    override suspend fun enableScheduleTemplate(deviceSN: String, templateID: String) {
        val body = Gson().toJson(ScheduleEnableRequest(templateID = templateID, deviceSN = deviceSN))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(URLs.enableSchedule())
            .method("POST", body)
            .build()

        val type = object : TypeToken<NetworkResponse<String>>() {}.type
        fetch<NetworkResponse<String>>(request, type)
    }

    override suspend fun deleteSchedule(deviceSN: String) {
        val request = Request.Builder().url(URLs.getDeleteSchedule(deviceSN)).build()

        val type = object : TypeToken<NetworkResponse<String>>() {}.type
        fetch<NetworkResponse<String>>(request, type)
    }

    override suspend fun fetchSchedulerFlag(deviceSN: String): SchedulerFlagResponse {
        val request = Request.Builder().url(URLs.getSchedulerFlag(deviceSN)).build()

        val type = object : TypeToken<NetworkResponse<SchedulerFlagResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<SchedulerFlagResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun fetchScheduleModes(deviceID: String): List<SchedulerModeResponse> {
        val request = Request.Builder().url(URLs.schedulerModes(deviceID)).build()

        val type = object : TypeToken<NetworkResponse<SchedulerModesResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<SchedulerModesResponse>> = fetch(request, type)
        return response.item.result?.modes ?: throw MissingDataException()
    }

    override suspend fun fetchCurrentSchedule(deviceSN: String): ScheduleListResponse {
        val request = Request.Builder().url(URLs.getCurrentSchedule(deviceSN)).build()

        val type = object : TypeToken<NetworkResponse<ScheduleListResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<ScheduleListResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchDeviceList(): List<DeviceDetailResponse> {
        val body = Gson().toJson(DeviceListRequest())
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.deviceList())
            .build()

        val type = object : TypeToken<NetworkResponse<PagedDeviceListResponse>>() {}.type
        val deviceListResult: NetworkTuple<NetworkResponse<PagedDeviceListResponse>> = fetch(request, type)
        deviceListResult.item.result?.let { deviceSummaryList ->
            val devices = deviceSummaryList.data.map {
                openapi_fetchDevice(it.deviceSN)
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

    override suspend fun openapi_fetchRealData(deviceSN: String, variables: List<Variable>): OpenQueryResponse {
        TODO("Not yet implemented")
    }

    override suspend fun openapi_fetchHistory(deviceSN: String, variables: List<String>, start: Long, end: Long): OpenHistoryResponse {
        TODO("Not yet implemented")
    }

    override suspend fun openapi_fetchReport(deviceSN: String, variables: List<ReportVariable>, queryDate: QueryDate, reportType: ReportType): List<OpenReportResponse> {
        TODO("Not yet implemented")
    }

    suspend fun openapi_fetchDevice(deviceSN: String): DeviceDetailResponse {
        val request = Request.Builder().url(URLs.deviceDetail(deviceSN)).build()

        val type = object : TypeToken<NetworkResponse<DeviceDetailResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<DeviceDetailResponse>> = fetch(request, type)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse {
        val request = Request.Builder().url(URLs.socGet(deviceSN)).build()

        val type = object : TypeToken<NetworkResponse<BatterySettingsResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<BatterySettingsResponse>> = fetch(request, type)
        store.batterySettingsResponseStream.value = NetworkOperation(description = "fetchBatterySettings", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
    }

//    override suspend fun fetchReport(
//        deviceID: String,
//        variables: List<ReportVariable>,
//        queryDate: QueryDate,
//        reportType: ReportType
//    ): ArrayList<ReportResponse> {
//        val body = Gson().toJson(ReportRequest(deviceID, variables, queryDate, reportType))
//            .toRequestBody("application/json".toMediaTypeOrNull())
//
//        val request = Request.Builder().post(body).url(URLs.report()).build()
//
//        val type = object : TypeToken<NetworkReportResponse>() {}.type
//        val response: NetworkTuple<NetworkReportResponse> = fetch(request, type)
//        store.reportResponseStream.value = NetworkOperation(description = "fetchReport", value = response.item, raw = response.text, request)
//        return response.item.result ?: throw MissingDataException()
//    }

    override suspend fun fetchAddressBook(deviceID: String): AddressBookResponse {
        val request = Request.Builder().url(URLs.addressBook(deviceID)).build()

        val type = object : TypeToken<NetworkResponse<AddressBookResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<AddressBookResponse>> = fetch(request, type)
        store.addressBookResponseStream.value = NetworkOperation(description = "fetchAddressBook", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
    }

//    override suspend fun fetchRaw(deviceID: String, variables: List<RawVariable>, queryDate: QueryDate): ArrayList<RawResponse> {
//        val body = Gson().toJson(RawRequest(deviceID, variables, queryDate))
//            .toRequestBody("application/json".toMediaTypeOrNull())
//
//        val request = Request.Builder().post(body).url(URLs.raw()).build()
//
//        val type = object : TypeToken<NetworkRawResponse>() {}.type
//        val response: NetworkTuple<NetworkRawResponse> = fetch(request, type)
//        store.rawResponseStream.value = NetworkOperation(description = "fetchRaw", value = response.item, raw = response.text, request)
//        return response.item.result ?: throw MissingDataException()
//    }

    override suspend fun fetchBattery(deviceID: String): BatteryResponse {
        val request = Request.Builder().url(URLs.battery(deviceID)).build()

        val type = object : TypeToken<NetworkResponse<BatteryResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<BatteryResponse>> = fetch(request, type)
        store.batteryResponseStream.value = NetworkOperation(description = "fetchBattery", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun fetchEarnings(deviceID: String): EarningsResponse {
        val request = Request.Builder().url(URLs.earnings(deviceID)).build()

        val type = object : TypeToken<NetworkResponse<EarningsResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<EarningsResponse>> = fetch(request, type)
        store.earningsResponseStream.value = NetworkOperation(description = "fetchEarnings", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun openapi_fetchVariables(): List<OpenApiVariable> {
        val request = Request.Builder().url(URLs.variables()).build()

        val type = object : TypeToken<NetworkResponse<OpenApiVariableArray>>() {}.type
        val response: NetworkTuple<NetworkResponse<OpenApiVariableArray>> = fetch(request, type)
        store.variablesResponseStream.value = NetworkOperation(description = "fetchVariables", value = response.item, raw = response.text, request)
        return response.item.result?.array ?: throw MissingDataException()
    }

    override suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String) {
        val body = Gson().toJson(SetSOCRequest(minGridSoc = minGridSOC, minSoc = minSOC, sn = deviceSN))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder().url(URLs.socSet()).post(body).build()

        val type = object : TypeToken<NetworkResponse<String>>() {}.type
        fetch<NetworkResponse<String>>(request, type)
    }

    override suspend fun fetchBatteryTimes(deviceSN: String): BatteryTimesResponse {
        val request = Request.Builder().url(URLs.batteryTimes(deviceSN)).build()

        val type = object : TypeToken<NetworkResponse<BatteryTimesResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<BatteryTimesResponse>> = fetch(request, type)
        store.batteryTimesResponseStream.value = NetworkOperation(description = "batteryTimesResponse", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        val body = Gson().toJson(SetBatteryTimesRequest(sn = deviceSN, times = times))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder().url(URLs.batteryTimeSet()).post(body).build()

        val type = object : TypeToken<NetworkResponse<String>>() {}.type
        fetch<NetworkResponse<String>>(request, type)
    }

    override suspend fun fetchWorkMode(deviceID: String): DeviceSettingsGetResponse {
        val request = Request.Builder().url(URLs.deviceSettings(deviceID)).build()

        val type = object : TypeToken<NetworkResponse<DeviceSettingsGetResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<DeviceSettingsGetResponse>> = fetch(request, type)
        store.deviceSettingsGetResponse.value = NetworkOperation(description = "deviceSettingsGetResponse", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun setWorkMode(deviceID: String, workMode: String) {
        val body = Gson().toJson(DeviceSettingsSetRequest(id = deviceID, key = "operation_mode__work_mode", values = DeviceSettingsValues(workMode)))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(URLs.deviceSettingsSet())
            .post(body)
            .build()

        val type = object : TypeToken<NetworkResponse<String>>() {}.type
        fetch<NetworkResponse<String>>(request, type)
    }

    override suspend fun fetchDataLoggers(): PagedDataLoggerListResponse {
        val body = Gson().toJson(DataLoggerListRequest())
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder().url(URLs.moduleList()).post(body).build()

        val type = object : TypeToken<NetworkResponse<PagedDataLoggerListResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<PagedDataLoggerListResponse>> = fetch(request, type)
        store.dataLoggerListResponse.value = NetworkOperation(description = "DataLoggerListResponse", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
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
                        val body: T = Gson().fromJson(text, type)
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
