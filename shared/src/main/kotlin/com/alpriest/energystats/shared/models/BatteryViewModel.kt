package com.alpriest.energystats.shared.models

import com.alpriest.energystats.shared.models.network.OpenRealQueryResponse
import com.alpriest.energystats.shared.models.network.SoC
import com.alpriest.energystats.shared.models.network.currentData
import com.alpriest.energystats.shared.models.network.currentValue

data class BatteryViewModel(
    var hasBattery: Boolean = false,
    var chargeLevel: Double = 0.0,
    var chargePower: Double = 0.0,
    var temperatures: BatteryTemperatures = BatteryTemperatures(batTemperature = 0.0, batTemperature1 = null, batTemperature2 = null),
    var residual: Int = 0,
    var hasError: Boolean = false
) {
    constructor(power: Double, soc: Int, residual: Double, temperatures: BatteryTemperatures) : this(
        hasBattery = true,
        chargeLevel = soc / 100.0,
        chargePower = power,
        temperatures = temperatures,
        residual = residual.toInt(),
        hasError = false
    )

    companion object {
        private fun noBattery(): BatteryViewModel {
            return BatteryViewModel(hasBattery = false)
        }

        fun make(
            currentDevice: Device,
            real: OpenRealQueryResponse,
        ): BatteryViewModel {
            val battery: BatteryViewModel = if (currentDevice.battery != null || currentDevice.hasBattery) {
                val chargePower = real.datas.currentValue("batChargePower")
                val dischargePower = real.datas.currentValue("batDischargePower")
                val power = chargePower.takeIf { it > 0 } ?: -dischargePower
                val temps = BatteryTemperatures(
                    real.datas.currentData("batTemperature")?.value,
                    real.datas.currentData("batTemperature_1")?.value,
                    real.datas.currentData("batTemperature_2")?.value,
                )

                BatteryViewModel(
                    power = power,
                    soc = real.datas.SoC().toInt(),
                    residual = real.datas.currentValue("ResidualEnergy") * 10.0,
                    temperatures = temps,
                )
            } else {
                noBattery()
            }

            return battery
        }
    }
}

data class BatteryTemperatures(
    val batTemperature: Double?,
    val batTemperature1: Double?,
    val batTemperature2: Double?
)