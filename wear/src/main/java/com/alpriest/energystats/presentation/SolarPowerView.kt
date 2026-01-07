package com.alpriest.energystats.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.ui.SunIconWithThresholds

@Composable
fun SolarPowerView(
    iconScale: IconScale,
    solarAmount: Double?,
    solarRangeDefinitions: SolarRangeDefinitions
) {
    FullPageStatusView(
        iconScale = iconScale,
        icon = {
            SunIconWithThresholds(solarAmount ?: 8.8, iconHeight = iconScale.iconHeight(), solarRangeDefinitions, true)
        },
        line1 = { textStyle ->
            RedactedKW(solarAmount, textStyle)
        },
    ) { textStyle ->
        Text(" ", style = textStyle)
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SolarPowerViewPreviewRound() {
    SolarPowerView(IconScale.LARGE, 1.0, SolarRangeDefinitions.defaults)
}
