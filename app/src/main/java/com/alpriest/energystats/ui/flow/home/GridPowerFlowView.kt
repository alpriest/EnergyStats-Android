package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.grid.PylonView
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun GridPowerFlowView(amount: Double, modifier: Modifier, iconHeight: Dp, themeStream: MutableStateFlow<AppTheme>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            PowerFlowView(
                modifier = Modifier,
                amount = amount,
                themeStream = themeStream,
                position = PowerFlowLinePosition.RIGHT
            )
        }

        Box(modifier = Modifier.padding(bottom = 40.dp)) {
            PylonView(
                modifier = Modifier
                    .height(iconHeight)
                    .width(iconHeight * 1.125f)
                    .padding(6.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GridPowerFlowViewPreview() {
    EnergyStatsTheme {
        Box(modifier = Modifier.height(300.dp)) {
            GridPowerFlowView(
                amount = 1.0,
                modifier = Modifier,
                iconHeight = 25.dp,
                themeStream = MutableStateFlow(AppTheme.UseDefaultDisplay)
            )
        }
    }
}
