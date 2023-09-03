package com.alpriest.energystats.ui.flow.home

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Wh
import com.alpriest.energystats.models.kWh
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
fun GridIconView(viewModel: HomePowerFlowViewModel, iconHeight: Dp, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val fontSize = themeStream.collectAsState().value.fontSize()
    val decimalPlaces = themeStream.collectAsState().value.decimalPlaces
    var showImport by remember { mutableStateOf(true) }
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
                    GridTotals({ showImport = !showImport }, showImport, viewModel, fontSize, decimalPlaces, themeStream)
                }
            } else {
                GridTotals({ showImport = !showImport }, showImport, viewModel, fontSize, decimalPlaces, themeStream)
            }
        }
    }
}

@Composable
private fun GridTotals(
    onClick: () -> Unit,
    showImport: Boolean,
    viewModel: HomePowerFlowViewModel,
    fontSize: TextUnit,
    decimalPlaces: Int,
    themeStream: MutableStateFlow<AppTheme>
) {
    val showValuesInWatts = themeStream.collectAsState().value.showValuesInWatts

    Box(modifier = Modifier.clickable { onClick() }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showImport) {
                Text(
                    text = if (showValuesInWatts) viewModel.gridImportTotal.Wh(decimalPlaces) else viewModel.gridImportTotal.kWh(decimalPlaces),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.total_import),
                    fontSize = fontSize,
                    color = Color.Gray,
                )
            } else {
                Text(
                    text = if (showValuesInWatts) viewModel.gridExportTotal.Wh(decimalPlaces) else viewModel.gridExportTotal.kWh(decimalPlaces),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.total_export),
                    fontSize = fontSize,
                    color = Color.Gray,
                )
            }
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
                themeStream = MutableStateFlow(AppTheme.preview())
            )
        }
    }
}
