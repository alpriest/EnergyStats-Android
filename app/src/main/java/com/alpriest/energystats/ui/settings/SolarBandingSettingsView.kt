package com.alpriest.energystats.ui.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.kWh
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.EarningsViewModel
import com.alpriest.energystats.ui.flow.home.SolarPowerFlow
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.SolarRangeDefinitions
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ThresholdView(mutableStateValue: MutableState<Float>, title: String, description: String) {
    val value = mutableStateValue.value

    SettingsColumnWithChild {
        SettingsTitleView(title)

        Row {
            Slider(
                value = value,
                onValueChange = { mutableStateValue.value = it },
                valueRange = 0.1f..5.0f,
                modifier = Modifier.weight(1.0f),
                steps = 48,
                colors = SliderDefaults.colors(
                    activeTickColor = colors.primary,
                    inactiveTickColor = colors.background,
                    activeTrackColor = colors.primary,
                    inactiveTrackColor = colors.background,
                    thumbColor = colors.primary
                )
            )

            Text(
                value.toDouble().kWh(3),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.2f)
            )
        }

        Text(description)
    }
}

@Composable
fun SolarBandingSettingsView(navController: NavHostController, configManager: ConfigManaging) {
    val amount = remember { mutableFloatStateOf(2.0f) }
    val threshold1 = remember { mutableFloatStateOf(configManager.themeStream.value.solarRangeDefinitions.threshold1.toFloat()) }
    val threshold2 = remember { mutableFloatStateOf(configManager.themeStream.value.solarRangeDefinitions.threshold2.toFloat()) }
    val threshold3 = remember { mutableFloatStateOf(configManager.themeStream.value.solarRangeDefinitions.threshold3.toFloat()) }
    val mutatedAppTheme = remember { MutableStateFlow(configManager.themeStream.value) }
    val context = LocalContext.current

    LaunchedEffect(threshold1.value) {
        if (threshold1.value > threshold2.value) {
            threshold2.value = threshold1.value
        }

        mutatedAppTheme.value = makeAppTheme(threshold1.value, threshold2.value, threshold3.value)
    }

    LaunchedEffect(threshold2.value) {
        if (threshold2.value > threshold3.value) {
            threshold3.value = threshold2.value
        }

        if (threshold2.value < threshold1.value) {
            threshold1.value = threshold2.value
        }

        mutatedAppTheme.value = makeAppTheme(threshold1.value, threshold2.value, threshold3.value)
    }

    LaunchedEffect(threshold3.value) {
        if (threshold3.value < threshold2.value) {
            threshold2.value = threshold3.value
        }

        mutatedAppTheme.value = makeAppTheme(threshold1.value, threshold2.value, threshold3.value)
    }

    ContentWithBottomButtons(navController, onSave = {
        configManager.solarRangeDefinitions = SolarRangeDefinitions(
            threshold1 = threshold1.floatValue.toDouble(),
            threshold2 = threshold2.floatValue.toDouble(),
            threshold3 = threshold3.floatValue.toDouble()
        )
        Toast.makeText(context, "Thresholds were saved", Toast.LENGTH_LONG).show()
    }) {
        SettingsPage {
            ThresholdView(mutableStateValue = threshold1, title = stringResource(R.string.low_threshold), description = stringResource(R.string.below_this_amount_the_sun_will_be_yellow))
            ThresholdView(mutableStateValue = threshold2, title = stringResource(R.string.medium_threshold), description = stringResource(R.string.between_low_and_medium_the_sun_will_be_yellow_and_glowing))
            ThresholdView(
                mutableStateValue = threshold3,
                title = stringResource(R.string.high_threshold),
                description = stringResource(R.string.between_medium_and_high_the_sun_will_be_orange_and_glowing_above_high_the_sun_will_be_red_and_glowing)
            )

            SettingsColumnWithChild {
                SettingsTitleView("Example")

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SolarPowerFlow(
                        amount = amount.value.toDouble(),
                        todaysGeneration = 1.0,
                        earnings = EarningsViewModel.preview(),
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp),
                        iconHeight = 40.dp,
                        themeStream = mutatedAppTheme
                    )

                    Slider(
                        value = amount.value,
                        onValueChange = { amount.value = it },
                        valueRange = 0.0f..threshold3.value + 0.5f,
                        colors = SliderDefaults.colors(
                            activeTickColor = colors.primary,
                            inactiveTickColor = colors.background,
                            activeTrackColor = colors.primary,
                            inactiveTrackColor = colors.background,
                            thumbColor = colors.primary
                        )
                    )
                }

                Text(
                    "Drag the slider above to see how your solar flow will display when generating different levels of power.",
                    color = colors.onSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            SettingsNavButton(
                stringResource(R.string.restore_defaults),
                disclosureIcon = null
            ) {
                threshold1.value = 1.0f
                threshold2.value = 2.0f
                threshold3.value = 3.0f
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun ContentWithBottomButtons(navController: NavController, onSave: suspend () -> Unit, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()

        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Divider(
                    color = Color.LightGray, modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
                CancelSaveButtonView(
                    navController,
                    onSave = onSave,
                    modifier = Modifier
                        .background(colors.surface)
                        .padding(12.dp)
                )
            }
        }
    }
}

fun makeAppTheme(threshold1: Float, threshold2: Float, threshold3: Float): AppTheme {
    return AppTheme.preview()
        .copy(
            solarRangeDefinitions = SolarRangeDefinitions(
                threshold1 = threshold1.toDouble(),
                threshold2 = threshold2.toDouble(),
                threshold3 = threshold3.toDouble()
            ),
            showTotalYield = false,
            showEstimatedEarnings = false
        )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 400)
@Composable
fun SolarBandingSettingsPreview() {
    EnergyStatsTheme(darkTheme = false) {
        SolarBandingSettingsView(
            NavHostController(LocalContext.current),
            configManager = FakeConfigManager()
        )
    }
}