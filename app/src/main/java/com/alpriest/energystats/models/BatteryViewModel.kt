package com.alpriest.energystats.models

data class BatteryViewModel(
    var hasBattery: Boolean,
    var chargeLevel: Double,
    var chargePower: Double,
    var temperature: Double,
    var residual: Int,
    var error: Error?
) {
    constructor(battery: BatteryResponse) : this(
        hasBattery = true,
        chargeLevel = battery.soc / 100.0,
        chargePower = 0 - battery.power,
        temperature = battery.temperature,
        residual = battery.residual.toInt(),
        error = null
    )

    constructor(
        hasBattery: Boolean = false,
        chargeLevel: Double = 0.0,
        chargePower: Double = 0.0,
        temperature: Double = 0.0,
        residual: Int = 0
    ) : this(hasBattery, chargeLevel, chargePower, temperature, residual, null)

    companion object {
        fun noBattery(): BatteryViewModel {
            return BatteryViewModel()
        }
    }
}