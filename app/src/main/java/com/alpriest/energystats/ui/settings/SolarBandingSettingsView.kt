package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import com.alpriest.energystats.models.kWh
import com.alpriest.energystats.ui.flow.home.EarningsViewModel
import com.alpriest.energystats.ui.flow.home.SolarPowerFlow
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun BreakpointView(mutableStateValue: MutableState<Float>, title: String) {
    val value = mutableStateValue.value

    SettingsColumnWithChild {
        Text(title)

        Row {
            Slider(
                value = value,
                onValueChange = { mutableStateValue.value = it },
                valueRange = 0.1f..5.0f,
                modifier = Modifier.weight(1.0f)
            )

            Text(
                value.toDouble().kWh(3),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.2f)
            )
        }
    }
}

@Composable
fun SolarBandingSettingsView(navController: NavHostController) {
    val amount = remember { mutableFloatStateOf(2.0f) }
    val breakpoint1 = remember { mutableFloatStateOf(1.0f) }
    val breakpoint2 = remember { mutableFloatStateOf(2.0f) }
    val breakpoint3 = remember { mutableFloatStateOf(3.0f) }

    SettingsPage {
        BreakpointView(mutableStateValue = breakpoint1, title = "Low threshold")
        BreakpointView(mutableStateValue = breakpoint2, title = "Medium threshold")
        BreakpointView(mutableStateValue = breakpoint3, title = "High threshold")

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
                    themeStream = MutableStateFlow(AppTheme.preview().copy(showTotalYield = false, showEstimatedEarnings = false))
                )

                Slider(
                    value = amount.value,
                    onValueChange = { amount.value = it },
                    valueRange = 0.1f..5.0f
                )
            }

            Text(
                "Drag the slider above to see how your solar flow will display when generating different levels of power.",
                color = MaterialTheme.colors.onSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        CancelSaveButtonView(navController, onSave = { /* TODO */ })
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun SolarBandingSettingsPreview() {
    EnergyStatsTheme(darkTheme = false) {
        SolarBandingSettingsView(NavHostController(LocalContext.current))
    }
}