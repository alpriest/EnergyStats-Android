package com.alpriest.energystats.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.presentation.theme.EnergyStatsTheme
import com.alpriest.energystats.shared.helpers.asPercent
import com.alpriest.energystats.shared.helpers.kW
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.ui.BatteryView
import com.alpriest.energystats.shared.ui.HouseView
import com.alpriest.energystats.shared.ui.PylonView
import com.alpriest.energystats.shared.ui.SunIconWithThresholds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            val vm: WearHomeViewModel = viewModel()
            val state by vm.state.collectAsState()

            WearApp(
                solarAmount = state.solarAmount,
                houseLoadAmount = state.houseLoadAmount,
                batteryChargeAmount = state.batteryChargeAmount,
                batteryChargeLevel = state.batteryChargeLevel,
                gridAmount = state.gridAmount,
                state.solarRangeDefinitions
            )
        }
    }
}

@Composable
fun WearApp(
    solarAmount: Double,
    houseLoadAmount: Double,
    batteryChargeAmount: Double,
    batteryChargeLevel: Double,
    gridAmount: Double,
    solarRangeDefinitions: SolarRangeDefinitions
) {
    EnergyStatsTheme {
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
                IconWrapper {
                    SunIconWithThresholds(solarAmount, iconHeight = 24.dp, solarRangeDefinitions, true)
                }
                Text(text = solarAmount.kW(2))
            }

            Column(
                modifier = Modifier.align(houseAlign),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconWrapper {
                    HouseView(
                        modifier = Modifier
                            .height(30.dp)
                            .width(30.dp * 1.3f),
                        Color.Black,
                        Color.White
                    )
                }
                Text(text = houseLoadAmount.kW(2))
            }

            Column(
                modifier = Modifier.align(batteryAlign),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("")
                IconWrapper {
                    BatteryView(
                        modifier = Modifier
                            .height(30.dp)
                            .width(30.dp * 1.25f),
                        Color.Black,
                        Color.White
                    )
                }
                Text(text = batteryChargeAmount.kW(2))
                Text(text = batteryChargeLevel.asPercent())
            }

            Column(
                modifier = Modifier.align(gridAlign),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconWrapper {
                    PylonView(
                        modifier = Modifier
                            .height(30.dp)
                            .width(30.dp * 1.1f),
                        color = Color.White,
                        strokeWidth = 2f
                    )
                }
                Text(text = gridAmount.kW(2))

                if (!isRoundDevice()) {
                    Text("")
                }
            }
        }
    }
}

@Composable
private fun IconWrapper(content: @Composable () -> Unit) {
    Column(modifier = Modifier.height(35.dp)) {
        content()
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreviewRound() {
    WearApp(1.2, 3.2, 1.0, 0.23, -1.9, SolarRangeDefinitions.defaults)
}

@Preview(device = WearDevices.SQUARE, showSystemUi = true)
@Composable
fun DefaultPreviewSquare() {
    WearApp(1.2, 3.2, 1.0, 0.23, -1.9, SolarRangeDefinitions.defaults)
}

@Preview(device = WearDevices.RECT, showSystemUi = true)
@Composable
fun DefaultPreviewRect() {
    WearApp(1.2, 3.2, 1.0, 0.23, -1.9, SolarRangeDefinitions.defaults)
}

@Composable
fun isRoundDevice(): Boolean {
    return LocalConfiguration.current.isScreenRound
}
