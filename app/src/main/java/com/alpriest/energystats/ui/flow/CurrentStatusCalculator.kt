package com.alpriest.energystats.ui.flow

import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.ui.flow.home.InverterTemperatures
import java.time.LocalDateTime

class RealQueryResponseMapper {
    fun mapCurrentValues(real: OpenQueryResponse): CurrentValues {
        return CurrentValues(
            pvPower = 0.0,
            feedinPower= 0.0,
            gridConsumptionPower = 0.0,
            loadsPower = 0.0,
            ambientTemperation = 0.0,
            invTemperation = 0.0,
            meterPower2 = 0.0,
            hasPV = true, // TODO,
            lastUpdate = LocalDateTime.now()
        )
    }
}

class CurrentStatusCalculator(status: CurrentValues, shouldInvertCT2: Boolean, shouldCombineCT2WithPVPower: Boolean) {
    val currentGrid: Double = status.feedinPower - status.gridConsumptionPower
    val currentHomeConsumption: Double = status.loadsPower
    val currentTemperatures = InverterTemperatures(ambient = status.ambientTemperation, inverter = status.invTemperation)
    val lastUpdate: LocalDateTime = status.lastUpdate
    val currentCT2: Double = if (shouldInvertCT2) 0 - status.meterPower2 else status.meterPower2
    val currentSolarPower: Double = calculateSolarPower(status.hasPV, status, shouldCombineCT2WithPVPower)

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
    val lastUpdate: LocalDateTime,
)

private fun List<RawResponse>.currentValue(forKey: String): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<RawResponse>.currentData(forKey: String): RawData? {
    return firstOrNull { it.variable.lowercase() == forKey.lowercase() }?.data?.lastOrNull()
}