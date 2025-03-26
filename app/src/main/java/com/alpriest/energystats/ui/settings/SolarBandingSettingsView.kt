package com.alpriest.energystats.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.SolarPowerFlow
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.SolarRangeDefinitions
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ThresholdView(mutableStateValue: MutableState<Float>, title: String, description: String) {
    val value = mutableStateValue.value

    SettingsColumn(
        padding = SettingsPaddingValues.withVertical(),
        header = title
    ) {
        Row {
            Slider(
                value = value,
                onValueChange = { mutableStateValue.value = it },
                valueRange = 0.1f..10.0f,
                modifier = Modifier.weight(1.0f),
                steps = 48,
                colors = SliderDefaults.colors(
                    activeTickColor = colorScheme.primary,
                    inactiveTickColor = colorScheme.background,
                    activeTrackColor = colorScheme.primary,
                    inactiveTrackColor = colorScheme.background,
                    thumbColor = colorScheme.primary
                )
            )

            Text(
                value.toDouble().kW(3),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.2f),
                color = colorScheme.onSecondary
            )
        }

        Text(
            description,
            color = colorScheme.onSecondary
        )
    }
}

@Composable
fun SolarBandingSettingsView(navController: NavHostController, configManager: ConfigManaging, modifier: Modifier) {
    val amount = remember { mutableFloatStateOf(2.0f) }
    val threshold1 = remember { mutableFloatStateOf(configManager.themeStream.value.solarRangeDefinitions.threshold1.toFloat()) }
    val threshold2 = remember { mutableFloatStateOf(configManager.themeStream.value.solarRangeDefinitions.threshold2.toFloat()) }
    val threshold3 = remember { mutableFloatStateOf(configManager.themeStream.value.solarRangeDefinitions.threshold3.toFloat()) }
    val mutatedAppTheme = remember { MutableStateFlow(configManager.themeStream.value) }
    val context = LocalContext.current
    trackScreenView("Sun display variation thresholds", "SolarBandingSettingsView")

    LaunchedEffect(threshold1.floatValue) {
        if (threshold1.floatValue > threshold2.floatValue) {
            threshold2.floatValue = threshold1.floatValue
        }

        mutatedAppTheme.value = makeAppTheme(threshold1.floatValue, threshold2.floatValue, threshold3.floatValue)
    }

    LaunchedEffect(threshold2.floatValue) {
        if (threshold2.floatValue > threshold3.floatValue) {
            threshold3.floatValue = threshold2.floatValue
        }

        if (threshold2.floatValue < threshold1.floatValue) {
            threshold1.floatValue = threshold2.floatValue
        }

        mutatedAppTheme.value = makeAppTheme(threshold1.floatValue, threshold2.floatValue, threshold3.floatValue)
    }

    LaunchedEffect(threshold3.floatValue) {
        if (threshold3.floatValue < threshold2.floatValue) {
            threshold2.floatValue = threshold3.floatValue
        }

        mutatedAppTheme.value = makeAppTheme(threshold1.floatValue, threshold2.floatValue, threshold3.floatValue)
    }

    ContentWithBottomButtonPair(navController, onSave = {
        configManager.solarRangeDefinitions = SolarRangeDefinitions(
            threshold1 = threshold1.floatValue.toDouble(),
            threshold2 = threshold2.floatValue.toDouble(),
            threshold3 = threshold3.floatValue.toDouble()
        )
        Toast.makeText(context, context.getString(R.string.thresholds_were_saved), Toast.LENGTH_LONG).show()
    }, { innerModifier ->
        SettingsPage(innerModifier) {
            ThresholdView(
                mutableStateValue = threshold1,
                title = stringResource(R.string.low_threshold),
                description = stringResource(R.string.below_this_amount_the_sun_will_be_yellow)
            )
            ThresholdView(
                mutableStateValue = threshold2,
                title = stringResource(R.string.medium_threshold),
                description = stringResource(R.string.between_low_and_medium_the_sun_will_be_yellow_and_glowing)
            )
            ThresholdView(
                mutableStateValue = threshold3,
                title = stringResource(R.string.high_threshold),
                description = stringResource(R.string.between_medium_and_high_the_sun_will_be_orange_and_glowing_above_high_the_sun_will_be_red_and_glowing)
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
                        valueRange = 0.0f..threshold3.floatValue + 0.5f,
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

            ESButton({
                threshold1.floatValue = 1.0f
                threshold2.floatValue = 2.0f
                threshold3.floatValue = 3.0f
            }) {
                Text(stringResource(R.string.restore_defaults))
            }

            SettingsBottomSpace()
        }
    }, modifier)
}

fun makeAppTheme(threshold1: Float, threshold2: Float, threshold3: Float): AppTheme {
    return AppTheme.demo()
        .copy(
            solarRangeDefinitions = SolarRangeDefinitions(
                threshold1 = threshold1.toDouble(),
                threshold2 = threshold2.toDouble(),
                threshold3 = threshold3.toDouble()
            ),
            showFinancialSummary = false
        )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 1000)
@Composable
fun SolarBandingSettingsPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SolarBandingSettingsView(
            NavHostController(LocalContext.current),
            configManager = FakeConfigManager(),
 Modifier
        )
    }
}