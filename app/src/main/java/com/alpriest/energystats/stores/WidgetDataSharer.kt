package com.alpriest.energystats.stores

import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.ConfigInterface

interface WidgetDataSharing {
    var batteryViewModel: BatteryViewModel?
}

class WidgetDataSharer(private var config: ConfigInterface) : WidgetDataSharing {
    override var batteryViewModel: BatteryViewModel?
        get() = config.batteryViewModel
        set(value) {
            config.batteryViewModel = value
        }
}