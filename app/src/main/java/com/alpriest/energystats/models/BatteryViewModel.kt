package com.alpriest.energystats.models

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
        fun noBattery(): BatteryViewModel {
            return BatteryViewModel(false)
        }
    }
}