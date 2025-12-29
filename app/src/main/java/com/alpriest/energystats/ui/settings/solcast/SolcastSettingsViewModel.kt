package com.alpriest.energystats.ui.settings.solcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SolcastSettingsViewData(
    val apiKey: String,
    val sites: List<SolcastSite>
)

class SolcastSettingsViewModelFactory(
    private val configManager: ConfigManaging,
    private val solarForecastingProvider: () -> SolcastCaching
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SolcastSettingsViewModel(configManager, solarForecastingProvider) as T
    }
}

class SolcastSettingsViewModel(
    private val configManager: ConfigManaging,
    private val solarForecastingProvider: () -> SolcastCaching
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    private val _viewDataStream = MutableStateFlow(SolcastSettingsViewData(configManager.solcastSettings.apiKey ?: "", configManager.solcastSettings.sites))
    val viewDataStream: StateFlow<SolcastSettingsViewData> = _viewDataStream

    private val _dirtyState = MutableStateFlow(false)
    val dirtyState: StateFlow<Boolean> = _dirtyState

    private var originalValue: SolcastSettingsViewData? = null

    init {
        originalValue = viewDataStream.value
        viewModelScope.launch {
            viewDataStream.collect {
                _dirtyState.value = originalValue != it
            }
        }
    }

    fun didChange(apiKey: String) {
        _viewDataStream.value = viewDataStream.value.copy(apiKey = apiKey)
    }

    suspend fun save() {
        val service = solarForecastingProvider()
        try {
            val response = service.fetchSites(apiKey = viewDataStream.value.apiKey)
            configManager.solcastSettings = SolcastSettings(viewDataStream.value.apiKey, response.sites.map { SolcastSite(site = it) })
            _viewDataStream.value = viewDataStream.value.copy(sites = configManager.solcastSettings.sites)
            resetDirtyState()
            alertDialogMessage.value = MonitorAlertDialogData(null, "Your Solcast settings were successfully verified.")
        } catch (ex: Exception) {
            alertDialogMessage.value = MonitorAlertDialogData(ex, "Your Solcast settings failed to verify\n\n${ex.localizedMessage}")
        }
    }

    fun removeKey() {
        configManager.solcastSettings = SolcastSettings.defaults
        originalValue = _viewDataStream.value
    }

    private fun resetDirtyState() {
        originalValue = _viewDataStream.value
        _dirtyState.value = false
    }
}