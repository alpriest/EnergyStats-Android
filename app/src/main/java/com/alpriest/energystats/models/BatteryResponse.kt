package com.alpriest.energystats.models

data class BatteryResponse(
    val power: Double,
    val soc: Int,
    val residual: Double,
    val temperature: Double
)

data class BatterySettingsResponse(
    val minGridSoc: Int,
    val minSoc: Int
)

data class EarningsResponse(
    val today: Earning,
    val currency: String,
    val month: Earning,
    val year: Earning,
    val cumulate: Earning
) {
    fun currencySymbol(): String {
        if (currency.startsWith("GBP")) {
            return "£"
        } else if (currency.startsWith("EUR")) {
            return "€"
        }

        return currency
    }

    fun currencyCode(): String {
        return currency.take(3)
    }
}

data class Earning(
    val generation: Double,
    val earnings: Double
)

data class BatteryTimesResponse(
    val sn: String,
    val times: List<ChargeTime>
)

data class ChargeTime(
    val enableCharge: Boolean = true,
    val enableGrid: Boolean,
    val startTime: Time,
    val endTime: Time
)

data class DeviceSettingsGetResponse(
    val protocol: String,
    val values: DeviceSettingsValues
)

data class DeviceSettingsValues(
    val operation_mode__work_mode: String
)

data class DeviceSettingsSetRequest(
    val id: String,
    val key: String,
    val values: DeviceSettingsValues
)

