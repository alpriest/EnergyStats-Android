package com.alpriest.energystats.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.alpriest.energystats.shared.ui.BatteryView
import com.alpriest.energystats.shared.ui.HouseView
import com.alpriest.energystats.shared.ui.PylonView
import com.alpriest.energystats.shared.ui.SunIcon
import com.alpriest.energystats.wear.R
import com.alpriest.energystats.wear.presentation.theme.EnergyStatsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    EnergyStatsTheme {
        val edgePadding = if (LocalConfiguration.current.isScreenRound) 12.dp else 8.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(edgePadding)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SunIcon(size = 24.dp, color = Color.Yellow)
                Text(text = "1.2kW")
            }

            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HouseView(
                    modifier = Modifier
                        .height(30.dp)
                        .width(30.dp * 1.3f),
                    Color.Black,
                    Color.White
                )
                Text(text = "1.2kW")
            }

            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BatteryView(
                    modifier = Modifier
                        .height(30.dp)
                        .width(30.dp * 1.1f),
                    Color.Black,
                    Color.White
                )
                Text(text = "-0.35kW")
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PylonView(
                    modifier = Modifier
                        .height(30.dp)
                        .width(30.dp * 1.1f),
                    color = Color.White,
                    strokeWidth = 2f
                )
                Text(text = "1.0kW")
            }
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}

@Composable
fun isRoundDevice(): Boolean {
    return LocalConfiguration.current.isScreenRound
}