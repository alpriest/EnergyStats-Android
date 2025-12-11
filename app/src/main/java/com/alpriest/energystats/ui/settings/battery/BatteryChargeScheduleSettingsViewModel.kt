package com.alpriest.energystats.ui.settings.battery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private val ChargeTimePeriod.hasTimes: Boolean
    get() {
        return start != Time.zero() || end != Time.zero()
    }

class BatteryChargeScheduleSettingsViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BatteryChargeScheduleSettingsViewModel(network, configManager) as T
    }
}

class BatteryChargeScheduleSettingsViewModel(
    private val network: Networking,
    private val config: ConfigManaging
) : ViewModel(), AlertDialogMessageProviding {
    var uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val summaryStream = MutableStateFlow("")
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    private val _viewDataStream = MutableStateFlow(
        BatteryChargeScheduleSettingsViewData(
            "",
            ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false),
            ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false)
        )
    )
    val viewDataStream: StateFlow<BatteryChargeScheduleSettingsViewData> = _viewDataStream

    private val _dirtyState = MutableStateFlow(false)
    val dirtyState: StateFlow<Boolean> = _dirtyState

    private var originalValue: BatteryChargeScheduleSettingsViewData? = null

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
                    val result = network.fetchBatteryTimes(deviceSN)
                    val chargeTimePeriod1 = result.getOrNull(0)?.let {
                        ChargeTimePeriod(
                            start = it.startTime, end = it.endTime, enabled = it.enable
                        )
                    } ?: ChargeTimePeriod.empty()

                    val chargeTimePeriod2 = result.getOrNull(1)?.let {
                        ChargeTimePeriod(
                            start = it.startTime, end = it.endTime, enabled = it.enable
                        )
                    } ?: ChargeTimePeriod.empty()

                    val viewData = BatteryChargeScheduleSettingsViewData(
                        generateSummary(chargeTimePeriod1, chargeTimePeriod2, context),
                        chargeTimePeriod1,
                        chargeTimePeriod2
                    )

                    originalValue = viewData
                    _viewDataStream.value = viewData

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: context.getString(R.string.unknown_error), true))
                }
            } ?: {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }
    }

    fun didChangeTimePeriod1(chargeTimePeriod: ChargeTimePeriod, context: Context) {
        _viewDataStream.value = viewDataStream.value.copy(chargeTimePeriod1 = chargeTimePeriod)
        generateSummary(viewDataStream.value.chargeTimePeriod1, viewDataStream.value.chargeTimePeriod2, context)
    }

    fun didChangeTimePeriod2(chargeTimePeriod: ChargeTimePeriod, context: Context) {
        _viewDataStream.value = viewDataStream.value.copy(chargeTimePeriod2 = chargeTimePeriod)
        generateSummary(viewDataStream.value.chargeTimePeriod1, viewDataStream.value.chargeTimePeriod2, context)
    }

    private fun generateSummary(period1: ChargeTimePeriod, period2: ChargeTimePeriod, context: Context): String {
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

        return resultParts.joinToString(" ")
    }

    suspend fun save(context: Context) {
        uiState.value = UiLoadState(LoadState.Active.Saving)
        val viewData = viewDataStream.value

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN
                val times = listOf(viewData.chargeTimePeriod1, viewData.chargeTimePeriod2).map { it.asChargeTime() }

                try {
                    network.setBatteryTimes(deviceSN, times)
                    resetDirtyState()

                    alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.battery_charge_schedule_was_saved))

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, "Something went wrong fetching data from FoxESS cloud.", false))
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

private fun ChargeTimePeriod.overlaps(period2: ChargeTimePeriod): Boolean {
    return !(end.hour < period2.start.hour || (end.hour == period2.start.hour && end.minute <= period2.start.minute) ||
            start.hour > period2.end.hour || (start.hour == period2.end.hour && start.minute >= period2.end.minute))
}
