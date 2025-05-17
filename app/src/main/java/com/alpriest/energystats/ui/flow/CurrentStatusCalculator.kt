package com.alpriest.energystats.ui.flow

import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.OpenQueryResponseData
import com.alpriest.energystats.models.OpenRealQueryResponse
import com.alpriest.energystats.parseToLocalDate
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.InverterTemperatures
import com.alpriest.energystats.ui.settings.PowerFlowStringsSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

            "CT2" ->
                return "CT2"

            else ->
                return settings.pv6Name
        }
    }

    fun stringType(): StringType {
        return when (name) {
            "PV1" -> StringType.PV1
            "PV2" -> StringType.PV2
            "PV3" -> StringType.PV3
            "PV4" -> StringType.PV4
            "PV5" -> StringType.PV5
            "CT2" -> StringType.CT2
            else -> StringType.PV6
        }
    }
}

enum class StringType {
    PV1, PV2, PV3, PV4, PV5, PV6, CT2
}

data class CurrentValues(
    val grid: Double,
    val homeConsumption: Double,
    val temperatures: InverterTemperatures?,
    val ct2: Double,
    val solarPower: Double,
    val solarStringsPower: List<StringPower>
)

class CurrentStatusCalculator(
    val response: OpenRealQueryResponse,
    val device: Device,
    val config: ConfigManaging,
    coroutineScope: CoroutineScope
) {
    private val _currentValuesStream =
        MutableStateFlow(CurrentValues(solarPower = 0.0, solarStringsPower = listOf(), grid = 0.0, homeConsumption = 0.0, temperatures = null, ct2 = 0.0))
    val currentValuesStream: StateFlow<CurrentValues> = _currentValuesStream.asStateFlow()
    var lastUpdate: LocalDateTime = LocalDateTime.now()

    init {
        coroutineScope.launch {
            config.themeStream.collect {
                updateCurrentValues()
            }
        }

        updateCurrentValues()
    }

    private fun updateCurrentValues() {
        val status = mapCurrentValues(response, device.hasPV)
        val grid = status.feedinPower - status.gridConsumptionPower
        val homeConsumption = loadsPower(status, config.shouldCombineCT2WithLoadsPower)
        val temperatures = InverterTemperatures(ambient = status.ambientTemperation, inverter = status.invTemperation)
        lastUpdate = parseToLocalDate(status.lastUpdate)
        val ct2 = if (config.shouldInvertCT2) 0 - status.meterPower2 else status.meterPower2
        val solarPower = calculateSolarPower(status.hasPV, status, config.shouldInvertCT2, config.shouldCombineCT2WithPVPower)
        val solarStringsPower = calculateSolarStringsPower(status.hasPV, status)

        _currentValuesStream.value = CurrentValues(
            grid = grid,
            homeConsumption = homeConsumption,
            temperatures = temperatures,
            ct2 = ct2,
            solarPower = solarPower,
            solarStringsPower = solarStringsPower
        )
    }

    private fun mapCurrentValues(response: OpenRealQueryResponse, hasPV: Boolean): CurrentRawValues {
        var stringsPvPower: List<StringPower> = listOf()
        if (config.powerFlowStrings.enabled) {
            stringsPvPower = config.powerFlowStrings.makeStringPowers(response)
        }

        return CurrentRawValues(
            pvPower = abs(response.datas.currentValue(forKey = "pvPower")),
            stringsPvPower = stringsPvPower,
            feedinPower = response.datas.currentValue(forKey = "feedinPower"),
            gridConsumptionPower = response.datas.currentValue(forKey = "gridConsumptionPower"),
            loadsPower = response.datas.currentValue(forKey = "loadsPower"),
            generationPower = response.datas.currentValue(forKey = "generationPower"),
            ambientTemperation = response.datas.currentValue(forKey = "ambientTemperation"),
            invTemperation = response.datas.currentValue(forKey = "invTemperation"),
            meterPower2 = response.datas.currentValue(forKey = "meterPower2"),
            hasPV = hasPV,
            lastUpdate = response.time
        )
    }

    private fun loadsPower(status: CurrentRawValues, shouldCombineCT2WithLoadsPower: Boolean): Double {
        return abs(status.gridConsumptionPower + status.generationPower - status.feedinPower + (if (shouldCombineCT2WithLoadsPower) abs(status.meterPower2) else 0.0))
    }

    private fun calculateSolarPower(hasPV: Boolean, status: CurrentRawValues, shouldInvertCT2: Boolean, shouldCombineCT2WithPVPower: Boolean): Double {
        val ct2 = if (shouldInvertCT2) 0 - status.meterPower2 else status.meterPower2

        return if (hasPV) {
            status.pvPower + (if (shouldCombineCT2WithPVPower) ct2 else 0.0)
        } else {
            ct2
        }
    }

    private fun calculateSolarStringsPower(hasPV: Boolean, status: CurrentRawValues): List<StringPower> {
        return if (hasPV) {
            status.stringsPvPower
        } else {
            listOf()
        }
    }
}

data class CurrentRawValues(
    val pvPower: Double,
    val stringsPvPower: List<StringPower>,
    val feedinPower: Double,
    val gridConsumptionPower: Double,
    val loadsPower: Double,
    val generationPower: Double,
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
