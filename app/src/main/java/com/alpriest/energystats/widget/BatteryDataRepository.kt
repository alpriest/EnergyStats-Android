package com.alpriest.energystats.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.alpriest.energystats.shared.models.BatteryData
import com.alpriest.energystats.shared.models.BatteryViewModel
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.WidgetTapAction
import com.alpriest.energystats.shared.services.BatteryCapacityCalculator
import com.alpriest.energystats.ui.AppContainer

class BatteryDataRepository private constructor() {
    var batteryPercentage: Float = 0f
    var hasBattery = true
    var chargeDescription: String? = null
    var tapAction: WidgetTapAction = WidgetTapAction.Launch

    suspend fun update(context: Context) {
        val appContainer = AppContainer(context)

        appContainer.configManager.currentDevice.value?.let {
            fetchData(context, appContainer, it)
            tapAction = appContainer.configManager.widgetTapAction
            BatteryWidget().updateAll(context)
        }
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

            val battery = BatteryViewModel.make(device, real)

            val chargeDescription = BatteryCapacityCalculator(appContainer.configManager.batteryCapacityW, appContainer.configManager.minSOC)
                .batteryPercentageRemaining(battery.chargePower, battery.chargeLevel / 100.0)?.batteryPercentageRemainingDuration(context)

            val batteryData = BatteryData(chargeDescription, battery.chargeLevel)
            appContainer.widgetDataSharer.batteryData = batteryData
            this.update(batteryData, appContainer.config.showBatteryTimeEstimateOnWidget)
        } else {
            this.hasBattery = false
        }
    }

    private fun update(battery: BatteryData, showBatteryTimeEstimateOnWidget: Boolean) {
        if (showBatteryTimeEstimateOnWidget) {
            this.chargeDescription = battery.chargeDescription
        } else {
            this.chargeDescription = null
        }
        this.batteryPercentage = battery.chargeLevel.toFloat()
        this.hasBattery = true
    }

    // Consume shared data and erase it so next time we fetch from network
    fun updateFromSharedConfig(context: Context) {
        val appContainer = AppContainer(context)
        val batteryData = appContainer.widgetDataSharer.batteryData

        if (batteryData != null) {
            this.update(batteryData, appContainer.config.showBatteryTimeEstimateOnWidget)
            appContainer.widgetDataSharer.batteryData = null
        }
    }

    companion object {
        private var instance: BatteryDataRepository? = null

        fun getInstance(): BatteryDataRepository {
            if (instance == null) {
                instance = BatteryDataRepository()
            }
            return instance!!
        }
    }
}