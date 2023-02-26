package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.House
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun HomePowerFlowView(amount: Double, modifier: Modifier, iconHeight: Dp, themeStream: MutableStateFlow<AppTheme>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(modifier = Modifier.weight(1f)) {
            PowerFlowView(
                amount = amount,
                themeStream = themeStream,
                position = PowerFlowLinePosition.MIDDLE
            )
        }

        Box(modifier = Modifier.padding(bottom = 40.dp)) {
            Icon(
                Icons.Rounded.House,
                contentDescription = "House",
                modifier = Modifier.size(iconHeight),
                tint = colors.onBackground
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
                iconHeight = 25.dp,
                themeStream = MutableStateFlow(AppTheme.UseDefaultDisplay)
            )
        }
    }
}
