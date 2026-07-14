package com.alpriest.energystats.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.shared.helpers.kWh
import com.alpriest.energystats.shared.ui.PowerFlowNegative
import com.alpriest.energystats.shared.ui.PowerFlowNeutral
import com.alpriest.energystats.shared.ui.PowerFlowPositive
import com.alpriest.energystats.shared.ui.PylonView
import com.alpriest.energystats.shared.ui.roundedToString

@Composable
fun GridPowerView(
    iconScale: IconScale,
    amount: Double?,
    totalImport: Double?,
    totalExport: Double?
) {
    val compareAmount = amount ?: 0.0
    val fillColor = if (compareAmount > 0.0) PowerFlowPositive else if (compareAmount < 0.0) PowerFlowNegative else PowerFlowNeutral

    FullPageStatusView(
        iconScale = iconScale,
        icon = {
            PylonView(
                modifier = Modifier
                    .height(iconScale.iconHeight())
                    .width(iconScale.iconHeight() * 1.1f),
                color = fillColor,
                strokeWidth = iconScale.strokeWidth()
            )
        },
        line1 = { textStyle ->
            kWWithPlaceholder(amount, textStyle)
        },
        line2 = { textStyle ->
            if (totalImport != null && totalExport != null) {
                Row {
                    TextWithPlaceholder(totalImport.roundedToString(1), textStyle.copy(color = PowerFlowNegative))
                    Text(
                        text = "/",
                        style = textStyle.copy(color = PowerFlowNeutral)
                    )
                    if (iconScale == IconScale.LARGE) {
                        TextWithPlaceholder(totalExport.kWh(1), textStyle.copy(color = PowerFlowPositive))
                    } else {
                        TextWithPlaceholder(totalExport.roundedToString(1), textStyle.copy(color = PowerFlowPositive))
                    }
                }
            } else {
                Text(" ")
            }
        }
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun GridPowerViewPreviewRound() {
    GridPowerView(IconScale.LARGE, 1.0, 2.2, 3.4)
}
