package com.alpriest.energystats.ui.flow

import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.ui.flow.home.InverterTemperatures
import com.alpriest.energystats.ui.flow.home.InverterViewModel
import com.alpriest.energystats.ui.flow.home.dateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

class CurrentStatusViewModel(device: Device, raws: List<RawResponse>, shouldInvertCT2: Boolean) {
    val currentSolarPower: Double = calculateSolarPower(device, raws, shouldInvertCT2)
    val currentHomeConsumption: Double = raws.currentValue("loadsPower")
    val currentGrid: Double = raws.currentValue("feedInPower") - raws.currentValue("gridConsumptionPower")
    val lastUpdate: LocalDateTime = raws.currentData("gridConsumptionPower")?.time?.let {
        SimpleDateFormat(dateFormat, Locale.getDefault()).parse(it)?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
    } ?: LocalDateTime.now()
    val inverterViewModel = makeInverterViewModel(device, raws)

    private fun makeInverterViewModel(device: Device, raw: List<RawResponse>): InverterViewModel? {
        return if (raw.find { it.variable == "ambientTemperation" } != null &&
            raw.find { it.variable == "invTemperation"} != null) {
            val temperatures = InverterTemperatures(raw.currentValue("ambientTemperation"), raw.currentValue("invTemperation"))
            InverterViewModel(temperatures, name = device.deviceType ?: "")
        } else {
            null
        }
    }

    private fun calculateSolarPower(device: Device, raws: List<RawResponse>, shouldInvertCT2: Boolean): Double {
        val ACtoDCconversion = 0.92

        return if (device.hasPV) {
            raws.currentValue("pvPower") + (if (shouldInvertCT2) 0 - raws.currentValue("meterPower2") else raws.currentValue("meterPower2")) / ACtoDCconversion
        } else {
            raws.currentValue("meterPower2") / ACtoDCconversion
        }
    }
}

private fun List<RawResponse>.currentValue(forKey: String): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<RawResponse>.currentData(forKey: String): RawData? {
    return firstOrNull { it.variable.lowercase() == forKey.lowercase() }?.data?.lastOrNull()
}