package com.alpriest.energystats.ui.settings.solar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.models.demo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SolarBandingSettingsViewModel(
    config: ConfigManaging
) : ViewModel() {
    private val _viewDataStream = MutableStateFlow(SolarBandingSettingsViewData(0.0f, 0.0f, 0.0f))
    val viewDataStream: StateFlow<SolarBandingSettingsViewData> = _viewDataStream

    private val _dirtyState = MutableStateFlow(false)
    val dirtyState: StateFlow<Boolean> = _dirtyState

    private val _themeStream: MutableStateFlow<AppSettings> = MutableStateFlow(config.appSettingsStream.value)
    val themeStream: StateFlow<AppSettings> = _themeStream

    private var originalValue: SolarBandingSettingsViewData? = null

    init {
        viewModelScope.launch {
            viewDataStream.collect {
                _dirtyState.value = originalValue?.compareTo(it) != 0
            }
        }

        val viewData = SolarBandingSettingsViewData(
            config.solarRangeDefinitions.threshold1.toFloat(),
            config.solarRangeDefinitions.threshold2.toFloat(),
            config.solarRangeDefinitions.threshold3.toFloat()
        )
        originalValue = viewData
        _viewDataStream.value = viewData
    }

    fun didChangeThreshold1(value: Float) {
        updateThresholds { copy(threshold1 = value) }
    }

    fun didChangeThreshold2(value: Float) {
        updateThresholds { copy(threshold2 = value) }
    }

    fun didChangeThreshold3(value: Float) {
        updateThresholds { copy(threshold3 = value) }
    }

    fun reset() {
        _viewDataStream.value = SolarBandingSettingsViewData(
            1.0f,
            2.0f,
            3.0f
        )
    }

    private fun makeAppTheme(): AppSettings {
        return AppSettings.demo()
            .copy(
                solarRangeDefinitions = SolarRangeDefinitions(
                    threshold1 = _viewDataStream.value.threshold1.toDouble(),
                    threshold2 = _viewDataStream.value.threshold2.toDouble(),
                    threshold3 = _viewDataStream.value.threshold3.toDouble()
                ),
                showFinancialSummary = false
            )
    }

    private fun updateThresholds(
        transform: SolarBandingSettingsViewData.() -> SolarBandingSettingsViewData
    ) {
        val current = _viewDataStream.value

        // Apply the specific change (t1/t2/t3)
        val changed = current.transform()

        // Enforce ordering: t1 <= t2 <= t3
        val t1 = changed.threshold1
        val t2 = maxOf(t1, changed.threshold2)
        val t3 = maxOf(t2, changed.threshold3)

        val normalized = changed.copy(
            threshold1 = t1,
            threshold2 = t2,
            threshold3 = t3
        )

        _viewDataStream.value = normalized
        _themeStream.value = makeAppTheme()
    }
}