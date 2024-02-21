package com.alpriest.energystats.ui.settings.battery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.R
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.services.ProhibitedActionException
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow

class BatterySOCSettingsViewModelFactory(
    private val network: FoxESSNetworking,
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BatterySOCSettingsViewModel(network, configManager) as T
    }
}

class BatterySOCSettingsViewModel(
    private val network: FoxESSNetworking,
    private val config: ConfigManaging
) : ViewModel(), AlertDialogMessageProviding {
    var minSOCStream = MutableStateFlow("")
    var minSOConGridStream = MutableStateFlow("")
    var uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    suspend fun load(context: Context) {
        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                try {
                    val result = network.openapi_fetchBatterySettings(deviceSN)
                    minSOCStream.value = result.minSoc.toString()
                    minSOConGridStream.value = result.minSocOnGrid.toString()
                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
                }
            } ?: {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }
    }

    suspend fun save(context: Context) {
        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.saving)))

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                try {
                    network.openapi_setBatterySoc(
                        deviceSN = deviceSN,
                        minSOCOnGrid = minSOConGridStream.value.toInt(),
                        minSOC = minSOCStream.value.toInt()
                    )

                    alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.battery_soc_changes_were_saved))

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: ProhibitedActionException) {
                    alertDialogMessage.value = MonitorAlertDialogData(ex, "Cannot save settings because you have an active schedule. You need to delete your schedule and try again.")
                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, "Something went wrong fetching data from FoxESS cloud."))
                }
            } ?: {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }
    }
}