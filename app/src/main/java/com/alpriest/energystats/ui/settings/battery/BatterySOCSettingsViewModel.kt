package com.alpriest.energystats.ui.settings.battery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import com.alpriest.energystats.shared.network.ProhibitedActionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BatterySOCSettingsViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BatterySOCSettingsViewModel(network, configManager) as T
    }
}

class BatterySOCSettingsViewModel(
    private val network: Networking,
    private val config: ConfigManaging
) : ViewModel(), AlertDialogMessageProviding {
    var uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    private val _viewDataStream = MutableStateFlow(BatterySOCSettingsViewData("",""))
    val viewDataStream: StateFlow<BatterySOCSettingsViewData> = _viewDataStream

    private val _dirtyState = MutableStateFlow(false)
    val dirtyState: StateFlow<Boolean> = _dirtyState

    private var originalValue: BatterySOCSettingsViewData? = null

    fun didChangeMinSOC(value: String) {
        _viewDataStream.value = viewDataStream.value.copy(minSOC = value)
    }

    fun didChangeMinSOConGrid(value: String) {
        _viewDataStream.value = viewDataStream.value.copy(minSOConGrid = value)
    }

    init {
        viewModelScope.launch {
            viewDataStream.collect {
                _dirtyState.value = originalValue != it
            }
        }
    }

    suspend fun load(context: Context) {
        uiState.value = UiLoadState(LoadState.Active.Loading)

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                try {
                    val result = network.fetchBatterySettings(deviceSN)
                    val viewData = BatterySOCSettingsViewData(result.minSoc.toString(), result.minSocOnGrid.toString())
                    originalValue = viewData
                    _viewDataStream.value = viewData
                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: context.getString(R.string.unknown_error)))
                }
            } ?: {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }
    }

    suspend fun save(context: Context) {
        uiState.value = UiLoadState(LoadState.Active.Saving)

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN
                val viewData = viewDataStream.value

                try {
                    network.setBatterySoc(
                        deviceSN = deviceSN,
                        minSOCOnGrid = viewData.minSOConGrid.toInt(),
                        minSOC = viewData.minSOC.toInt()
                    )
                    resetDirtyState()

                    alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.battery_soc_changes_were_saved))

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: ProhibitedActionException) {
                    alertDialogMessage.value = MonitorAlertDialogData(ex, "Cannot save settings because you have an active schedule. You need to delete your schedule and try again.")
                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, "Something went wrong fetching data from FoxESS cloud.", true))
                }
            } ?: {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }
    }

    private fun resetDirtyState() {
        originalValue = _viewDataStream.value
        _dirtyState.value = false
    }
}