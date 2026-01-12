package com.alpriest.energystats.ui.settings.solar

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.ui.flow.home.SolarPowerFlow
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsBottomSpace
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPaddingValues
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

class SolarBandingSettingsViewModelFactory(
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SolarBandingSettingsViewModel(configManager) as T
    }
}

data class SolarBandingSettingsViewData(
    val threshold1: Float,
    val threshold2: Float,
    val threshold3: Float
): Comparable<SolarBandingSettingsViewData> {
    override fun compareTo(other: SolarBandingSettingsViewData): Int {
        fun Float.round2() = String.format(Locale.ENGLISH, "%.2f", this).toFloat()

        val a1 = threshold1.round2()
        val a2 = threshold2.round2()
        val a3 = threshold3.round2()
        val b1 = other.threshold1.round2()
        val b2 = other.threshold2.round2()
        val b3 = other.threshold3.round2()

        return when {
            a1 != b1 -> a1.compareTo(b1)
            a2 != b2 -> a2.compareTo(b2)
            else -> a3.compareTo(b3)
        }
    }
}

class SolarBandingSettingsView(
    private val configManager: ConfigManaging,
    private val navController: NavHostController
) {
    @Composable
    fun Content(viewModel: SolarBandingSettingsViewModel = viewModel(factory = SolarBandingSettingsViewModelFactory(configManager))) {
        val amount = remember { mutableFloatStateOf(2.0f) }
        val viewData = viewModel.viewDataStream.collectAsState().value
        val mutatedAppTheme = remember { MutableStateFlow(configManager.appSettingsStream.value) }
        val context = LocalContext.current
        trackScreenView("Sun display variation thresholds", "SolarBandingSettingsView")

        ContentWithBottomButtonPair(
            navController,
            onConfirm = {
                configManager.solarRangeDefinitions = SolarRangeDefinitions(
                    threshold1 = viewData.threshold1.toDouble(),
                    threshold2 = viewData.threshold2.toDouble(),
                    threshold3 = viewData.threshold3.toDouble()
                )
                Toast.makeText(context, context.getString(R.string.thresholds_were_saved), Toast.LENGTH_LONG).show()
            },
            dirtyStateFlow = viewModel.dirtyState,
            content = { innerModifier ->
                SettingsPage(innerModifier) {
                    ThresholdView(
                        value = viewData.threshold1,
                        title = stringResource(R.string.low_threshold),
                        description = stringResource(R.string.below_this_amount_the_sun_will_be_yellow),
                        onChange = viewModel::didChangeThreshold1
                    )
                    ThresholdView(
                        value = viewData.threshold2,
                        title = stringResource(R.string.medium_threshold),
                        description = stringResource(R.string.between_low_and_medium_the_sun_will_be_yellow_and_glowing),
                        onChange = viewModel::didChangeThreshold2
                    )
                    ThresholdView(
                        value = viewData.threshold3,
                        title = stringResource(R.string.high_threshold),
                        description = stringResource(R.string.between_medium_and_high_the_sun_will_be_orange_and_glowing_above_high_the_sun_will_be_red_and_glowing),
                        onChange = viewModel::didChangeThreshold3
                    )

                    SettingsColumn(
                        header = stringResource(R.string.example),
                        padding = SettingsPaddingValues.withVertical()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            SolarPowerFlow(
                                amount = amount.floatValue.toDouble(),
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(100.dp),
                                iconHeight = 40.dp,
                                themeStream = mutatedAppTheme
                            )

                            Slider(
                                value = amount.floatValue,
                                onValueChange = { amount.floatValue = it },
                                valueRange = 0.0f..viewData.threshold3 + 0.5f,
                                colors = SliderDefaults.colors(
                                    activeTickColor = colorScheme.primary,
                                    inactiveTickColor = colorScheme.background,
                                    activeTrackColor = colorScheme.primary,
                                    inactiveTrackColor = colorScheme.background,
                                    thumbColor = colorScheme.primary
                                )
                            )
                        }

                        Text(
                            stringResource(R.string.drag_the_slider_above_to_see_how_your_solar_flow_will_display_when_generating_different_levels_of_power),
                            color = colorScheme.onSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    ESButton(viewModel::reset) {
                        Text(stringResource(R.string.restore_defaults))
                    }

                    SettingsBottomSpace()
                }
            },
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 1000)
@Composable
fun SolarBandingSettingsPreview() {
    val viewModel = SolarBandingSettingsViewModel(FakeConfigManager())
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SolarBandingSettingsView(FakeConfigManager(), NavHostController(LocalContext.current))
            .Content(viewModel)
    }
}