package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.SunIcon
import com.alpriest.energystats.ui.flow.LineOrientation
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.battery.iconBackgroundColor
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Sunny
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SolarPowerFlow(amount: Double, modifier: Modifier, iconHeight: Dp, themeStream: MutableStateFlow<AppTheme>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val glowing: Boolean
        val sunColor: Color
        var glowColor: Color = Color.Transparent
        val orange = Color(0xFFF2A53D)
        val theme by themeStream.collectAsState()

        if (amount >= 0.001f && amount < theme.solarRangeDefinitions.threshold1) {
            glowing = false
            sunColor = Sunny
        } else if (amount >= theme.solarRangeDefinitions.threshold1 && amount < theme.solarRangeDefinitions.threshold2) {
            glowing = true
            glowColor = Sunny.copy(alpha = 0.4f)
            sunColor = Sunny
        } else if (amount >= theme.solarRangeDefinitions.threshold2 && amount < theme.solarRangeDefinitions.threshold3) {
            glowing = true
            glowColor = Sunny.copy(alpha = 0.9f)
            sunColor = orange
        } else if (amount >= theme.solarRangeDefinitions.threshold3 && amount < 500f) {
            glowing = true
            glowColor = orange
            sunColor = Color.Red
        } else {
            glowing = false
            sunColor = iconBackgroundColor(isDarkMode(themeStream))
            glowColor = Color.Transparent
        }

        SunIcon(
            size = iconHeight,
            color = sunColor,
            glowColor = if (glowing) glowColor else null,
            modifier = Modifier.requiredSize(width = iconHeight, height = iconHeight)
        )

        Spacer(modifier = Modifier.height(4.dp))

        PowerFlowView(
            amount = amount,
            themeStream = themeStream,
            position = PowerFlowLinePosition.NONE,
            orientation = LineOrientation.VERTICAL
        )
    }
}

@Preview(showBackground = true, widthDp = 500)
@Composable
fun SolarPowerFlowViewPreview() {
    val amount = remember { mutableFloatStateOf(0.0f) }

    EnergyStatsTheme {
        Column {
            SolarPowerFlow(
                amount = amount.value.toDouble(),
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                iconHeight = 40.dp,
                themeStream = MutableStateFlow(AppTheme.demo().copy(showFinancialSummary = false))
            )

            Slider(
                value = amount.value,
                onValueChange = { amount.value = it },
                valueRange = 0.1f..5.0f
            )

            Row(
                modifier = Modifier
                    .height(300.dp)
                    .wrapContentWidth()
            ) {
                SolarPowerFlow(
                    amount = 0.0,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    themeStream = MutableStateFlow(AppTheme.demo())
                )

                SolarPowerFlow(
                    amount = 0.5,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    themeStream = MutableStateFlow(AppTheme.demo())
                )

                SolarPowerFlow(
                    amount = 1.5,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    themeStream = MutableStateFlow(AppTheme.demo())
                )

                SolarPowerFlow(
                    amount = 2.5,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    themeStream = MutableStateFlow(AppTheme.demo())
                )

                SolarPowerFlow(
                    amount = 3.5,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    themeStream = MutableStateFlow(AppTheme.demo())
                )
            }
        }
    }
}
