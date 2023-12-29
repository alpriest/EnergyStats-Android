package com.alpriest.energystats.ui.settings.solcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow

class SolcastSettingsViewModelFactory(
    private val configManager: ConfigManaging,
    private val solarForecastingProvider: () -> SolarForecasting
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SolcastSettingsViewModel(configManager, solarForecastingProvider) as T
    }
}

class SolcastSettingsViewModel(
    private val configManager: ConfigManaging,
    private val solarForecastingProvider: () -> SolarForecasting
) : ViewModel(), AlertDialogMessageProviding {
    val apiKeyStream = MutableStateFlow("")
    val sitesStream = MutableStateFlow<List<SolcastSite>>(listOf())
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    init {
        apiKeyStream.value = configManager.solcastSettings.apiKey ?: ""
        sitesStream.value = configManager.solcastSettings.sites
    }

    suspend fun save() {
        val service = solarForecastingProvider()
        try {
            val response = service.fetchSites(apiKey = apiKeyStream.value)
            configManager.solcastSettings = SolcastSettings(apiKeyStream.value, response.sites.map { SolcastSite(site = it) })
            sitesStream.value = configManager.solcastSettings.sites
            alertDialogMessage.value = MonitorAlertDialogData(null, "Your Solcast settings were successfully verified.")
        } catch (ex: Exception) {
            alertDialogMessage.value = MonitorAlertDialogData(ex, "Your Solcast settings failed to verify (${ex.localizedMessage}")
        }
    }

    fun removeKey() {
        configManager.solcastSettings = SolcastSettings.defaults
        apiKeyStream.value = ""
        sitesStream.value = listOf()
    }
}