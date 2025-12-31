package com.alpriest.energystats.stores

import com.alpriest.energystats.services.ConfigInterface
import com.alpriest.energystats.widget.GenerationViewData

interface WidgetDataSharing {
    var batteryData: BatteryData?
    var generationViewData: GenerationViewData?
}

data class BatteryData(
    val chargeDescription: String?,
    val chargeLevel: Double
)

class WidgetDataSharer(private var config: ConfigInterface) : WidgetDataSharing {
    override var batteryData: BatteryData?
        get() = config.batteryData
        set(value) {
            config.batteryData = value
        }

    override var generationViewData: GenerationViewData?
        get() = config.generationViewData
        set(value) {
            config.generationViewData = value
        }
}