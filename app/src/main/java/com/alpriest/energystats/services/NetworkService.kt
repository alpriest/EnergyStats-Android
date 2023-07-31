package com.alpriest.energystats.services

import com.alpriest.energystats.models.AddressBookResponse
import com.alpriest.energystats.models.AuthRequest
import com.alpriest.energystats.models.AuthResponse
import com.alpriest.energystats.models.BatteryResponse
import com.alpriest.energystats.models.BatterySettingsResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.ChargeTime
import com.alpriest.energystats.models.DeviceListRequest
import com.alpriest.energystats.models.EarningsResponse
import com.alpriest.energystats.models.PagedDeviceListResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawRequest
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.models.ReportRequest
import com.alpriest.energystats.models.ReportResponse
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.SetBatteryTimesRequest
import com.alpriest.energystats.models.SetSOCRequest
import com.alpriest.energystats.models.VariablesResponse
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.settings.battery.ChargeTimePeriod
import com.alpriest.energystats.ui.statsgraph.ReportType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface NetworkResponseInterface {
    val errno: Int
}

class NetworkService(private val credentials: CredentialStore, private val store: InMemoryLoggingNetworkStore) : Networking {
    private val okHttpClient by lazy {
        val userAgents = arrayOf(
            "Mozilla/5.0 (Linux; Android 12; SM-S906N Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/80.0.3987.119 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 10; SM-G996U Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Mobile Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (Linux; Android 7.0; SM-T827R4 Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.116 Safari/537.36",
            "Mozilla/5.0 (Linux; Android 5.1; AFTS Build/LMY47O) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/41.99900.2250.0242 Safari/537.36",
            "AppleTV11,1/11.1",
            "Mozilla/5.0 (iPhone14,3; U; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) Version/10.0 Mobile/19A346 Safari/602.1",
            "Mozilla/5.0 (iPhone13,2; U; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) Version/10.0 Mobile/15E148 Safari/602.1"
        )

        OkHttpClient()
            .newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()

                val requestBuilder = original.newBuilder()
                    .header("token", credentials.getToken() ?: "")
                    .header("User-Agent", userAgents.random())
                    .header("Accept", "application/json, text/plain, */*")
                    .header(
                        "Referrer",
                        "https://www.foxesscloud.com/bus/device/inverterDetail?id=xyz&flowType=1&status=1&hasPV=true&hasBattery=true"
                    )
                    .header("Accept-Language", "en-US;q=0.9,en;q=0.8,de;q=0.7,nl;q=0.6")
                    .header("Content-Type", "application/json")

                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    private var token: String?
        get() {
            return credentials.getToken()
        }
        set(value) {
            credentials.setToken(value)
        }

    override suspend fun fetchDeviceList(): PagedDeviceListResponse {
        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            Gson().toJson(DeviceListRequest())
        )

        val request = Request.Builder()
            .post(body)
            .url("https://www.foxesscloud.com/c/v0/device/list")
            .build()

        val type = object : TypeToken<NetworkResponse<PagedDeviceListResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<PagedDeviceListResponse>> = fetch(request, type)
        store.deviceListResponseStream.value = NetworkOperation(description = "fetchDeviceList", value = response.item, raw = response.text)
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
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.foxesscloud.com")
            .addPathSegments("c/v0/device/battery/soc/get")
            .addQueryParameter("sn", deviceSN)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val type = object : TypeToken<NetworkResponse<BatterySettingsResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<BatterySettingsResponse>> = fetch(request, type)
        store.batterySettingsResponseStream.value = NetworkOperation(description = "fetchBatterySettings", value = response.item, raw = response.text)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun fetchReport(
        deviceID: String,
        variables: List<ReportVariable>,
        queryDate: QueryDate,
        reportType: ReportType
    ): ArrayList<ReportResponse> {
        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            Gson().toJson(ReportRequest(deviceID, variables, queryDate, reportType))
        )

        val request = Request.Builder()
            .post(body)
            .url("https://www.foxesscloud.com/c/v0/device/history/report")
            .build()

        val type = object : TypeToken<NetworkReportResponse>() {}.type
        val response: NetworkTuple<NetworkReportResponse> = fetch(request, type)
        store.reportResponseStream.value = NetworkOperation(description = "fetchReport", value = response.item, raw = response.text)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun fetchAddressBook(deviceID: String): AddressBookResponse {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.foxesscloud.com")
            .addPathSegments("c/v0/device/addressbook")
            .addQueryParameter("deviceID", deviceID)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val type = object : TypeToken<NetworkResponse<AddressBookResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<AddressBookResponse>> = fetch(request, type)
        store.addressBookResponseStream.value = NetworkOperation(description = "fetchAddressBook", value = response.item, raw = response.text)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun fetchRaw(deviceID: String, variables: List<RawVariable>, queryDate: QueryDate): ArrayList<RawResponse> {
        val body = Gson().toJson(RawRequest(deviceID, variables, queryDate))
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .post(body)
            .url("https://www.foxesscloud.com/c/v0/device/history/raw")
            .build()

        val type = object : TypeToken<NetworkRawResponse>() {}.type
        val response: NetworkTuple<NetworkRawResponse> = fetch(request, type)
        store.rawResponseStream.value = NetworkOperation(description = "fetchRaw", value = response.item, raw = response.text)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun fetchBattery(deviceID: String): BatteryResponse {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.foxesscloud.com")
            .addPathSegments("c/v0/device/battery/info")
            .addQueryParameter("id", deviceID)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val type = object : TypeToken<NetworkResponse<BatteryResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<BatteryResponse>> = fetch(request, type)
        store.batteryResponseStream.value = NetworkOperation(description = "fetchBattery", value = response.item, raw = response.text)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun fetchEarnings(deviceID: String): EarningsResponse {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.foxesscloud.com")
            .addPathSegments("c/v0/device/earnings")
            .addQueryParameter("deviceID", deviceID)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val type = object : TypeToken<NetworkResponse<EarningsResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<EarningsResponse>> = fetch(request, type)
        store.earningsResponseStream.value = NetworkOperation(description = "fetchEarnings", value = response.item, raw = response.text)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun fetchVariables(deviceID: String): List<RawVariable> {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.foxesscloud.com")
            .addPathSegments("c/v1/device/variables")
            .addQueryParameter("deviceID", deviceID)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val type = object : TypeToken<NetworkResponse<VariablesResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<VariablesResponse>> = fetch(request, type)
        store.variablesResponseStream.value = NetworkOperation(description = "fetchVariables", value = response.item, raw = response.text)
        return response.item.result?.variables ?: throw MissingDataException()
    }

    override suspend fun setSoc(minGridSOC: Int, minSOC: Int, deviceSN: String) {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.foxesscloud.com")
            .addPathSegments("c/v0/device/battery/soc/set")
            .build()

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            Gson().toJson(SetSOCRequest(minGridSoc = minGridSOC, minSoc = minSOC, sn = deviceSN))
        )

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val type = object : TypeToken<NetworkResponse<String>>() {}.type
        fetch<NetworkResponse<String>>(request, type)
    }

    override suspend fun fetchBatteryTimes(deviceSN: String): BatteryTimesResponse {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.foxesscloud.com")
            .addPathSegments("c/v0/device/battery/time/get")
            .addQueryParameter("deviceSN", deviceSN)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val type = object : TypeToken<NetworkResponse<BatteryTimesResponse>>() {}.type
        val response: NetworkTuple<NetworkResponse<BatteryTimesResponse>> = fetch(request, type)
        store.batteryTimesResponseStream.value = NetworkOperation(description = "batteryTimesResponse", value = response.item, raw = response.text)
        return response.item.result ?: throw MissingDataException()
    }

    override suspend fun setBatteryTimes(deviceSN: String, times: List<ChargeTime>) {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.foxesscloud.com")
            .addPathSegments("c/v0/device/battery/time/set")
            .build()

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            Gson().toJson(SetBatteryTimesRequest(sn = deviceSN, times = times))
        )

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val type = object : TypeToken<NetworkResponse<String>>() {}.type
        fetch<NetworkResponse<String>>(request, type)
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

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            Gson().toJson(AuthRequest(user = usernameToUse, password = hashedPasswordToUse))
        )

        val request = Request.Builder()
            .url("https://www.foxesscloud.com/c/v0/user/login")
            .post(body)
            .build()

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

        return Result.success(item)
    }
}

data class NetworkTuple<T : NetworkResponseInterface>(
    val item: T,
    val text: String?
)