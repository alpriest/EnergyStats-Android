package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.flow.LineOrientation
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.shared.models.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.shared.models.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun HomePowerFlowView(amount: Double, modifier: Modifier, themeStream: MutableStateFlow<AppTheme>, position: PowerFlowLinePosition = PowerFlowLinePosition.MIDDLE) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(modifier = Modifier.weight(1f)) {
            PowerFlowView(
                amount = amount,
                themeStream = themeStream,
                position = position,
                orientation = LineOrientation.VERTICAL
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePowerFlowViewPreview() {
    EnergyStatsTheme {
        Box(modifier = Modifier.height(300.dp)) {
            HomePowerFlowView(
                amount = 1.0,
                modifier = Modifier,
                themeStream = MutableStateFlow(AppTheme.demo())
            )
        }
    }
}
