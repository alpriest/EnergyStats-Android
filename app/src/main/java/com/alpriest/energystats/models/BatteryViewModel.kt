package com.alpriest.energystats.models

class BatteryViewModel {
    var hasBattery: Boolean
    var chargePower: Double
    var chargeLevel: Double

    constructor(battery: BatteryResponse) {
        chargeLevel = battery.soc / 100.0
        chargePower = 0 - battery.power
        hasBattery = true
    }

    constructor() {
        hasBattery = false
        chargeLevel = 0.0
        chargePower = 0.0
    }

    companion object {
        fun noBattery(): BatteryViewModel {
            return BatteryViewModel()
        }
    }
}