package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.demo
import com.alpriest.energystats.shared.ui.SunIconWithThresholds
import com.alpriest.energystats.ui.flow.LineOrientation
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SolarPowerFlow(amount: Double, modifier: Modifier, iconHeight: Dp, appSettingsStream: StateFlow<AppSettings>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val theme = appSettingsStream.collectAsState().value
        SunIconWithThresholds(amount, iconHeight, theme.solarRangeDefinitions, isDarkMode(theme.colorTheme))

        Spacer(modifier = Modifier.height(4.dp))

        PowerFlowView(
            amount = amount,
            appSettingsStream = appSettingsStream,
            position = PowerFlowLinePosition.NONE,
            orientation = LineOrientation.VERTICAL
        )
    }
}

@Preview(showBackground = true, widthDp = 500)
@Composable
fun SolarPowerFlowViewPreview() {
    val amount = remember { mutableFloatStateOf(0.0f) }

    EnergyStatsTheme {
        Column {
            SolarPowerFlow(
                amount = amount.value.toDouble(),
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                iconHeight = 40.dp,
                appSettingsStream = MutableStateFlow(AppSettings.demo().copy(showFinancialSummary = false))
            )

            Slider(
                value = amount.value,
                onValueChange = { amount.value = it },
                valueRange = 0.1f..5.0f
            )

            Row(
                modifier = Modifier
                    .height(300.dp)
                    .wrapContentWidth()
            ) {
                SolarPowerFlow(
                    amount = 0.0,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    appSettingsStream = MutableStateFlow(AppSettings.demo())
                )

                SolarPowerFlow(
                    amount = 0.5,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    appSettingsStream = MutableStateFlow(AppSettings.demo())
                )

                SolarPowerFlow(
                    amount = 1.5,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    appSettingsStream = MutableStateFlow(AppSettings.demo())
                )

                SolarPowerFlow(
                    amount = 2.5,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    appSettingsStream = MutableStateFlow(AppSettings.demo())
                )

                SolarPowerFlow(
                    amount = 3.5,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    appSettingsStream = MutableStateFlow(AppSettings.demo())
                )
            }
        }
    }
}
