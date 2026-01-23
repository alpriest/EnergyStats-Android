package com.alpriest.energystats.ui.settings.battery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.helpers.celsius
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.network.Time
import com.alpriest.energystats.shared.network.FoxServerError
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.UiLoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BatteryHeatingScheduleSettingsViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BatteryHeatingScheduleSettingsViewModel(network, configManager) as T
    }
}

class BatteryHeatingScheduleSettingsViewModel(
    private val network: Networking,
    private val config: ConfigManaging
) : ViewModel(), AlertDialogMessageProviding {
    var uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    private val _viewDataStream = MutableStateFlow(
        BatteryHeatingScheduleSettingsViewData(
            available = true,
            enabled = false,
            currentState = null,
            ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false),
            ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false),
            ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false),
            startTemperature = 1.0,
            endTemperature = 10.0,
            minStartTemperature = 1.0,
            maxStartTemperature = 9.0,
            minEndTemperature = 10.0,
            maxEndTemperature = 15.0,
            summary = ""
        )
    )
    val viewDataStream: StateFlow<BatteryHeatingScheduleSettingsViewData> = _viewDataStream

    private val _dirtyState = MutableStateFlow(false)
    val dirtyState: StateFlow<Boolean> = _dirtyState

    private var originalValue: BatteryHeatingScheduleSettingsViewData? = null

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
                    val result = network.fetchBatteryHeatingSchedule(deviceSN)
                    val timePeriod1 = ChargeTimePeriod(start = result.period1Start, end = result.period1End, enabled = result.period1Enabled)
                    val timePeriod2 = ChargeTimePeriod(start = result.period2Start, end = result.period2End, enabled = result.period2Enabled)
                    val timePeriod3 = ChargeTimePeriod(start = result.period3Start, end = result.period3End, enabled = result.period3Enabled)

                    val viewData = BatteryHeatingScheduleSettingsViewData(
                        available = true,
                        enabled = result.enabled,
                        currentState = result.warmUpState,
                        timePeriod1 = timePeriod1,
                        timePeriod2 = timePeriod2,
                        timePeriod3 = timePeriod3,
                        startTemperature = result.startTemperature,
                        endTemperature = result.endTemperature,
                        minStartTemperature = result.minStartTemperature,
                        maxStartTemperature = result.maxStartTemperature,
                        minEndTemperature = result.minEndTemperature,
                        maxEndTemperature = result.maxEndTemperature,
                        summary = generateSummary(
                            result.enabled, timePeriod1, timePeriod2, timePeriod3,
                            result.startTemperature, result.endTemperature, context
                        )
                    )

                    originalValue = viewData
                    _viewDataStream.value = viewData

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: FoxServerError) {
                    if (ex.errno == 41200) {
                        _viewDataStream.value = viewDataStream.value.copy(available = false)
                        uiState.value = UiLoadState(LoadState.Inactive)
                    } else {
                        uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: context.getString(R.string.unknown_error), true))
                    }
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: context.getString(R.string.unknown_error), true))
                }
            } ?: {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }
    }

    private fun generateSummary(
        enabled: Boolean,
        timePeriod1: ChargeTimePeriod,
        timePeriod2: ChargeTimePeriod,
        timePeriod3: ChargeTimePeriod,
        startTemperature: Double,
        endTemperature: Double,
        context: Context
    ): String {
        if (!enabled) {
            return context.getString(R.string.your_battery_heater_is_not_enabled)
        }

        val times = listOfNotNull(
            if (timePeriod1.enabled) timePeriod1 else null,
            if (timePeriod2.enabled) timePeriod2 else null,
            if (timePeriod3.enabled) timePeriod3 else null
        ).sortedWith { first, second -> first.start.compareTo(second.start) }
            .map { it.description }

        if (times.isEmpty()) {
            return context.getString(R.string.battery_schedule_enabled_but_no_active_time_periods)
        }

        return context.getString(R.string.battery_heater_schedule_summary, range(startTemperature, endTemperature), times.commaSeparated())
    }

    private fun range(lower: Double, upper: Double): String {
        return lower.celsius + " and " + upper.celsius
    }

    suspend fun save(context: Context) {
        uiState.value = UiLoadState(LoadState.Active.Saving)
        val viewData = viewDataStream.value

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                try {
                    network.setBatteryHeatingSchedule(
                        deviceSN,
                        viewData.enabled,
                        viewData.timePeriod1.start,
                        viewData.timePeriod1.end,
                        period1Enabled = viewData.timePeriod1.enabled,
                        period2Start = viewData.timePeriod2.start,
                        period2End = viewData.timePeriod2.end,
                        period2Enabled = viewData.timePeriod2.enabled,
                        period3Start = viewData.timePeriod3.start,
                        period3End = viewData.timePeriod3.end,
                        period3Enabled = viewData.timePeriod3.enabled,
                        startTemperature = viewData.startTemperature,
                        endTemperature = viewData.endTemperature
                    )
                    resetDirtyState()

                    alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.battery_charge_schedule_was_saved))

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, context.getString(R.string.something_went_wrong_fetching_data_from_foxess_cloud), false))
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

    fun didChangeTimePeriod1(chargeTimePeriod: ChargeTimePeriod, context: Context) {
        _viewDataStream.value = viewDataStream.value.copy(timePeriod1 = chargeTimePeriod)
        updateSummary(context)
    }

    fun didChangeTimePeriod2(chargeTimePeriod: ChargeTimePeriod, context: Context) {
        _viewDataStream.value = viewDataStream.value.copy(timePeriod2 = chargeTimePeriod)
        updateSummary(context)
    }

    fun didChangeTimePeriod3(chargeTimePeriod: ChargeTimePeriod, context: Context) {
        _viewDataStream.value = viewDataStream.value.copy(timePeriod3 = chargeTimePeriod)
        updateSummary(context)
    }

    fun didChangeEnabled(enabled: Boolean, context: Context) {
        _viewDataStream.value = viewDataStream.value.copy(enabled = enabled)
        updateSummary(context)
    }

    fun didChangeTemperatures(lower: Double, upper: Double, context: Context) {
        _viewDataStream.value = viewDataStream.value.copy(startTemperature = lower, endTemperature = upper)
        updateSummary(context)
    }

    private fun updateSummary(context: Context) {
        val value = viewDataStream.value
        _viewDataStream.value = viewDataStream.value.copy(summary = generateSummary(value.enabled, value.timePeriod1, value.timePeriod2, value.timePeriod3, value.startTemperature, value.endTemperature, context))
    }
}

fun List<String>.commaSeparated(): String {
    return this.joinToString(", ")
}
