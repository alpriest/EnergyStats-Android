package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpriest.energystats.models.kWh
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
fun SolarBandingSettingsView(navController: NavHostController, appTheme: MutableStateFlow<AppTheme>) {
    val amount = remember { mutableFloatStateOf(2.0f) }
    val threshold1 = remember { mutableFloatStateOf(1.0f) }
    val threshold2 = remember { mutableFloatStateOf(2.0f) }
    val threshold3 = remember { mutableFloatStateOf(3.0f) }
    val mutatedAppTheme = appTheme

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

    SettingsPage {
        ThresholdView(mutableStateValue = threshold1, title = "Low threshold", description = "Below this amount the sun will be yellow.")
        ThresholdView(mutableStateValue = threshold2, title = "Medium threshold", description = "Between low and medium the sun will be yellow and glowing.")
        ThresholdView(
            mutableStateValue = threshold3,
            title = "High threshold",
            description = "Between medium and high the sun will be orange and glowing. Above high the sun will be red and glowing."
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
                color = MaterialTheme.colors.onSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        SettingsNavButton(
            "Restore defaults",
            disclosureIcon = null
        ) {
            // TODO
        }

        CancelSaveButtonView(navController, onSave = { /* TODO */ })
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
            appTheme = MutableStateFlow(AppTheme.preview().copy(showTotalYield = false, showEstimatedEarnings = false))
        )
    }
}