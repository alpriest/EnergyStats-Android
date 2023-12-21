package com.alpriest.energystats.services

import com.alpriest.energystats.models.AddressBookResponse
import com.alpriest.energystats.models.AuthRequest
import com.alpriest.energystats.models.AuthResponse
import com.alpriest.energystats.models.BatteryResponse
import com.alpriest.energystats.models.BatterySettingsResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DataLoggerListRequest
import com.alpriest.energystats.models.DeviceListRequest
import com.alpriest.energystats.models.DeviceSettingsGetResponse
import com.alpriest.energystats.models.DeviceSettingsSetRequest
import com.alpriest.energystats.models.DeviceSettingsValues
import com.alpriest.energystats.models.EarningsResponse
import com.alpriest.energystats.models.ErrorMessagesResponse
import com.alpriest.energystats.models.PagedDataLoggerListResponse
import com.alpriest.energystats.models.PagedDeviceListResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawRequest
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.models.RawVariable
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
import com.alpriest.energystats.models.VariablesResponse
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.statsgraph.ReportType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
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

                val requestBuilder = original.newBuilder()
                    .header("token", credentials.getToken() ?: "")
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

                chain.proceed(requestBuilder.build())
            }

        interceptor?.let {
            builder.addInterceptor(it)
        }

        builder.build()
    }

    private var token: String?
        get() {
            return credentials.getToken()
        }
        set(value) {
            credentials.setToken(value)
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

    override suspend fun fetchDeviceList(): PagedDeviceListResponse {
        val body = Gson().toJson(DeviceListRequest())
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url(URLs.deviceList())
            .build()

        val type = object : TypeToken<NetworkResponse<PagedDeviceListResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<PagedDeviceListResponse>> = fetch(request, type)
        store.deviceListResponseStream.value = NetworkOperation(description = "fetchDeviceList", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun verifyCredentials(username: String, password: String) {
        token = fetchLoginToken(username, password)
    }

    override suspend fun ensureHasToken() {
        try {
            if (token == null) {
                token = fetchLoginToken()
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    override suspend fun fetchBatterySettings(deviceSN: String): BatterySettingsResponse {
        val request = Request.Builder().url(URLs.socGet(deviceSN)).build()

        val type = object : TypeToken<NetworkResponse<BatterySettingsResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<BatterySettingsResponse>> = fetch(request, type)
        store.batterySettingsResponseStream.value = NetworkOperation(description = "fetchBatterySettings", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun fetchReport(
        deviceID: String,
        variables: List<ReportVariable>,
        queryDate: QueryDate,
        reportType: ReportType
    ): ArrayList<ReportResponse> {
        val body = Gson().toJson(ReportRequest(deviceID, variables, queryDate, reportType))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder().post(body).url(URLs.report()).build()

        val type = object : TypeToken<NetworkReportResponse>() {}.type
        val response: NetworkTuple<NetworkReportResponse> = fetch(request, type)
        store.reportResponseStream.value = NetworkOperation(description = "fetchReport", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun fetchAddressBook(deviceID: String): AddressBookResponse {
        val request = Request.Builder().url(URLs.addressBook(deviceID)).build()

        val type = object : TypeToken<NetworkResponse<AddressBookResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<AddressBookResponse>> = fetch(request, type)
        store.addressBookResponseStream.value = NetworkOperation(description = "fetchAddressBook", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun fetchRaw(deviceID: String, variables: List<RawVariable>, queryDate: QueryDate): ArrayList<RawResponse> {
        val body = Gson().toJson(RawRequest(deviceID, variables, queryDate))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder().post(body).url(URLs.raw()).build()

        val type = object : TypeToken<NetworkRawResponse>() {}.type
        val response: NetworkTuple<NetworkRawResponse> = fetch(request, type)
        store.rawResponseStream.value = NetworkOperation(description = "fetchRaw", value = response.item, raw = response.text, request)
        return response.item.result ?: throw MissingDataException()
    }

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

    override suspend fun fetchVariables(deviceID: String): List<RawVariable> {
        val request = Request.Builder().url(URLs.variables(deviceID)).build()

        val type = object : TypeToken<NetworkResponse<VariablesResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<VariablesResponse>> = fetch(request, type)
        store.variablesResponseStream.value = NetworkOperation(description = "fetchVariables", value = response.item, raw = response.text, request)
        return response.item.result?.variables ?: throw MissingDataException()
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

    private suspend fun fetchLoginToken(
        username: String? = null,
        hashedPassword: String? = null
    ): String {
        val usernameToUse: String =
            (username ?: credentials.getUsername()) ?: throw BadCredentialsException()
        val hashedPasswordToUse: String =
            (hashedPassword ?: credentials.getHashedPassword())
                ?: throw BadCredentialsException()

        val body = Gson().toJson(AuthRequest(user = usernameToUse, password = hashedPasswordToUse))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder().url(URLs.login()).post(body).build()

        val type = object : TypeToken<NetworkResponse<AuthResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<AuthResponse>> = fetch(request, type)
        return response.item.result?.token ?: throw MissingDataException()
    }

    private suspend fun <T : NetworkResponseInterface> fetch(
        request: Request,
        type: Type,
        retry: Boolean = true
    ): NetworkTuple<T> {
        try {
            return suspendCoroutine { continuation ->
                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        continuation.resumeWithException(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
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
        } catch (ex: InvalidTokenException) {
            if (retry) {
                token = null
                token = fetchLoginToken()
                return fetch(request, type, retry = false)
            } else {
                throw ex
            }
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
