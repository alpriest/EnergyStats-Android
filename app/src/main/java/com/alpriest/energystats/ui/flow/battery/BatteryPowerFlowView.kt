package com.alpriest.energystats.ui.flow.battery

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.asPercent
import com.alpriest.energystats.models.kWh
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.ui.flow.LineOrientation
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.settings.ColorThemeMode
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
            modifier = Modifier.fillMaxHeight(),
            orientation = LineOrientation.VERTICAL
        )
    }
}

@Composable
fun isDarkMode(themeStream: MutableStateFlow<AppTheme>): Boolean {
    return when (themeStream.collectAsState().value.colorTheme) {
        ColorThemeMode.Light -> false
        ColorThemeMode.Dark -> true
        ColorThemeMode.Auto -> isSystemInDarkTheme()
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
    val fontSize = themeStream.collectAsState().value.fontSize()
    val smallFontSize = themeStream.collectAsState().value.smallFontSize()
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
                .width(iconHeight * 1.25f),
            isDarkMode = isDarkMode(themeStream)
        )

        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Row {
                BatteryPercentage({ percentage = !percentage }, percentage, viewModel, fontSize, decimalPlaces)

                if (showBatteryTemperature) {
                    Text(
                        viewModel.temperature.asTemperature(),
                        fontSize = fontSize,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        } else {
            BatteryPercentage({ percentage = !percentage }, percentage, viewModel, fontSize, decimalPlaces)

            if (showBatteryTemperature) {
                Text(
                    viewModel.temperature.asTemperature(),
                    fontSize = fontSize
                )
            }
        }

        if (showBatteryEstimate) {
            viewModel.batteryExtra?.let {
                Text(
                    duration(estimate = it),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    color = Color.Gray,
                    fontSize = smallFontSize
                )
            }
        }
    }
}

@Composable
private fun BatteryPercentage(
    onClick: () -> Unit,
    percentage: Boolean,
    viewModel: BatteryPowerViewModel,
    fontSize: TextUnit,
    decimalPlaces: Int
) {
    Box(modifier = Modifier.clickable {onClick() }) {
        Row {
            if (percentage) {
                Text(
                    viewModel.batteryStateOfCharge().asPercent(),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    viewModel.batteryStoredChargekWh().kWh(decimalPlaces),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
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
        in 1441 .. 2880 -> "$text ${Math.round(estimate.duration / 1440.0)} day"
        else -> "$text ${Math.round(estimate.duration / 1440.0)} days"
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 300)
@Composable
fun BatteryPowerFlowViewPreview() {
    EnergyStatsTheme {
        BatteryIconView(
            viewModel = BatteryPowerViewModel(
                FakeConfigManager(),
                actualStateOfCharge = 0.25,
                chargePowerkWH = -0.5,
                temperature = 13.6,
                residual = 5678,
                hasError = false
            ),
            themeStream = MutableStateFlow(AppTheme.preview(showBatteryTemperature = true, showBatteryEstimate = true)),
            iconHeight = 24.dp
        )
    }
}