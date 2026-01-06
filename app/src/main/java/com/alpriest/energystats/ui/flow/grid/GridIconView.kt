package com.alpriest.energystats.ui.flow.grid

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpriest.energystats.R
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.shared.network.DemoNetworking
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.ui.PylonView
import com.alpriest.energystats.ui.flow.BannerAlertManager
import com.alpriest.energystats.ui.flow.CurrentValues
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.flow.energy
import com.alpriest.energystats.ui.flow.home.LoadedPowerFlowViewModel
import com.alpriest.energystats.ui.flow.home.ShimmerText
import com.alpriest.energystats.ui.flow.preview
import com.alpriest.energystats.shared.models.StringPower
import com.alpriest.energystats.shared.ui.iconBackgroundColor
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun GridIconView(viewModel: LoadedPowerFlowViewModel, iconHeight: Dp, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val showGridTotals = themeStream.collectAsStateWithLifecycle().value.showGridTotals

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        PylonView(
            modifier = Modifier
                .height(iconHeight)
                .width(iconHeight * 1f)
                .clipToBounds(),
            color = iconBackgroundColor(isDarkMode(themeStream)),
            strokeWidth = themeStream.collectAsStateWithLifecycle().value.strokeWidth()
        )

        if (showGridTotals) {
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    GridTotals(viewModel, 1, themeStream)
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GridTotals(viewModel, 1, themeStream)
                }
            }
        }
    }
}

@Composable
private fun GridTotals(
    viewModel: LoadedPowerFlowViewModel,
    decimalPlaces: Int,
    themeStream: MutableStateFlow<AppTheme>
) {
    val displayUnit = themeStream.collectAsStateWithLifecycle().value.displayUnit
    val fontSize = themeStream.collectAsStateWithLifecycle().value.fontSize()
    val smallFontSize = themeStream.collectAsStateWithLifecycle().value.smallFontSize()
    val gridImportTotal = viewModel.gridImportTotal.collectAsStateWithLifecycle().value
    val gridExportTotal = viewModel.gridExportTotal.collectAsStateWithLifecycle().value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShimmerText(
            shimmering = gridImportTotal == null,
            text = (gridImportTotal ?: 0.0).energy(displayUnit, decimalPlaces),
            fontSize = fontSize
        )
        Text(
            stringResource(R.string.total_import),
            fontSize = smallFontSize,
            color = Color.Gray,
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShimmerText(
            shimmering = gridExportTotal == null,
            text = (gridExportTotal ?: 0.0).energy(displayUnit, decimalPlaces),
            fontSize = fontSize
        )
        Text(
            stringResource(R.string.total_export),
            fontSize = smallFontSize,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
fun GridIconViewPreview() {
    val loadedPowerFlowViewModel = LoadedPowerFlowViewModel(
        LocalContext.current,
        currentValuesStream = MutableStateFlow(CurrentValues(2.45, 2.45, null, 0.4, 1.0, listOf(
            StringPower("pv1", 0.3),
            StringPower("pv2", 0.7)
        ))),
        hasBattery = true,
        battery = BatteryViewModel(),
        FakeConfigManager(),
        currentDevice = Device.preview(),
        network = DemoNetworking(),
        BannerAlertManager()
    )

    EnergyStatsTheme {
        GridIconView(
            loadedPowerFlowViewModel,
            iconHeight = 30.dp,
            themeStream = MutableStateFlow(AppTheme.demo().copy(showGridTotals = true)),
            modifier = Modifier
        )
    }
}
