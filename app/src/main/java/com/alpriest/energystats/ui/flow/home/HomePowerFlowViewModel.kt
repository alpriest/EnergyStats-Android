package com.alpriest.energystats.ui.flow.home

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.models.ReportData
import com.alpriest.energystats.models.ReportResponse
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.battery.BatteryPowerViewModel
import java.util.Calendar

const val dateFormat = "yyyy-MM-dd HH:mm:ss"

data class InverterTemperatures(
    val ambient: Double,
    val inverter: Double
)

data class InverterViewModel(
    val temperatures: InverterTemperatures,
    val name: String
)

class HomePowerFlowViewModel(
    val solar: Double,
    val home: Double,
    val grid: Double,
    val todaysGeneration: Double,
    val earnings: String,
    val inverterViewModel: InverterViewModel?,
    val hasBattery: Boolean,
    val battery: BatteryViewModel,
    val configManager: ConfigManaging,
    val homeTotal: Double,
    val gridImportTotal: Double,
    val gridExportTotal: Double
) : ViewModel() {
    val batteryViewModel: BatteryPowerViewModel? = if (hasBattery)
        BatteryPowerViewModel(configManager, battery.chargeLevel, battery.chargePower, battery.temperature, battery.residual, battery.error)
    else
        null
}
