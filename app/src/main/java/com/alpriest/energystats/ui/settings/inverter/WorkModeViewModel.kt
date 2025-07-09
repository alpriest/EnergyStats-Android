package com.alpriest.energystats.ui.settings.inverter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.DeviceSettingsItem
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.inverter.schedule.WorkMode
import kotlinx.coroutines.flow.MutableStateFlow

class WorkModeViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkModeViewModel(network, configManager, navController) as T
    }
}

class WorkModeViewModel(
    val network: Networking,
    val config: ConfigManaging,
    val navController: NavController
) : ViewModel(), AlertDialogMessageProviding {
    var workModeStream = MutableStateFlow(WorkMode.SelfUse)
    var uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val items = listOf(WorkMode.SelfUse, WorkMode.Feedin, WorkMode.Backup, WorkMode.ForceCharge, WorkMode.ForceDischarge)

    suspend fun load(context: Context) {
        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                try {
                    val result = network.fetchDeviceSettingsItem(deviceSN, DeviceSettingsItem.WorkMode)
                    workModeStream.value = WorkMode.from(result.value)
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
                    network.setDeviceSettingsItem(
                        deviceSN,
                        DeviceSettingsItem.WorkMode,
                        workModeStream.value.networkTitle()
                    )

                    alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.inverter_work_mode_was_saved))

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, context.getString(R.string.something_went_wrong_fetching_data_from_foxess_cloud)))
                }
            } ?: run {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }
    }

    fun select(workMode: WorkMode) {
        workModeStream.value = workMode
    }
}
