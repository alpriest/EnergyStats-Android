package com.alpriest.energystats.stores

import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.services.ConfigInterface
import com.alpriest.energystats.widget.GenerationViewData

interface WidgetDataSharing {
    var batteryViewModel: BatteryViewModel?
    var generationViewData: GenerationViewData?
}

class WidgetDataSharer(private var config: ConfigInterface) : WidgetDataSharing {
    override var batteryViewModel: BatteryViewModel?
        get() = config.batteryViewModel
        set(value) {
            config.batteryViewModel = value
        }

    override var generationViewData: GenerationViewData?
        get() = config.generationViewData
        set(value) {
            config.generationViewData = value
        }
}