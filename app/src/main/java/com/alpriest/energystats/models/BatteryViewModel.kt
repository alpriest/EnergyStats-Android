package com.alpriest.energystats.models

import com.alpriest.energystats.ui.flow.SoC
import com.alpriest.energystats.ui.flow.currentValue

data class BatteryViewModel(
    var hasBattery: Boolean = false,
    var chargeLevel: Double = 0.0,
    var chargePower: Double = 0.0,
    var temperature: Double = 0.0,
    var residual: Int = 0,
    var hasError: Boolean = false
) {
    constructor(power: Double, soc: Int, residual: Double, temperature: Double): this(
        hasBattery = true,
        chargeLevel = soc / 100.0,
        chargePower = power,
        temperature = temperature,
        residual = residual.toInt(),
        hasError = false
    )

    companion object {
        private fun noBattery(): BatteryViewModel {
            return BatteryViewModel(false)
        }

        fun make(
            currentDevice: Device,
            real: OpenQueryResponse
        ): BatteryViewModel {
            val battery: BatteryViewModel = if (currentDevice.battery != null || currentDevice.hasBattery) {
                val chargePower = real.datas.currentValue("batChargePower")
                val dischargePower = real.datas.currentValue("batDischargePower")
                val power = chargePower.takeIf { it > 0 } ?: -dischargePower

                BatteryViewModel(
                    power = power,
                    soc = real.datas.SoC().toInt(),
                    residual = real.datas.currentValue("ResidualEnergy") * 10.0,
                    temperature = real.datas.currentValue("batTemperature")
                )
            } else {
                BatteryViewModel.noBattery()
            }
            return battery

        }
    }
}