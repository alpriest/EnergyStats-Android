package com.alpriest.energystats.models

data class BatteryViewModel(
    var hasBattery: Boolean = false,
    var chargeLevel: Double = 0.0,
    var chargePower: Double = 0.0,
    var temperature: Double = 0.0,
    var residual: Int = 0,
    var hasError: Boolean = false
) {
    constructor(battery: BatteryResponse, hasError: Boolean) : this(
        hasBattery = true,
        chargeLevel = battery.soc / 100.0,
        chargePower = 0 - battery.power,
        temperature = battery.temperature,
        residual = battery.residual.toInt(),
        hasError = hasError
    )

    companion object {
        fun noBattery(): BatteryViewModel {
            return BatteryViewModel(false)
        }
    }
}