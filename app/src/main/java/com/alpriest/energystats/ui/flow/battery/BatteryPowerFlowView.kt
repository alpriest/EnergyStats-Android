package com.alpriest.energystats.ui.flow.battery

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpriest.energystats.R
import com.alpriest.energystats.models.BatteryTemperatures
import com.alpriest.energystats.models.asPercent
import com.alpriest.energystats.models.kWh
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.ui.flow.LineOrientation
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
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
    return isDarkMode(colorTheme = themeStream.collectAsStateWithLifecycle().value.colorTheme)
}

@Composable
fun isDarkMode(colorTheme: ColorThemeMode): Boolean {
    return when (colorTheme) {
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
    val percentage = themeStream.collectAsStateWithLifecycle().value.showBatterySOCAsPercentage
    val fontSize = themeStream.collectAsStateWithLifecycle().value.fontSize()
    val smallFontSize = themeStream.collectAsStateWithLifecycle().value.smallFontSize()
    val showBatteryTemperature = themeStream.collectAsStateWithLifecycle().value.showBatteryTemperature
    val decimalPlaces = themeStream.collectAsStateWithLifecycle().value.decimalPlaces
    val showBatteryEstimate = themeStream.collectAsStateWithLifecycle().value.showBatteryEstimate

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
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BatteryStateOfChargeView({ viewModel.setBatteryAsPercentage(it) }, percentage, viewModel, fontSize, decimalPlaces)

                if (showBatteryTemperature) {
                    viewModel.temperatures.forEach {
                        Text(
                            it.asTemperature(),
                            fontSize = fontSize
                        )
                    }
                }
            }
        } else {
            BatteryStateOfChargeView({ viewModel.setBatteryAsPercentage(it) }, percentage, viewModel, fontSize, decimalPlaces)

            if (showBatteryTemperature) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.temperatures.forEach {
                        Text(
                            it.asTemperature(),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (showBatteryEstimate) {
            viewModel.batteryExtra?.let {
                Text(
                    duration(estimate = it) + (if (viewModel.showUsableBatteryOnly) "*" else ""),
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
private fun BatteryStateOfChargeView(
    onClick: (Boolean) -> Unit,
    percentage: Boolean,
    viewModel: BatteryPowerViewModel,
    fontSize: TextUnit,
    decimalPlaces: Int
) {
    Box(modifier = Modifier.clickable {
        onClick(!percentage)
    }) {
        Row {
            if (percentage) {
                Text(
                    viewModel.batteryStateOfCharge().asPercent() + (if (viewModel.showUsableBatteryOnly) "*" else ""),
                    fontSize = fontSize
                )
            } else {
                Text(
                    viewModel.batteryStoredChargekWh().kWh(decimalPlaces) + (if (viewModel.showUsableBatteryOnly) "*" else ""),
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
    val day = stringResource(R.string.day)
    val days = stringResource(R.string.days)

    return when (estimate.duration) {
        in 0..60 -> "$text ${estimate.duration} $mins"
        in 61..119 -> "$text ${estimate.duration / 60} $hour"
        in 120..1440 -> "$text ${Math.round(estimate.duration / 60.0)} $hours"
        else -> {
            val dayNumber = Math.round(estimate.duration / 1440.0)
            if (dayNumber == 1L) {
                "$text $dayNumber $day"
            } else {
                "$text $dayNumber $days"
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 300)
@Preview(name = "Dark Mode", showBackground = true, widthDp = 300, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BatteryPowerFlowViewPreview() {
    val configManager = FakeConfigManager()
    configManager.showUsableBatteryOnly = true

    EnergyStatsTheme {
        BatteryIconView(
            viewModel = BatteryPowerViewModel(
                configManager,
                actualStateOfCharge = 0.25,
                chargePowerkWH = 0.5,
                batteryTemperatures = BatteryTemperatures(13.6, null, null),
                residual = 5678
            ),
            themeStream = MutableStateFlow(AppTheme.demo(showBatteryTemperature = true, showBatteryEstimate = true)),
            iconHeight = 24.dp
        )
    }
}