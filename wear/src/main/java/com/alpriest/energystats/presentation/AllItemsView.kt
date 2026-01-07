package com.alpriest.energystats.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.shared.models.SolarRangeDefinitions

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun AllItemsView(
    solarAmount: Double?,
    houseLoadAmount: Double?,
    batteryChargeAmount: Double?,
    batteryChargeLevel: Double?,
    gridAmount: Double?,
    solarRangeDefinitions: SolarRangeDefinitions,
    totalImport: Double?,
    totalExport: Double?
) {
    val edgePadding = if (isRoundDevice()) 12.dp else 16.dp
    val solarAlign = if (isRoundDevice()) Alignment.TopCenter else Alignment.TopStart
    val houseAlign = if (isRoundDevice()) Alignment.CenterStart else Alignment.TopEnd
    val batteryAlign = if (isRoundDevice()) Alignment.CenterEnd else Alignment.BottomStart
    val gridAlign = if (isRoundDevice()) Alignment.BottomCenter else Alignment.BottomEnd

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(edgePadding)
    ) {
        Column(
            modifier = Modifier.align(solarAlign),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SolarPowerView(IconScale.SMALL, solarAmount, solarRangeDefinitions)
        }

        Column(
            modifier = Modifier.align(houseAlign),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomePowerView(IconScale.SMALL, houseLoadAmount)
        }

        Column(
            modifier = Modifier.align(batteryAlign),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BatteryPowerView(IconScale.SMALL, batteryChargeAmount, batteryChargeLevel)
        }

        Column(
            modifier = Modifier.align(gridAlign),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GridPowerView(IconScale.SMALL, gridAmount, totalImport, totalExport)
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreviewSmallRound() {
    AllItemsView(1.2, 3.2, 1.0, 0.23, -1.9, SolarRangeDefinitions.Companion.defaults, 2.2, 1.2)
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun DefaultPreviewLargeRound() {
    AllItemsView(1.2, 3.2, 1.0, 0.23, -1.9, SolarRangeDefinitions.Companion.defaults, 2.2, 1.2)
}

@Preview(device = WearDevices.SQUARE, showSystemUi = true)
@Composable
fun DefaultPreviewSquare() {
    AllItemsView(1.2, 3.2, 1.0, 0.23, -1.9, SolarRangeDefinitions.Companion.defaults, 2.2, 1.2)
}

@Preview(device = WearDevices.RECT, showSystemUi = true)
@Composable
fun DefaultPreviewRect() {
    AllItemsView(1.2, 3.2, 1.0, 0.23, -1.9, SolarRangeDefinitions.Companion.defaults, 2.2, 1.2)
}

@Composable
fun isRoundDevice(): Boolean {
    return LocalConfiguration.current.isScreenRound
}