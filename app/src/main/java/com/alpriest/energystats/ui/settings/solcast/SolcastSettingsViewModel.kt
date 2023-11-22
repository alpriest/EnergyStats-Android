package com.alpriest.energystats.ui.settings.solcast

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.stores.ConfigManaging
import kotlinx.coroutines.flow.MutableStateFlow

class SolcastSettingsViewModelFactory(
    private val configManager: ConfigManaging,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SolcastSettingsViewModel(configManager, context) as T
    }
}

class SolcastSettingsViewModel(
    configManager: ConfigManaging,
    private val context: Context
) : ViewModel() {
    val apiKeyStream = MutableStateFlow<String>("")
    val sitesStream = MutableStateFlow<List<SolcastSite>>(listOf())

    init {
        apiKeyStream.value = configManager.solcastSettings.apiKey ?: ""
        sitesStream.value = configManager.solcastSettings.sites
    }
}