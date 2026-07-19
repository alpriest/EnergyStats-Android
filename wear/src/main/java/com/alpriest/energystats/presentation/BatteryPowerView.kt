package com.alpriest.energystats.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.shared.helpers.kWh
import com.alpriest.energystats.shared.ui.BatteryView
import com.alpriest.energystats.shared.ui.PowerFlowNegative
import com.alpriest.energystats.shared.ui.PowerFlowNeutral
import com.alpriest.energystats.shared.ui.PowerFlowPositive
import com.alpriest.energystats.shared.ui.roundedToString

@Composable
fun BatteryPowerView(
    iconScale: IconScale,
    amount: Double?,
    chargeLevel: Double?,
    totalCharge: Double?,
    totalDischarge: Double?
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
            Line1Text(iconScale, amount, chargeLevel, textStyle)
        },
        line2 = { textStyle ->
            Line2Text(iconScale, chargeLevel, totalCharge, totalDischarge, textStyle)
        }
    )
}

@Composable
private fun Line1Text(iconScale: IconScale, amount: Double?, chargeLevel: Double?, textStyle: TextStyle) {
    return when (iconScale) {
        IconScale.SMALL -> kWWithPlaceholder(amount, textStyle)
        IconScale.LARGE -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            kWWithPlaceholder(amount, textStyle)
            RedactedPercentage(chargeLevel, textStyle)
        }
    }
}

@Composable
private fun Line2Text(iconScale: IconScale, chargeLevel: Double?, totalCharge: Double?, totalDischarge: Double?, textStyle: TextStyle) {
    return when (iconScale) {
        IconScale.SMALL -> RedactedPercentage(chargeLevel, textStyle)
        IconScale.LARGE -> Row {
            if (totalCharge != null && totalDischarge != null) {
                TextWithPlaceholder(totalCharge?.roundedToString(1), textStyle.copy(color = PowerFlowNegative))
                Text(
                    text = "/",
                    style = textStyle.copy(color = PowerFlowNeutral)
                )
                TextWithPlaceholder(totalDischarge?.kWh(1), textStyle.copy(color = PowerFlowPositive))
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun BatterPowerViewPreviewRound() {
    BatteryPowerView(IconScale.LARGE, 0.5, 0.88, totalCharge = 1.2, totalDischarge = 3.4)
}
