package com.alpriest.energystats.ui.summary

import com.alpriest.energystats.shared.models.network.SolcastForecastList
import com.alpriest.energystats.shared.models.network.SolcastForecastResponse
import com.alpriest.energystats.shared.models.network.SolcastSiteResponse
import com.alpriest.energystats.shared.models.network.SolcastSiteResponseList
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.shared.models.SolcastSite
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.collections.map

class DemoSolarForecasting : SolcastCaching {
    override suspend fun fetchSites(apiKey: String): SolcastSiteResponseList {
        return SolcastSiteResponseList(
            listOf(
                SolcastSiteResponse(
                    name = "Front",
                    resourceId = "abc-123",
                    capacity = 3.7,
                    longitude = -0.2664026,
                    latitude = 51.5287398,
                    azimuth = 134,
                    tilt = 45.0,
                    lossFactor = 0.9,
                    dcCapacity = 5.6,
                    installDate = ""
                ),
                SolcastSiteResponse(
                    name = "Back",
                    resourceId = "def-123",
                    capacity = 3.7,
                    longitude = -0.2664026,
                    latitude = 51.5287398,
                    azimuth = 134,
                    tilt = 45.0,
                    lossFactor = 0.9,
                    dcCapacity = 5.6,
                    installDate = ""
                )
            )
        )
    }

    override suspend fun fetchForecast(site: SolcastSite, apiKey: String, ignoreCache: Boolean): SolcastForecastList {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val now = LocalDate.now()

        val forecasts: List<SolcastForecastResponse> = listOf(
            SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T06:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T06:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T07:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T07:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0084, pvEstimate10 = 0.0056, pvEstimate90 = 0.0167, dateFormat.parse("2023-11-14T08:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0501, pvEstimate10 = 0.0223, pvEstimate90 = 0.0891, dateFormat.parse("2023-11-14T08:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0975, pvEstimate10 = 0.0418, pvEstimate90 = 0.1811, dateFormat.parse("2023-11-14T09:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.1635, pvEstimate10 = 0.0771, pvEstimate90 = 0.4012, dateFormat.parse("2023-11-14T09:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.3364, pvEstimate10 = 0.1377, pvEstimate90 = 0.746, dateFormat.parse("2023-11-14T10:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.4891, pvEstimate10 = 0.2125, pvEstimate90 = 1.1081, dateFormat.parse("2023-11-14T10:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.609, pvEstimate10 = 0.2531, pvEstimate90 = 1.505, dateFormat.parse("2023-11-14T11:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.7061, pvEstimate10 = 0.2835, pvEstimate90 = 1.8413, dateFormat.parse("2023-11-14T11:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.7667, pvEstimate10 = 0.2936, pvEstimate90 = 2.09, dateFormat.parse("2023-11-14T12:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.8404, pvEstimate10 = 0.3037, pvEstimate90 = 2.3005, dateFormat.parse("2023-11-14T12:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.9307, pvEstimate10 = 0.3138, pvEstimate90 = 2.5050, dateFormat.parse("2023-11-14T13:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.9832, pvEstimate10 = 0.3087, pvEstimate90 = 2.5392, dateFormat.parse("2023-11-14T13:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.9438, pvEstimate10 = 0.2733, pvEstimate90 = 2.5179, dateFormat.parse("2023-11-14T14:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.8035, pvEstimate10 = 0.1973, pvEstimate90 = 2.8682, dateFormat.parse("2023-11-14T14:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.5897, pvEstimate10 = 0.128, pvEstimate90 = 2.5599, dateFormat.parse("2023-11-14T15:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.1594, pvEstimate10 = 0.0716, pvEstimate90 = 1.6839, dateFormat.parse("2023-11-14T15:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0496, pvEstimate10 = 0.0248, pvEstimate90 = 0.6277, dateFormat.parse("2023-11-14T16:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0028, pvEstimate10 = 0.0028, pvEstimate90 = 0.0055, dateFormat.parse("2023-11-14T16:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T17:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T17:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T18:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T18:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T19:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T19:30:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T20:00:00Z")!!, "PT30M"),
            SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T20:30:00Z")!!, "PT30M")
        )

        val today = forecasts.map {
            val localDateTime = it.periodEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .withYear(now.year)
                .withMonth(now.monthValue)
                .withDayOfMonth(now.dayOfMonth)

            it.copy(periodEnd = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))
        }

        val tomorrow = forecasts.map {
            val localDateTime = it.periodEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .withYear(now.year)
                .withMonth(now.monthValue)
                .withDayOfMonth(now.dayOfMonth)
                .plusDays(1)

            it.copy(periodEnd = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))
        }

        return SolcastForecastList(null, today + tomorrow)
    }
}