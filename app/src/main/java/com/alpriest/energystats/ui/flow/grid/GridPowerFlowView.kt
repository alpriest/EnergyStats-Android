package com.alpriest.energystats.ui.flow.grid

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun GridPowerFlowView(amount: Double, modifier: Modifier = Modifier, themeStream: MutableStateFlow<AppTheme>) {
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

@Preview(showBackground = true, heightDp = 400)
@Composable
fun GridPowerFlowViewPreview() {
    EnergyStatsTheme {
        GridPowerFlowView(
            amount = 1.0,
            modifier = Modifier,
            themeStream = MutableStateFlow(AppTheme.preview().copy(showGridTotals = true))
        )
    }
}
