package com.alpriest.energystats.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.shared.ui.BatteryView
import com.alpriest.energystats.shared.ui.PowerFlowNegative
import com.alpriest.energystats.shared.ui.PowerFlowNeutral
import com.alpriest.energystats.shared.ui.PowerFlowPositive

@Composable
fun BatteryPowerView(
    iconScale: IconScale,
    amount: Double?,
    chargeLevel: Double?
) {
    val compareAmount = amount ?: 0.0
    val fillColor = if (compareAmount > 0.0) PowerFlowPositive else if (compareAmount < 0.0) PowerFlowNegative else PowerFlowNeutral

    FullPageStatusView(
        iconScale = iconScale,
        icon = {
            Box {
                val batteryModifier = Modifier
                    .height(iconScale.iconHeight())
                    .width(iconScale.iconHeight() * 1.25f)
                val clampedChargeLevel = chargeLevel?.coerceIn(0.0, 1.0) ?: 0.0

                BatteryView(
                    modifier = batteryModifier,
                    foregroundColor = Color.Black,
                    backgroundColor = PowerFlowNeutral
                )

                BatteryView(
                    modifier = batteryModifier.drawWithContent {
                        val fillHeight = size.height * (1.0f - clampedChargeLevel.toFloat())

                        clipRect(top = 0f, left = 0f, right = size.width, bottom = fillHeight) {
                            // This will be clipped
                        }
                        // Then we define the part to show by clipping the inverse
                        clipRect(top = fillHeight, left = 0f, right = size.width, bottom = size.height) {
                            // This part of the original content will be drawn
                            this@drawWithContent.drawContent()
                        }
                    },
                    foregroundColor = Color.Black,
                    backgroundColor = fillColor
                )
            }
        },
        line1 = { textStyle ->
            RedactedKW(amount, textStyle)
        },
        line2 = { textStyle ->
            RedactedPercentage(chargeLevel, textStyle)
        }
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun BatterPowerViewPreviewRound() {
    BatteryPowerView(IconScale.LARGE, 0.5, 0.88)
}
