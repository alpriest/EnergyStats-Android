package com.alpriest.energystats.ui.settings.devicesettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.network.DeviceSettingsItem
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.ui.flow.BannerAlertManaging

class DeviceSettingsItemViewModelFactory(
    private val configManager: ConfigManaging,
    private val networking: Networking,
    private val item: DeviceSettingsItem,
    private val bannerAlertManager: BannerAlertManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DeviceSettingsItemViewModel(configManager, networking, item) as T
    }
}