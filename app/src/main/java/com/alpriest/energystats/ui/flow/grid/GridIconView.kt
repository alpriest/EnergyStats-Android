package com.alpriest.energystats.ui.flow.grid

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.ui.flow.EarningsViewModel
import com.alpriest.energystats.ui.flow.StringPower
import com.alpriest.energystats.ui.flow.home.GenerationViewModel
import com.alpriest.energystats.ui.flow.home.HomePowerFlowViewModel
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.preview
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun GridIconView(viewModel: HomePowerFlowViewModel, iconHeight: Dp, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val decimalPlaces = themeStream.collectAsState().value.decimalPlaces
    val showGridTotals = themeStream.collectAsState().value.showGridTotals

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        PylonView(
            modifier = Modifier
                .height(iconHeight)
                .width(iconHeight * 1f)
                .clipToBounds(),
            themeStream = themeStream
        )

        if (showGridTotals) {
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Row {
                    GridTotals(viewModel, decimalPlaces, themeStream)
                }
            } else {
                GridTotals(viewModel, decimalPlaces, themeStream)
            }
        }
    }
}

@Composable
private fun GridTotals(
    viewModel: HomePowerFlowViewModel,
    decimalPlaces: Int,
    themeStream: MutableStateFlow<AppTheme>
) {
    val displayUnit = themeStream.collectAsState().value.displayUnit
    val fontSize = themeStream.collectAsState().value.fontSize()
    val smallFontSize = themeStream.collectAsState().value.smallFontSize()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = viewModel.gridImportTotal.energy(displayUnit, decimalPlaces),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.total_import),
            fontSize = smallFontSize,
            color = Color.Gray,
        )

        Text(
            text = viewModel.gridExportTotal.energy(displayUnit, decimalPlaces),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
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
    val homePowerFlowViewModel = HomePowerFlowViewModel(
        solar = 1.0,
        solarStrings = listOf(
            StringPower("pv1", 0.3),
            StringPower("pv2", 0.7)
        ),
        home = 2.45,
        grid = 2.45,
        todaysGeneration = GenerationViewModel(response = OpenHistoryResponse(deviceSN = "1", datas = listOf()), false),
        earnings = EarningsViewModel.preview(),
        inverterTemperatures = null,
        hasBattery = true,
        battery = BatteryViewModel(),
        FakeConfigManager(),
        homeTotal = 1.0,
        gridImportTotal = 1.0,
        gridExportTotal = 2.0,
        ct2 = 0.4
    )

    EnergyStatsTheme {
        GridIconView(
            homePowerFlowViewModel,
            iconHeight = 30.dp,
            themeStream = MutableStateFlow(AppTheme.preview().copy(showGridTotals = true)),
            modifier = Modifier
        )
    }
}
