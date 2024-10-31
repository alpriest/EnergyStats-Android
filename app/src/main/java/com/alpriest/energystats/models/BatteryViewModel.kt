package com.alpriest.energystats.models

import com.alpriest.energystats.ui.flow.SoC
import com.alpriest.energystats.ui.flow.currentData
import com.alpriest.energystats.ui.flow.currentValue

data class BatteryViewModel(
    var hasBattery: Boolean = false,
    var chargeLevel: Double = 0.0,
    var chargePower: Double = 0.0,
    var temperatures: List<Double> = listOf(0.0),
    var residual: Int = 0,
    var hasError: Boolean = false
) {
    constructor(power: Double, soc: Int, residual: Double, temperatures: List<Double>) : this(
        hasBattery = true,
        chargeLevel = soc / 100.0,
        chargePower = power,
        temperatures = temperatures,
        residual = residual.toInt(),
        hasError = false
    )

    companion object {
        private fun noBattery(): BatteryViewModel {
            return BatteryViewModel(false)
        }

        fun make(
            currentDevice: Device,
            real: OpenRealQueryResponse
        ): BatteryViewModel {
            val battery: BatteryViewModel = if (currentDevice.battery != null || currentDevice.hasBattery) {
                val chargePower = real.datas.currentValue("batChargePower")
                val dischargePower = real.datas.currentValue("batDischargePower")
                val power = chargePower.takeIf { it > 0 } ?: -dischargePower
                val batteryTemperatures: List<Double> = listOf(
                    real.datas.currentData("batTemperature")?.value,
                    real.datas.currentData("batTemperature_1")?.value,
                    real.datas.currentData("batTemperature_2")?.value,
                ).mapNotNull { it }

                BatteryViewModel(
                    power = power,
                    soc = real.datas.SoC().toInt(),
                    residual = real.datas.currentValue("ResidualEnergy") * 10.0,
                    temperatures = batteryTemperatures
                )
            } else {
                noBattery()
            }

            return battery
        }
    }
}