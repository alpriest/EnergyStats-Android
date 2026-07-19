package com.alpriest.energystats.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.helpers.kWh
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.ui.SunIconWithThresholds

@Composable
fun SolarPowerView(
    iconScale: IconScale,
    solarAmount: Double?,
    solarRangeDefinitions: SolarRangeDefinitions,
    total: Double?
) {
    FullPageStatusView(
        iconScale = iconScale,
        icon = {
            SunIconWithThresholds(solarAmount ?: 8.8, iconHeight = iconScale.iconHeight(), solarRangeDefinitions, true)
        },
        line1 = { textStyle ->
            kWWithPlaceholder(solarAmount, textStyle)
        },
    ) { textStyle ->
        if (iconScale == IconScale.LARGE && total != null) {
            TextWithPlaceholder(stringResource(R.string.today, total.kWh(1)), textStyle)
        } else {
            Text(" ", style = textStyle)
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SolarPowerViewPreviewRound() {
    SolarPowerView(IconScale.LARGE, 1.0, SolarRangeDefinitions.defaults, 17.8)
}
