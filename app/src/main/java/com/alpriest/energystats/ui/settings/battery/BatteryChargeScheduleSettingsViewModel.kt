package com.alpriest.energystats.ui.settings.battery

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private val ChargeTimePeriod.hasTimes: Boolean
    get() {
        return start != Time.zero() || end != Time.zero()
    }

class BatteryChargeScheduleSettingsViewModelFactory(
    private val network: FoxESSNetworking,
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BatteryChargeScheduleSettingsViewModel(network, configManager) as T
    }
}

private fun ChargeTimePeriod.overlaps(period2: ChargeTimePeriod): Boolean {
    return !(end.hour < period2.start.hour || (end.hour == period2.start.hour && end.minute <= period2.start.minute) ||
            start.hour > period2.end.hour || (start.hour == period2.end.hour && start.minute >= period2.end.minute))
}

class BatteryChargeScheduleSettingsViewModel(
    private val network: FoxESSNetworking,
    private val config: ConfigManaging
) : ViewModel(), AlertDialogMessageProviding {
    val timePeriod1Stream = MutableStateFlow(ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false))
    val timePeriod2Stream = MutableStateFlow(ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false))
    var uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val summaryStream = MutableStateFlow("")
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    suspend fun load(context: Context) {
        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                try {
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

                    generateSummary(timePeriod1Stream.value, timePeriod2Stream.value, context)
                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
                }
            } ?: {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }

        coroutineScope {
            launch {
                timePeriod1Stream.collect {
                    generateSummary(it, timePeriod2Stream.value, context)
                }
            }

            launch {
                timePeriod2Stream.collect {
                    generateSummary(timePeriod1Stream.value, it, context)
                }
            }
        }
    }

    private fun generateSummary(period1: ChargeTimePeriod, period2: ChargeTimePeriod, context: Context) {
        val resultParts = mutableListOf<String>()

        if (!period1.enabled && !period2.enabled) {
            if (period1.hasTimes && period2.hasTimes) {
                resultParts.add(String.format(context.getString(R.string.both_battery_freeze_periods), period1.description, period2.description))
            } else if (period1.hasTimes) {
                resultParts.add(String.format(context.getString(R.string.one_battery_freeze_period), period1.description))
            } else if (period2.hasTimes) {
                resultParts.add(String.format("one_battery_freeze_period", period2.description))
            } else {
                resultParts.add(context.getString(R.string.no_battery_charge))
            }
        } else if (period1.enabled && period2.enabled) {
            resultParts.add(String.format(context.getString(R.string.both_battery_charge_periods), period1.description, period2.description))

            if (period1.overlaps(period2)) {
                resultParts.add(context.getString(R.string.battery_periods_overlap))
            }
        } else if (period1.enabled) {
            resultParts.add(String.format(context.getString(R.string.one_battery_charge_period), period1.description))

            if (period2.hasTimes) {
                resultParts.add(String.format(context.getString(R.string.one_battery_freeze_period), period2.description))
            }

            if (period1.overlaps(period2)) {
                resultParts.add(context.getString(R.string.battery_periods_overlap))
            }
        } else if (period2.enabled) {
            resultParts.add(String.format(context.getString(R.string.one_battery_charge_period), period2.description))

            if (period1.hasTimes) {
                resultParts.add(String.format(context.getString(R.string.one_battery_freeze_period), period1.description))
            }

            if (period1.overlaps(period2)) {
                resultParts.add(context.getString(R.string.battery_periods_overlap))
            }
        }

        summaryStream.value = resultParts.joinToString(" ")
    }

    suspend fun save(context: Context) {
        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.saving)))

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN
                val times = listOf(timePeriod1Stream.value, timePeriod2Stream.value).map { it.asChargeTime() }

                try {
                    network.setBatteryTimes(
                        deviceSN = deviceSN,
                        times = times
                    )

                    alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.battery_charge_schedule_was_saved))

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex,"Something went wrong fetching data from FoxESS cloud."))
                }
            } ?: {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }
    }
}