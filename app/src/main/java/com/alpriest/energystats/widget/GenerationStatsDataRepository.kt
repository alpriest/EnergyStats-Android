package com.alpriest.energystats.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.stores.WidgetTapAction
import com.alpriest.energystats.ui.AppContainer

class GenerationStatsDataRepository private constructor() {
    var today: Double = 0.0
    var month: Double = 0.0
    var cumulative: Double = 0.0
    var tapAction: WidgetTapAction = WidgetTapAction.Launch

    suspend fun update(context: Context) {
        val appContainer = AppContainer(context)

        appContainer.configManager.currentDevice.value?.let {
            fetchData(appContainer, it)
            tapAction = appContainer.configManager.widgetTapAction
        }

        GenerationStatsWidget().updateAll(context)
    }

    private suspend fun fetchData(appContainer: AppContainer, device: Device) {
        val result = appContainer.networking.fetchPowerGeneration(
            deviceSN = device.deviceSN
        )

        var generationViewData = GenerationViewData(
            today = result.today,
            month = result.month,
            cumulative = result.cumulative
        )

        this.update(generationViewData)
    }

    private fun update(generationViewData: GenerationViewData) {
        today = generationViewData.today
        month = generationViewData.month
        cumulative = generationViewData.cumulative
    }

    // Consume viewModel and erase it so next time we fetch from network
    fun updateFromSharedConfig(context: Context) {
        val appContainer = AppContainer(context)
        appContainer.widgetDataSharer.generationViewData?.let {
            this.update(it)
            appContainer.widgetDataSharer.generationViewData = null
        }
    }

    companion object {
        private var instance: GenerationStatsDataRepository? = null

        fun getInstance(): GenerationStatsDataRepository {
            if (instance == null) {
                instance = GenerationStatsDataRepository()
            }
            return instance!!
        }
    }
}

data class GenerationViewData(
    var today: Double,
    var month: Double,
    var cumulative: Double
)