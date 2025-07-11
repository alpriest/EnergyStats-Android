package com.alpriest.energystats.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.stores.WidgetTapAction
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

            val battery = BatteryViewModel.make(device, real, appContainer.configManager, context)
            this.update(battery, appContainer.config.showBatteryTimeEstimateOnWidget)
        } else {
            this.hasBattery = false
        }
    }

    private fun update(battery: BatteryViewModel, showBatteryTimeEstimateOnWidget: Boolean) {
        if (showBatteryTimeEstimateOnWidget) {
            this.chargeDescription = battery.chargeDescription
        } else {
            this.chargeDescription = null
        }
        this.batteryPercentage = battery.chargeLevel.toFloat()
        this.hasBattery = true
    }

    // Consume viewModel and erase it so next time we fetch from network
    fun updateFromSharedConfig(context: Context) {
        val appContainer = AppContainer(context)
        val batteryViewModel = appContainer.widgetDataSharer.batteryViewModel

        if (batteryViewModel != null) {
            this.update(batteryViewModel, appContainer.config.showBatteryTimeEstimateOnWidget)
            appContainer.widgetDataSharer.batteryViewModel = null
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