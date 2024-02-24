package com.alpriest.energystats.ui.flow

import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.models.OpenQueryResponseData
import com.alpriest.energystats.ui.flow.home.InverterTemperatures
import com.alpriest.energystats.ui.flow.home.dateFormat
import java.lang.Double.max
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

class CurrentStatusCalculator(
    response: OpenQueryResponse,
    hasPV: Boolean,
    shouldInvertCT2: Boolean,
    shouldCombineCT2WithPVPower: Boolean,
    shouldCombineCT2WithLoadsPower: Boolean
) {
    val currentGrid: Double
    val currentHomeConsumption: Double
    val currentTemperatures: InverterTemperatures
    val lastUpdate: LocalDateTime
    val currentCT2: Double
    val currentSolarPower: Double

    init {
        val status = mapCurrentValues(response, hasPV)
        currentGrid = status.feedinPower - status.gridConsumptionPower
        currentHomeConsumption = calculateLoadsPower(status, shouldCombineCT2WithLoadsPower)
        currentTemperatures = InverterTemperatures(ambient = status.ambientTemperation, inverter = status.invTemperation)
        lastUpdate = convertToTime(item = status.lastUpdate)
        currentCT2 = if (shouldInvertCT2) 0-status.meterPower2 else status.meterPower2
        currentSolarPower = calculateSolarPower(status.hasPV, status, shouldCombineCT2WithPVPower)
    }

    private fun mapCurrentValues(response: OpenQueryResponse, hasPV: Boolean): CurrentRawValues {
        return CurrentRawValues(
            pvPower = response.datas.currentValue(forKey = "pvPower"),
            feedinPower = response.datas.currentValue(forKey = "feedinPower"),
            gridConsumptionPower = response.datas.currentValue(forKey = "gridConsumptionPower"),
            loadsPower = response.datas.currentValue(forKey = "loadsPower"),
            ambientTemperation = response.datas.currentValue(forKey = "ambientTemperation"),
            invTemperation = response.datas.currentValue(forKey = "invTemperation"),
            meterPower2 = response.datas.currentValue(forKey = "meterPower2"),
            hasPV = hasPV,
            lastUpdate = response.time
        )
    }

    private fun convertToTime(item: String): LocalDateTime {
        val simpleDate = SimpleDateFormat(dateFormat, Locale.getDefault()).parse(item)
        return simpleDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    private fun calculateSolarPower(hasPV: Boolean, status: CurrentRawValues, shouldCombineCT2WithPVPower: Boolean): Double {
        return if (hasPV) {
            status.pvPower + (if (shouldCombineCT2WithPVPower) currentCT2 else 0.0)
        } else {
            currentCT2
        }
    }

    private fun calculateLoadsPower(status: CurrentRawValues, shouldCombineCT2WithLoadsPower: Boolean): Double {
        return max(0.0, status.loadsPower + (if (shouldCombineCT2WithLoadsPower) status.meterPower2 else 0.0))
    }
}

data class CurrentRawValues(
    val pvPower: Double,
    val feedinPower: Double,
    val gridConsumptionPower: Double,
    val loadsPower: Double,
    val ambientTemperation: Double,
    val invTemperation: Double,
    val meterPower2: Double,
    val hasPV: Boolean,
    val lastUpdate: String,
)

fun List<OpenQueryResponseData>.currentValue(forKey: String): Double {
    return currentData(forKey)?.value ?: 0.0
}

fun List<OpenQueryResponseData>.currentData(forKey: String): OpenQueryResponseData? {
    return firstOrNull { it.variable.lowercase() == forKey.lowercase() }
}