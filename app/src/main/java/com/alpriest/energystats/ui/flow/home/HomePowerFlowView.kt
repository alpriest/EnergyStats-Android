package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun HomePowerFlowView(amount: Double, modifier: Modifier, themeStream: MutableStateFlow<AppTheme>, position: PowerFlowLinePosition = PowerFlowLinePosition.MIDDLE) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(modifier = Modifier.weight(1f)) {
            PowerFlowView(
                amount = amount,
                themeStream = themeStream,
                position = position
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePowerFlowViewPreview() {
    EnergyStatsTheme {
        Box(modifier = Modifier.height(300.dp)) {
            HomePowerFlowView(
                amount = 1.0,
                modifier = Modifier,
                themeStream = MutableStateFlow(AppTheme.preview())
            )
        }
    }
}

fun AppTheme.Companion.preview(
    useLargeDisplay: Boolean = false,
    useColouredLines: Boolean = true,
    showBatteryTemperature: Boolean = true,
    showBatteryEstimate: Boolean = true,
    showSunnyBackground: Boolean = true,
    decimalPlaces: Int = 2,
    showUsableBatteryOnly: Boolean = false,
    showTotalYield: Boolean = true,
    selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode = SelfSufficiencyEstimateMode.Off
): AppTheme {
    return AppTheme(
        useLargeDisplay = useLargeDisplay,
        useColouredLines = useColouredLines,
        showBatteryTemperature = showBatteryTemperature,
        showBatteryEstimate = showBatteryEstimate,
        showSunnyBackground = showSunnyBackground,
        decimalPlaces = decimalPlaces,
        showUsableBatteryOnly = showUsableBatteryOnly,
        showTotalYield = showTotalYield,
        selfSufficiencyEstimateMode = selfSufficiencyEstimateMode
    )
}