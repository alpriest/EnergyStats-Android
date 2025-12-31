package com.alpriest.energystats.models

import android.content.Context
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.network.OpenRealQueryResponse
import com.alpriest.energystats.shared.models.network.SoC
import com.alpriest.energystats.shared.models.network.currentData
import com.alpriest.energystats.shared.models.network.currentValue
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.battery.BatteryCapacityCalculator
import com.alpriest.energystats.ui.flow.battery.BatteryCapacityEstimate

data class BatteryViewModel(
    var hasBattery: Boolean = false,
    var chargeLevel: Double = 0.0,
    var chargePower: Double = 0.0,
    var temperatures: BatteryTemperatures = BatteryTemperatures(batTemperature = 0.0, batTemperature1 = null, batTemperature2 = null),
    var residual: Int = 0,
    var hasError: Boolean = false,
    var chargeDescription: String? = null
) {
    constructor(power: Double, soc: Int, residual: Double, temperatures: BatteryTemperatures, configManager: ConfigManaging, context: Context) : this(
        hasBattery = true,
        chargeLevel = soc / 100.0,
        chargePower = power,
        temperatures = temperatures,
        residual = residual.toInt(),
        hasError = false,
        chargeDescription = BatteryCapacityCalculator(configManager.batteryCapacityW, configManager.minSOC)
            .batteryPercentageRemaining(power, soc / 100.0)?.let {
                duration(context, it)
            }
    )

    companion object {
        private fun noBattery(): BatteryViewModel {
            return BatteryViewModel(hasBattery = false)
        }

        private fun duration(context: Context, estimate: BatteryCapacityEstimate): String {
            val text = context.getString(estimate.stringId)
            val mins = context.getString(R.string.mins)
            val hour = context.getString(R.string.hour)
            val hours = context.getString(R.string.hours)

            return when (estimate.duration) {
                in 0..60 -> "$text ${estimate.duration} $mins"
                in 61..119 -> "$text ${estimate.duration / 60} $hour"
                in 120..1440 -> "$text ${Math.round(estimate.duration / 60.0)} $hours"
                in 1441..2880 -> "$text ${Math.round(estimate.duration / 1440.0)} day"
                else -> "$text ${Math.round(estimate.duration / 1440.0)} days"
            }
        }

        fun make(
            currentDevice: Device,
            real: OpenRealQueryResponse,
            configManager: ConfigManaging,
            context: Context
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
                    configManager = configManager,
                    context
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