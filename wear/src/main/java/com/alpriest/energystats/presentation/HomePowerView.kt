package com.alpriest.energystats.presentation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.shared.ui.HouseView

@Composable
fun HomePowerView(
    iconScale: IconScale,
    amount: Double?
) {
    FullPageStatusView(
        iconScale = iconScale,
        icon = {
            HouseView(
                modifier = Modifier
                    .height(iconScale.iconHeight())
                    .width(iconScale.iconHeight() * 1.3f),
                Color.Black,
                Color.White
            )
        },
        line1 = {
            RedactedKW(amount)
        },
    ) {
        Text(" ")
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun HomePowerViewPreviewRound() {
    HomePowerView(IconScale.LARGE, 1.0)
}
