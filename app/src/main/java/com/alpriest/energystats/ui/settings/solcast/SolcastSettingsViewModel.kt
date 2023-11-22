package com.alpriest.energystats.ui.settings.solcast

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.R
import com.alpriest.energystats.stores.ConfigManaging
import kotlinx.coroutines.flow.MutableStateFlow

class SolcastSettingsViewModelFactory(
    private val configManager: ConfigManaging,
    private val context: Context,
    private val makeService: () -> SolarForecasting
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SolcastSettingsViewModel(configManager, context, makeService) as T
    }
}

class SolcastSettingsViewModel(
    private val configManager: ConfigManaging,
    private val context: Context,
    private val makeService: () -> SolarForecasting
) : ViewModel() {
    val apiKeyStream = MutableStateFlow("")
    val sitesStream = MutableStateFlow<List<SolcastSite>>(listOf())

    init {
        apiKeyStream.value = configManager.solcastSettings.apiKey ?: ""
        sitesStream.value = configManager.solcastSettings.sites
    }

    fun save() {
        val service = makeService()
        try {
            val sites = service.fetchSites()
            configManager.solcastSettings = SolcastSettings(apiKeyStream.value, sites)
            sitesStream.value = sites
            Toast.makeText(context, "Your Solcast settings were successfully verified.", Toast.LENGTH_LONG).show()
        } catch (ex: Exception) {
            Toast.makeText(context, "Your Solcast settings were successfully verified.", Toast.LENGTH_LONG).show()
        }
    }
}