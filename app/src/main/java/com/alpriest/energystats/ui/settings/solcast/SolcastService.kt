package com.alpriest.energystats.ui.settings.solcast

import com.alpriest.energystats.BuildConfig
import com.alpriest.energystats.shared.models.SolcastSite
import com.alpriest.energystats.shared.models.network.SolcastForecastResponseList
import com.alpriest.energystats.shared.models.network.SolcastSiteResponseList
import com.alpriest.energystats.shared.network.InvalidConfigurationException
import com.alpriest.energystats.shared.network.TryLaterException
import com.alpriest.energystats.shared.config.ConfigManaging
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Solcast(private val configManager: ConfigManaging) : SolarForecasting {
    override suspend fun fetchForecast(site: SolcastSite, apiKey: String): SolcastForecastResponseList {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("api.solcast.com.au")
            .addPathSegment("rooftop_sites")
            .addPathSegment(site.resourceId)
            .addPathSegment("forecasts")
            .addQueryParameter(name = "format", value = "json")
            .addQueryParameter(name = "API_KEY", value = apiKey)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val type = object : TypeToken<SolcastForecastResponseList>() {}.type
        configManager.lastSolcastRefresh = LocalDateTime.now()
        return fetch(request, type)
    }

    override suspend fun fetchSites(apiKey: String): SolcastSiteResponseList {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("api.solcast.com.au")
            .addPathSegments("rooftop_sites")
            .addQueryParameter(name = "format", value = "json")
            .addQueryParameter(name = "API_KEY", value = apiKey)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val type = object : TypeToken<SolcastSiteResponseList>() {}.type

        return fetch(request, type)
    }

    private suspend fun <T> fetch(
        request: Request,
        type: Type
    ): T {
        return suspendCoroutine { continuation ->
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val text = response.body?.string()
                        when (response.code) {
                            404 -> {
                                val errorApiResponseType = object : TypeToken<ErrorApiResponse>() {}.type
                                val gson = GsonBuilder()
                                    .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
                                    .create()
                                val body: ErrorApiResponse = gson.fromJson(text, errorApiResponseType)

                                continuation.resumeWithException(InvalidConfigurationException(body.responseStatus.message))
                            }

                            429 -> {
                                continuation.resumeWithException(TryLaterException())
                            }

                            else -> {
                                val body: T = Gson().fromJson(text, type)
                                continuation.resume(body)
                            }
                        }
                    } catch (ex: Exception) {
                        continuation.resumeWithException(ex)
                    }
                }
            })
        }
    }

    private val okHttpClient by lazy {
        val builder = OkHttpClient()
            .newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", UserAgent.description())
                    .build()
                chain.proceed(request)
            }

        builder.build()
    }
}

private data class ErrorApiResponse(
    @SerializedName("response_status")
    val responseStatus: ResponseStatus
)

private data class ResponseStatus(val message: String)

class UserAgent {
    companion object {
        fun description(): String {
            val buildVersion = BuildConfig.VERSION_NAME

            return "Energy-Stats/Android/$buildVersion"
        }
    }
}