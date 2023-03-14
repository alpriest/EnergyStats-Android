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
fun GridPowerFlowView(amount: Double, modifier: Modifier, themeStream: MutableStateFlow<AppTheme>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Box {
            PowerFlowView(
                modifier = Modifier,
                amount = amount,
                themeStream = themeStream,
                position = PowerFlowLinePosition.RIGHT,
                useColouredLines = true
            )
        }
    }
}

@Composable
fun GridIconView(iconHeight: Dp, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
            PylonView(
                modifier = Modifier
                    .height(iconHeight)
                    .width(iconHeight * 1.125f)
                    .padding(6.dp),
                themeStream = themeStream
            )
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
                themeStream = MutableStateFlow(AppTheme.preview())
            )
        }
    }
}
