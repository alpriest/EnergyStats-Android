package com.alpriest.energystats.ui.flow

import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.models.OpenQueryResponseData
import com.alpriest.energystats.ui.flow.home.InverterTemperatures
import com.alpriest.energystats.ui.flow.home.dateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

class RealQueryResponseMapper {
    fun mapCurrentValues(response: OpenQueryResponse): CurrentValues {
        return CurrentValues(
            pvPower = response.datas.currentValue(forKey = "pvPower"),
            feedinPower = response.datas.currentValue(forKey = "feedinPower"),
            gridConsumptionPower = response.datas.currentValue(forKey = "gridConsumptionPower"),
            loadsPower = response.datas.currentValue(forKey = "loadsPower"),
            ambientTemperation = response.datas.currentValue(forKey = "ambientTemperation"),
            invTemperation = response.datas.currentValue(forKey = "invTemperation"),
            meterPower2 = response.datas.currentValue(forKey = "meterPower2"),
            hasPV = true, // TODO,
            lastUpdate = response.time
        )
    }
}

class CurrentStatusCalculator(status: CurrentValues, shouldInvertCT2: Boolean, shouldCombineCT2WithPVPower: Boolean) {
    val currentGrid: Double = status.feedinPower - status.gridConsumptionPower
    val currentHomeConsumption: Double = status.loadsPower
    val currentTemperatures = InverterTemperatures(ambient = status.ambientTemperation, inverter = status.invTemperation)
    val lastUpdate: LocalDateTime = convertToTime(item = status.lastUpdate)
    val currentCT2: Double = if (shouldInvertCT2) 0 - status.meterPower2 else status.meterPower2
    val currentSolarPower: Double = calculateSolarPower(status.hasPV, status, shouldCombineCT2WithPVPower)

    private fun convertToTime(item: String): LocalDateTime {
        val simpleDate = SimpleDateFormat(dateFormat, Locale.getDefault()).parse(item)
        return simpleDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    private fun calculateSolarPower(hasPV: Boolean, status: CurrentValues, shouldCombineCT2WithPVPower: Boolean): Double {
        return if (hasPV) {
            status.pvPower + (if (shouldCombineCT2WithPVPower) currentCT2 else 0.0)
        } else {
            currentCT2
        }
    }
}

data class CurrentValues(
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