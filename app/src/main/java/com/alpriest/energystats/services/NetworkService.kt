package com.alpriest.energystats.services

import com.alpriest.energystats.models.*
import com.alpriest.energystats.stores.CredentialStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface NetworkResponseInterface {
    val errno: Int
}

class NetworkService(private val credentials: CredentialStore, private val config: ConfigInterface) : Networking {
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
            MediaType.parse("application/json"),
            Gson().toJson(DeviceListRequest())
        )

        val request = Request.Builder()
            .post(body)
            .url("https://www.foxesscloud.com/c/v0/device/list")
            .build()

        val type = object : TypeToken<NetworkResponse<PagedDeviceListResponse>>() {}.type
        val response: NetworkResponse<PagedDeviceListResponse> = fetch(request, type)
        return response.result ?: throw MissingDataException()
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
        val response: NetworkResponse<BatterySettingsResponse> = fetch(request, type)
        return response.result ?: throw MissingDataException()
    }

    override suspend fun fetchReport(
        deviceID: String,
        variables: Array<ReportVariable>,
        queryDate: QueryDate
    ): ArrayList<ReportResponse> {
        val body = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(ReportRequest(deviceID, variables, queryDate))
        )

        val request = Request.Builder()
            .post(body)
            .url("https://www.foxesscloud.com/c/v0/device/history/report")
            .build()

        val type = object : TypeToken<NetworkReportResponse>() {}.type
        val response: NetworkReportResponse = fetch(request, type)
        return response.result ?: throw MissingDataException()
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
        val response: NetworkResponse<AddressBookResponse> = fetch(request, type)
        return response.result ?: throw MissingDataException()
    }

    override suspend fun fetchRaw(deviceID: String, variables: Array<String>): ArrayList<RawResponse> {
        val body = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(RawRequest(deviceID, variables))
        )

        val request = Request.Builder()
            .post(body)
            .url("https://www.foxesscloud.com/c/v0/device/history/raw")
            .build()

        val type = object : TypeToken<NetworkRawResponse>() {}.type
        val response: NetworkRawResponse = fetch(request, type)
        return response.result ?: throw MissingDataException()
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
        val response: NetworkResponse<BatteryResponse> = fetch(request, type)
        return response.result ?: throw MissingDataException()
    }

    override suspend fun fetchVariables(deviceID: String): List<RawVariable> {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.foxesscloud.com")
            .addPathSegments("c/v0/device/variables")
            .addQueryParameter("deviceID", deviceID)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val type = object : TypeToken<NetworkResponse<VariablesResponse>>() {}.type
        val response: NetworkResponse<VariablesResponse> = fetch(request, type)
        return response.result?.variables ?: throw MissingDataException()
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
            MediaType.parse("application/json"),
            Gson().toJson(AuthRequest(user = usernameToUse, password = hashedPasswordToUse))
        )

        val request = Request.Builder()
            .url("https://www.foxesscloud.com/c/v0/user/login")
            .post(body)
            .build()

        val type = object : TypeToken<NetworkResponse<AuthResponse>>() {}.type
        val response: NetworkResponse<AuthResponse> = fetch(request, type)
        return response.result?.token ?: throw MissingDataException()
    }

    private suspend fun <T : NetworkResponseInterface> fetch(
        request: Request,
        type: Type,
        retry: Boolean = true
    ): T {
        try {
            return suspendCoroutine { continuation ->
                okHttpClient.newCall(request).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        continuation.resumeWithException(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            val text = response.body()?.string()
                            val body: T = Gson().fromJson(text, type)
                            val result: Result<T> = check(body)

                            result.fold(
                                onSuccess = { continuation.resume(it) },
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
