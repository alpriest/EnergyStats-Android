package com.alpriest.energystats.ui.flow.grid

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.ui.flow.LineOrientation
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.shared.models.demo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun GridPowerFlowView(amount: Double, modifier: Modifier = Modifier, appSettingsStream: StateFlow<AppSettings>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Box {
            PowerFlowView(
                modifier = Modifier,
                amount = amount,
                appSettingsStream = appSettingsStream,
                position = PowerFlowLinePosition.RIGHT,
                useColouredLines = true,
                orientation = LineOrientation.VERTICAL
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
            appSettingsStream = MutableStateFlow(AppSettings.demo().copy(showGridTotals = true))
        )
    }
}
