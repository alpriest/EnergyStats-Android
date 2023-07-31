package com.alpriest.energystats.ui.settings.battery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.alpriest.energystats.R
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import kotlinx.coroutines.flow.MutableStateFlow

class BatterySOCSettingsViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Networking::class.java, ConfigManaging::class.java, NavController::class.java, Context::class.java)
            .newInstance(network, configManager, navController, context)
    }
}

class BatterySOCSettingsViewModel(
    private val network: Networking,
    private val config: ConfigManaging,
    private val navController: NavController,
    private val context: Context
) : ViewModel() {
    var minSOCStream = MutableStateFlow("")
    var minSOConGridStream = MutableStateFlow("")
    var activityStream = MutableStateFlow<String?>(null)

    suspend fun load() {
        activityStream.value = context.getString(R.string.loading)

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                val result = network.fetchBatterySettings(deviceSN)
                minSOCStream.value = result.minSoc.toString()
                minSOConGridStream.value = result.minGridSoc.toString()
            }
        }.also {
            activityStream.value = null
        }
    }

    suspend fun save() {
        activityStream.value = context.getString(R.string.saving)

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                network.setSoc(
                    minSOC = minSOCStream.value.toInt(),
                    minGridSOC = minSOConGridStream.value.toInt(),
                    deviceSN = deviceSN
                )

                navController.popBackStack()
            } ?: run {
                activityStream.value = null
            }
        }
    }
}