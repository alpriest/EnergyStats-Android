package com.alpriest.energystats.ui.settings.battery

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.R
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import kotlinx.coroutines.flow.MutableStateFlow

class BatterySOCSettingsViewModelFactory(
    private val network: FoxESSNetworking,
    private val configManager: ConfigManaging,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(FoxESSNetworking::class.java, ConfigManaging::class.java, Context::class.java)
            .newInstance(network, configManager, context)
    }
}

class BatterySOCSettingsViewModel(
    private val network: FoxESSNetworking,
    private val config: ConfigManaging,
    private val context: Context
) : ViewModel() {
    var minSOCStream = MutableStateFlow("")
    var minSOConGridStream = MutableStateFlow("")
    var uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))

    suspend fun load() {
        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                try {
                    val result = network.fetchBatterySettings(deviceSN)
                    minSOCStream.value = result.minSoc.toString()
                    minSOConGridStream.value = result.minGridSoc.toString()
                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex.localizedMessage))
                }
            } ?: {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }
    }

    suspend fun save() {
        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.saving)))

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                try {
                    network.setSoc(
                        minSOC = minSOCStream.value.toInt(),
                        minGridSOC = minSOConGridStream.value.toInt(),
                        deviceSN = deviceSN
                    )

                    Toast.makeText(context, context.getString(R.string.battery_soc_changes_were_saved), Toast.LENGTH_LONG).show()

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error("Something went wrong fetching data from FoxESS cloud."))
                }
            } ?: {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }
    }
}