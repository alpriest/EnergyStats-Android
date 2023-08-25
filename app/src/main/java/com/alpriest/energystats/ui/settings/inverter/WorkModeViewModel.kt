package com.alpriest.energystats.ui.settings.inverter

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.alpriest.energystats.R
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import kotlinx.coroutines.flow.MutableStateFlow

class WorkModeViewModelFactory(
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

class WorkModeViewModel(
    val network: Networking,
    val config: ConfigManaging,
    val navController: NavController,
    val context: Context
) : ViewModel() {
    var workModeStream = MutableStateFlow(WorkMode.SELF_USE)
    var activityStream = MutableStateFlow<String?>(null)

    suspend fun load() {
        activityStream.value = context.getString(R.string.loading)

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceID = device.deviceID

                try {
                    val result = network.fetchWorkMode(deviceID)
                    workModeStream.value = InverterWorkMode.from(result.values.operation_mode__work_mode).asWorkMode()
                } catch (ex: Exception) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                }
            }.also {
                activityStream.value = null
            }
        }
    }

    suspend fun save() {
        activityStream.value = context.getString(R.string.saving)

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceID = device.deviceID

                try {
                    network.setWorkMode(
                        deviceID = deviceID,
                        workMode = workModeStream.value.asInverterWorkMode().text,
                    )

                    Toast.makeText(context, context.getString(R.string.inverter_work_mode_was_saved), Toast.LENGTH_LONG).show()

                    activityStream.value = null
                } catch (ex: Exception) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                }
            } ?: run {
                activityStream.value = null
            }
        }
    }

    fun select(workMode: WorkMode) {
        workModeStream.value = workMode
    }
}
