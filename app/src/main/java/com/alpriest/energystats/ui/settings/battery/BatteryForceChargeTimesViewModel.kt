package com.alpriest.energystats.ui.settings.battery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BatteryForceChargeTimesViewModelFactory(
    private val network: Networking, private val configManager: ConfigManaging, private val navController: NavController
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Networking::class.java, ConfigManaging::class.java, NavController::class.java).newInstance(network, configManager, navController)
    }
}

private fun ChargeTimePeriod.overlaps(period2: ChargeTimePeriod): Boolean {
    return !(end.hour < period2.start.hour || (end.hour == period2.start.hour && end.minute <= period2.start.minute) ||
            start.hour > period2.end.hour || (start.hour == period2.end.hour && start.minute >= period2.end.minute))
}

class BatteryForceChargeTimesViewModel(
    private val network: Networking, private val config: ConfigManaging, private val navController: NavController
) : ViewModel() {
    val timePeriod1Stream = MutableStateFlow(ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false))
    val timePeriod2Stream = MutableStateFlow(ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false))
    var activityStream = MutableStateFlow<String?>(null)
    val summaryStream = MutableStateFlow<String>("")

    suspend fun load() {
        activityStream.value = "Loading"

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                val result = network.fetchBatteryTimes(deviceSN)
                result.times.getOrNull(0)?.let {
                    timePeriod1Stream.value = ChargeTimePeriod(
                        start = it.startTime, end = it.endTime, enabled = it.enableGrid
                    )
                }

                result.times.getOrNull(1)?.let {
                    timePeriod2Stream.value = ChargeTimePeriod(
                        start = it.startTime, end = it.endTime, enabled = it.enableGrid
                    )
                }

                generateSummary(timePeriod1Stream.value, timePeriod2Stream.value)
            }
        }.also {
            activityStream.value = null
        }

        coroutineScope {
            launch {
                timePeriod1Stream.collect {
                    generateSummary(it, timePeriod2Stream.value)
                }
            }

            launch {
                timePeriod2Stream.collect {
                    generateSummary(timePeriod1Stream.value, it)
                }
            }
        }
    }

    private fun generateSummary(period1: ChargeTimePeriod, period2: ChargeTimePeriod) {
        var result = ""

        if (!period1.enabled && !period2.enabled) {
            result = "Your battery will not be force charged from the grid."
        } else if (period1.enabled && period2.enabled) {
            result = "Your battery will be force charged from the grid ${period1.description}, and ${period2.description}"

            if (period1.overlaps(period2)) {
                result = "$result. These periods overlap, you may want to update them."
            }
        } else if (period1.enabled) {
            result = "Your battery will be force charged from the grid ${period1.description}"
        } else if (period2.enabled) {
            result = "Your battery will be force charged from the grid ${period2.description}"
        }

        summaryStream.value = result
    }

    suspend fun save() {
//        activityStream.value = "Saving"
//
//        runCatching {
//            config.currentDevice.value?.let { device ->
//                val deviceSN = device.deviceSN
//
//                network.setSoc(
//                    minSOC = minSOCStream.value.toInt(),
//                    minGridSOC = minSOConGridStream.value.toInt(),
//                    deviceSN = deviceSN
//                )
//
//                navController.popBackStack()
//            } ?: run {
//                activityStream.value = null
//            }
//        }
    }
}