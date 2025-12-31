package com.alpriest.energystats.stores

import com.alpriest.energystats.preview.FakeStoredConfigStore
import com.alpriest.energystats.shared.models.BatteryData
import com.alpriest.energystats.shared.models.GenerationViewData
import com.alpriest.energystats.shared.models.StoredConfig

interface WidgetDataSharing {
    var batteryData: BatteryData?
    var generationViewData: GenerationViewData?
}

class WidgetDataSharer(private var config: StoredConfig) : WidgetDataSharing {
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

    companion object {
        fun preview(): WidgetDataSharing {
            return WidgetDataSharer(FakeStoredConfigStore())
        }
    }
}
