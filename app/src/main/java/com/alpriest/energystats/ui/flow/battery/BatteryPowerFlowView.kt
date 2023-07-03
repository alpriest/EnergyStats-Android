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
import com.alpriest.energystats.models.asPercent
import com.alpriest.energystats.models.kWh
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun BatteryPowerFlow(
    viewModel: BatteryPowerViewModel,
    modifier: Modifier = Modifier,
    themeStream: MutableStateFlow<AppTheme>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        PowerFlowView(
            amount = viewModel.chargePowerkWH,
            themeStream = themeStream,
            position = PowerFlowLinePosition.LEFT,
            useColouredLines = true,
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Composable
fun BatteryIconView(
    viewModel: BatteryPowerViewModel,
    themeStream: MutableStateFlow<AppTheme>,
    iconHeight: Dp,
    modifier: Modifier = Modifier
) {
    var percentage by remember { mutableStateOf(true) }
    val fontSize: TextUnit = themeStream.collectAsState().value.fontSize()
    val showBatteryTemperature = themeStream.collectAsState().value.showBatteryTemperature
    val decimalPlaces = themeStream.collectAsState().value.decimalPlaces
    val showBatteryEstimate = themeStream.collectAsState().value.showBatteryEstimate

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        BatteryView(
            modifier = Modifier
                .height(iconHeight)
                .width(iconHeight * 1.25f)
                .padding(5.dp)
        )

        Box(modifier = Modifier.clickable { percentage = !percentage }) {
            Row {
                if (percentage) {
                    Text(
                        viewModel.batteryStateOfCharge().asPercent(),
                        fontSize = fontSize
                    )
                } else {
                    Text(
                        viewModel.batteryStoredCharge().kWh(decimalPlaces),
                        fontSize = fontSize
                    )
                }
            }
        }

        if (showBatteryTemperature) {
            Text(
                viewModel.temperature.asTemperature(),
                fontSize = fontSize
            )
        }

        if (showBatteryEstimate) {
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

fun Double.asTemperature(): String {
    return "${this}°C"
}

@Composable
fun duration(estimate: BatteryCapacityEstimate): String {
    val text = stringResource(estimate.stringId)
    val mins = stringResource(R.string.mins)
    val hour = stringResource(R.string.hour)
    val hours = stringResource(R.string.hours)

    return when (estimate.duration) {
        in 0..60 -> "$text ${estimate.duration} $mins"
        in 61..119 -> "$text ${estimate.duration / 60} $hour"
        in 120..1440 -> "$text ${Math.round(estimate.duration / 60.0)} $hours"
        else -> "$text ${Math.round(estimate.duration / 1444.0)} days"
    }
}

@Preview(showBackground = true)
@Composable
fun BatteryPowerFlowViewPreview() {
    EnergyStatsTheme {
        BatteryPowerFlow(
            viewModel = BatteryPowerViewModel(
                FakeConfigManager(),
                actualStateOfCharge = 0.25,
                chargePowerkWH = -0.5,
                temperature = 13.6,
                residual = 5678
            ),
            modifier = Modifier,
            themeStream = MutableStateFlow(AppTheme.preview())
        )
    }
}