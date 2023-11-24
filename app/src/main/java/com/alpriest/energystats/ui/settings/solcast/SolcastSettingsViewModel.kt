package com.alpriest.energystats.ui.settings.solcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow

class SolcastSettingsViewModelFactory(
    private val configManager: ConfigManaging,
    private val makeService: () -> SolarForecasting
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SolcastSettingsViewModel(configManager, makeService) as T
    }
}

class SolcastSettingsViewModel(
    private val configManager: ConfigManaging,
    private val makeService: () -> SolarForecasting
) : ViewModel(), AlertDialogMessageProviding {
    val apiKeyStream = MutableStateFlow("")
    val sitesStream = MutableStateFlow<List<SolcastSite>>(listOf())
    override val alertDialogMessage = MutableStateFlow<String?>(null)

    init {
        apiKeyStream.value = configManager.solcastSettings.apiKey ?: ""
        sitesStream.value = configManager.solcastSettings.sites
    }

    suspend fun save() {
        val service = makeService()
        try {
            val response = service.fetchSites(apiKey = apiKeyStream.value)
            configManager.solcastSettings = SolcastSettings(apiKeyStream.value, response.sites.map { SolcastSite(site = it) })
            sitesStream.value = configManager.solcastSettings.sites
            alertDialogMessage.value = "Your Solcast settings were successfully verified."
        } catch (ex: Exception) {
            alertDialogMessage.value = "Your Solcast settings failed to verify (${ex.localizedMessage}"
        }
    }
}