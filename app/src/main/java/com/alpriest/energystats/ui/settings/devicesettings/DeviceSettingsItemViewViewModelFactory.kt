package com.alpriest.energystats.ui.settings.devicesettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.models.DeviceSettingsItem
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging

class DeviceSettingsItemViewViewModelFactory(
    private val configManager: ConfigManaging,
    private val networking: Networking,
    private val item: DeviceSettingsItem
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DeviceSettingsItemViewViewModel(configManager, networking, item) as T
    }
}