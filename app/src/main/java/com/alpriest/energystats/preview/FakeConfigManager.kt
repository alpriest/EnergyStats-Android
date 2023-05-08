package com.alpriest.energystats.preview

import com.alpriest.energystats.models.*
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.login.ConfigManager

class FakeConfigManager: ConfigManager(
    config = FakeConfigStore(),
    networking = DemoNetworking(),
    rawDataStore = RawDataStore(),
    appVersion = "1.19"
)
