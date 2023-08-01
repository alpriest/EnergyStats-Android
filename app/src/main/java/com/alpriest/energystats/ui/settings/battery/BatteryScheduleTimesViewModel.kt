package com.alpriest.energystats.ui.settings.battery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private val ChargeTimePeriod.hasTimes: Boolean
    get() {
        return start != Time.zero() || end != Time.zero()
    }

class BatteryScheduleTimesViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            Networking::class.java,
            ConfigManaging::class.java,
            NavController::class.java,
            Context::class.java
        ).newInstance(network, configManager, navController, context)
    }
}

private fun ChargeTimePeriod.overlaps(period2: ChargeTimePeriod): Boolean {
    return !(end.hour < period2.start.hour || (end.hour == period2.start.hour && end.minute <= period2.start.minute) ||
            start.hour > period2.end.hour || (start.hour == period2.end.hour && start.minute >= period2.end.minute))
}

class BatteryScheduleTimesViewModel(
    private val network: Networking,
    private val config: ConfigManaging,
    private val navController: NavController,
    private val context: Context
) : ViewModel() {
    val timePeriod1Stream = MutableStateFlow(ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false))
    val timePeriod2Stream = MutableStateFlow(ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false))
    var activityStream = MutableStateFlow<String?>(null)
    val summaryStream = MutableStateFlow("")

    suspend fun load() {
        activityStream.value = context.getString(R.string.loading)

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
            if (period1.hasTimes && period2.hasTimes) {
                result = String.format(context.getString(R.string.both_battery_freeze_periods), period1.description, period2.description)
            } else if (period1.hasTimes) {
                result = String.format(context.getString(R.string.one_battery_freeze_period), period1.description)
            } else if (period2.hasTimes) {
                result = String.format("one_battery_freeze_period", period2.description)
            } else {
                result = context.getString(R.string.no_battery_charge)
            }
        } else if (period1.enabled && period2.enabled) {
            result = String.format(context.getString(R.string.both_battery_charge_periods), period1.description, period2.description)

            if (period1.overlaps(period2)) {
                result += context.getString(R.string.battery_periods_overlap)
            }
        } else if (period1.enabled) {
            result = String.format(context.getString(R.string.one_battery_charge_period), period1.description)
        } else if (period2.enabled) {
            result = String.format(context.getString(R.string.one_battery_charge_period), period2.description)
        }

        summaryStream.value = result
    }

    suspend fun save() {
        activityStream.value = context.getString(R.string.saving)

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN
                val times = listOf(timePeriod1Stream.value, timePeriod2Stream.value).map { it.asChargeTime() }

                network.setBatteryTimes(
                    deviceSN = deviceSN,
                    times = times
                )

                navController.popBackStack()
            } ?: run {
                activityStream.value = null
            }
        }
    }
}