package com.alpriest.energystats.ui.flow.battery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.models.asPercent
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun BatteryPowerFlow(
    viewModel: BatteryPowerViewModel,
    iconHeight: Dp,
    modifier: Modifier = Modifier
) {
    var percentage by remember { mutableStateOf(true) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(modifier = Modifier.weight(1f)) {
            PowerFlowView(
                amount = viewModel.batteryChargePowerkWH
            )
        }

        BatteryView(
            modifier = Modifier
                .height(iconHeight)
                .width(iconHeight * 1.25f)
                .padding(5.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
        ) {
            Box(modifier = Modifier.clickable { percentage = !percentage }) {
                if (percentage) {
                    Text(
                        viewModel.batteryStateOfCharge.asPercent(),
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        viewModel.batteryCapacity,
                        fontSize = 12.sp
                    )
                }
            }

            viewModel.batteryExtra?.let {
                Text(
                    it,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    maxLines = 2,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
fun BatteryPowerFlowViewPreview() {
    EnergyStatsTheme {
        BatteryPowerFlow(
            viewModel = BatteryPowerViewModel(
                FakeConfigManager(),
                batteryChargePowerkWH = -0.5,
                batteryStateOfCharge = 0.25
            ),
            iconHeight = 40.dp,
            modifier = Modifier.width(100.dp)
        )
    }
}