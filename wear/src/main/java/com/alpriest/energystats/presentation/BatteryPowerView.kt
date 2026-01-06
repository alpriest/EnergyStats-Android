package com.alpriest.energystats.presentation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.shared.ui.BatteryView

@Composable
fun BatteryPowerView(
    iconScale: IconScale,
    amount: Double?,
    chargeLevel: Double?
) {
    FullPageStatusView(
        iconScale = iconScale,
        icon = {
            BatteryView(
                modifier = Modifier
                    .height(iconScale.iconHeight())
                    .width(iconScale.iconHeight() * 1.25f),
                Color.Black,
                Color.White
            )
        },
        line1 = {
            RedactedKW(amount)
        },
        line2 = {
            RedactedPercentage(chargeLevel)
        }
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun BatterPowerViewPreviewRound() {
    BatteryPowerView(IconScale.LARGE, 1.0, 0.4)
}
