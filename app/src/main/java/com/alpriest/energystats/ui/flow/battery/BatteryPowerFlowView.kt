package com.alpriest.energystats.ui.flow.battery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.models.asPercent
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun BatteryPowerFlow(
    viewModel: BatteryPowerViewModel,
    iconHeight: Dp,
    modifier: Modifier = Modifier,
    themeStream: MutableStateFlow<AppTheme>
) {
    var percentage by remember { mutableStateOf(true) }
    val fontSize: TextUnit = themeStream.collectAsState().value.fontSize()
    val showBatteryTemperature = themeStream.collectAsState().value.showBatteryTemperature

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(modifier = Modifier.weight(1f)) {
            PowerFlowView(
                amount = viewModel.batteryChargePowerkWH,
                themeStream = themeStream,
                position = PowerFlowLinePosition.LEFT,
                useColouredLines = true
            )
        }

        BatteryView(
            modifier = Modifier
                .height(iconHeight)
                .width(iconHeight * 1.25f)
                .padding(5.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
        ) {
            Box(modifier = Modifier.clickable { percentage = !percentage }) {
                Row {
                    if (percentage) {
                        Text(
                            viewModel.batteryStateOfCharge.asPercent(),
                            fontSize = fontSize
                        )
                    } else {
                        Text(
                            viewModel.batteryCapacity,
                            fontSize = fontSize
                        )
                    }
                }
            }

            if (showBatteryTemperature) {
                Text(
                    viewModel.batteryTemperature.asTemperature(),
                    fontSize = fontSize
                )
            }

            viewModel.batteryExtra?.let {
                Text(
                    duration(estimate = it),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    color = Color.Gray,
                    fontSize = fontSize
                )
            }
        }
    }
}

private fun Double.asTemperature(): String {
    return "${this}°C"
}

@Composable
private fun duration(estimate: BatteryCapacityEstimate): String {
    val text = stringResource(estimate.stringId)
    val mins = stringResource(R.string.mins)
    val hour = stringResource(R.string.hour)
    val hours = stringResource(R.string.hours)

    return when (estimate.duration) {
        in 0..60 -> "$text $estimate.duration $mins"
        in 61..119 -> "$text ${estimate.duration / 60} $hour"
        else -> "$text ${Math.round(estimate.duration / 60.0)} $hours"
    }
}

@Preview(showBackground = true)
@Composable
fun BatteryPowerFlowViewPreview() {
    EnergyStatsTheme {
        BatteryPowerFlow(
            viewModel = BatteryPowerViewModel(
                FakeConfigManager(),
                batteryStateOfCharge = 0.25,
                batteryChargePowerkWH = -0.5,
                batteryTemperature = 13.6
            ),
            iconHeight = 40.dp,
            modifier = Modifier,
            themeStream = MutableStateFlow(AppTheme(useLargeDisplay = true, useColouredLines = true, showBatteryTemperature = false))
        )
    }
}