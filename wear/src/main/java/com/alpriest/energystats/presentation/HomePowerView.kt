package com.alpriest.energystats.presentation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.helpers.kWh
import com.alpriest.energystats.shared.ui.HouseView

@Composable
fun HomePowerView(
    iconScale: IconScale,
    amount: Double?,
    total: Double?
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
        line1 = { textStyle ->
            kWWithPlaceholder(amount, textStyle)
        },
        line2 = { textStyle ->
            total?.let {
                TextWithPlaceholder(stringResource(R.string.today, total.kWh(1)), textStyle)
            } ?: Text(" ", style = textStyle)
        }
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun HomePowerViewPreviewRound() {
    HomePowerView(IconScale.LARGE, 1.0, 3.4)
}
