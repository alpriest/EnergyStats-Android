package com.alpriest.energystats.ui.flow.home

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.EarningsViewModel
import com.alpriest.energystats.ui.flow.StringPower
import com.alpriest.energystats.ui.flow.battery.BatteryPowerViewModel

const val dateFormat = "yyyy-MM-dd HH:mm:ss"

data class InverterTemperatures(
    val ambient: Double,
    val inverter: Double
)

class LoadedPowerFlowViewModel(
    val solar: Double,
    val solarStrings: List<StringPower>,
    val home: Double,
    val grid: Double,
    val todaysGeneration: GenerationViewModel,
    val earnings: EarningsViewModel,
    val inverterTemperatures: InverterTemperatures?,
    val hasBattery: Boolean,
    val battery: BatteryViewModel,
    val configManager: ConfigManaging,
    val homeTotal: Double,
    val gridImportTotal: Double,
    val gridExportTotal: Double,
    val ct2: Double,
    val deviceState: DeviceState
) : ViewModel() {
    val batteryViewModel: BatteryPowerViewModel? = if (hasBattery)
        BatteryPowerViewModel(configManager, battery.chargeLevel, battery.chargePower, battery.temperature, battery.residual)
    else
        null
}
