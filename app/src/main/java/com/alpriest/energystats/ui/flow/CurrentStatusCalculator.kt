package com.alpriest.energystats.ui.flow

import com.alpriest.energystats.models.OpenQueryResponseData
import com.alpriest.energystats.models.OpenRealQueryResponse
import com.alpriest.energystats.parseToLocalDate
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.InverterTemperatures
import com.alpriest.energystats.ui.settings.PowerFlowStringsSettings
import java.time.LocalDateTime
import kotlin.math.abs

data class StringPower(val name: String, val amount: Double) {
    fun displayName(settings: PowerFlowStringsSettings): String {
        when (name) {
            "PV1" ->
                return settings.pv1Name

            "PV2" ->
                return settings.pv2Name

            "PV3" ->
                return settings.pv3Name

            "PV4" ->
                return settings.pv4Name

            "PV5" ->
                return settings.pv5Name

            else ->
                return settings.pv6Name
        }
    }
}

class CurrentStatusCalculator(
    response: OpenRealQueryResponse,
    hasPV: Boolean,
    val config: ConfigManaging
) {
    val currentGrid: Double
    val currentHomeConsumption: Double
    val currentTemperatures: InverterTemperatures
    val lastUpdate: LocalDateTime
    val currentCT2: Double
    val currentSolarPower: Double
    val currentSolarStringsPower: List<StringPower>

    init {
        val status = mapCurrentValues(response, hasPV)
        currentGrid = status.feedinPower - status.gridConsumptionPower
        currentHomeConsumption = if (config.useTraditionalLoadFormula) calculateLoadsPower(status) else loadsPower(status, config.shouldCombineCT2WithLoadsPower)
        currentTemperatures = InverterTemperatures(ambient = status.ambientTemperation, inverter = status.invTemperation)
        lastUpdate = parseToLocalDate(status.lastUpdate)
        currentCT2 = if (config.shouldInvertCT2) 0 - status.meterPower2 else status.meterPower2
        currentSolarPower = calculateSolarPower(status.hasPV, status, config.shouldCombineCT2WithPVPower)
        currentSolarStringsPower = calculateSolarStringsPower(status.hasPV, status)
    }

    private fun loadsPower(status: CurrentRawValues, shouldCombineCT2WithLoadsPower: Boolean): Double {
        return status.loadsPower + (if (shouldCombineCT2WithLoadsPower) status.meterPower2 else 0.0)
    }

    private fun mapCurrentValues(response: OpenRealQueryResponse, hasPV: Boolean): CurrentRawValues {
        var stringsPvPower: List<StringPower> = listOf()
        if (config.powerFlowStrings.enabled) {
            stringsPvPower = config.powerFlowStrings.makeStringPowers(response)
        }

        return CurrentRawValues(
            pvPower = response.datas.currentValue(forKey = "pvPower"),
            stringsPvPower = stringsPvPower,
            feedinPower = response.datas.currentValue(forKey = "feedinPower"),
            gridConsumptionPower = response.datas.currentValue(forKey = "gridConsumptionPower"),
            loadsPower = response.datas.currentValue(forKey = "loadsPower"),
            generationPower = response.datas.currentValue(forKey = "generationPower"),
            epsPower = response.datas.currentValue(forKey = "epsPower"),
            ambientTemperation = response.datas.currentValue(forKey = "ambientTemperation"),
            invTemperation = response.datas.currentValue(forKey = "invTemperation"),
            meterPower2 = response.datas.currentValue(forKey = "meterPower2"),
            hasPV = hasPV,
            lastUpdate = response.time
        )
    }

    private fun calculateSolarPower(hasPV: Boolean, status: CurrentRawValues, shouldCombineCT2WithPVPower: Boolean): Double {
        return if (hasPV) {
            status.pvPower + (if (shouldCombineCT2WithPVPower) currentCT2 else 0.0)
        } else {
            currentCT2
        }
    }

    private fun calculateSolarStringsPower(hasPV: Boolean, status: CurrentRawValues): List<StringPower> {
        return if (hasPV) {
            status.stringsPvPower
        } else {
            listOf()
        }
    }

    private fun calculateLoadsPower(status: CurrentRawValues): Double {
        return status.gridConsumptionPower + status.generationPower - status.feedinPower + abs(status.meterPower2)
    }
}

data class CurrentRawValues(
    val pvPower: Double,
    val stringsPvPower: List<StringPower>,
    val feedinPower: Double,
    val gridConsumptionPower: Double,
    val loadsPower: Double,
    val generationPower: Double,
    val epsPower: Double,
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

fun List<OpenQueryResponseData>.SoC(): Double {
    return currentData("SoC")?.value ?: currentData("SoC_1")?.value ?: 0.0
}
