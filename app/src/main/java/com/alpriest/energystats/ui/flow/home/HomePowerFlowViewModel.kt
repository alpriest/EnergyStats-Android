package com.alpriest.energystats.ui.flow.home

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.EarningsViewModel
import com.alpriest.energystats.ui.flow.battery.BatteryPowerViewModel

const val dateFormat = "yyyy-MM-dd HH:mm:ss"

data class InverterTemperatures(
    val ambient: Double,
    val inverter: Double
)

data class InverterTemperaturesViewModel(
    val temperatures: InverterTemperatures,
    val name: String,
    val plantName: String?
)

class HomePowerFlowViewModel(
    val solar: Double,
    val home: Double,
    val grid: Double,
    val todaysGeneration: GenerationViewModel,
    val earnings: EarningsViewModel,
    val inverterTemperatures: InverterTemperaturesViewModel?,
    val hasBattery: Boolean,
    val battery: BatteryViewModel,
    val configManager: ConfigManaging,
    val homeTotal: Double,
    val gridImportTotal: Double,
    val gridExportTotal: Double,
    val ct2: Double
) : ViewModel() {
    val batteryViewModel: BatteryPowerViewModel? = if (hasBattery)
        BatteryPowerViewModel(configManager, battery.chargeLevel, battery.chargePower, battery.temperature, battery.residual, battery.hasError)
    else
        null
}
