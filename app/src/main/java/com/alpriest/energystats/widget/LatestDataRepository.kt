package com.alpriest.energystats.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.alpriest.energystats.R
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.ui.AppContainer
import com.alpriest.energystats.ui.flow.battery.BatteryCapacityCalculator
import com.alpriest.energystats.ui.flow.battery.BatteryCapacityEstimate

class LatestDataRepository private constructor() {
    var batteryPercentage: Float = 0f
    var hasBattery = true
    var chargeDescription: String? = null

    suspend fun update(context: Context) {
        val appContainer = AppContainer(context)

        appContainer.configManager.currentDevice.value?.let {
            fetchData(context, appContainer, it)
        }

        BatteryWidget().updateAll(context)
    }

    private suspend fun fetchData(context: Context, appContainer: AppContainer, device: Device) {
        if (device.hasBattery) {
            val variables: List<String> = listOf(
                "batChargePower",
                "batDischargePower",
                "SoC",
                "SoC_1",
                "batTemperature",
                "batTemperature_1",
                "batTemperature_2",
                "ResidualEnergy"
            )

            val real = appContainer.networking.fetchRealData(
                deviceSN = device.deviceSN,
                variables
            )

            val minSOC = device.battery?.minSOC?.toDouble() ?: 0.0
            val calculator = BatteryCapacityCalculator(appContainer.configManager.batteryCapacity, minSOC)
            val battery = BatteryViewModel.make(device, real)
            if (appContainer.config.showBatteryTimeEstimateOnWidget) {
                calculator.batteryPercentageRemaining(battery.chargePower, battery.chargeLevel)?.let {
                    chargeDescription = duration(context, it)
                }
            }
            batteryPercentage = battery.chargeLevel.toFloat()
            hasBattery = true
        } else {
            hasBattery = false
        }
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
            in 1441 .. 2880 -> "$text ${Math.round(estimate.duration / 1440.0)} day"
            else -> "$text ${Math.round(estimate.duration / 1440.0)} days"
        }
    }

    companion object {
        private var instance: LatestDataRepository? = null

        fun getInstance(): LatestDataRepository {
            if (instance == null) {
                instance = LatestDataRepository()
            }
            return instance!!
        }
    }
}