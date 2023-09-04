package com.alpriest.energystats.ui.flow.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.battery.asTemperature
import com.alpriest.energystats.ui.flow.inverter.InverterIconView
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

class InverterViewModel(
    private val configManager: ConfigManaging,
    val temperatures: InverterTemperaturesViewModel?
) {
    val deviceType: String
        get() {
            return configManager.currentDevice.value?.deviceType ?: ""
        }

    val devicePlantName: String?
        get() {
            return configManager.currentDevice.value?.plantName
        }
}

@Composable
fun InverterView(
    themeStream: MutableStateFlow<AppTheme>,
    viewModel: InverterViewModel,
    orientation: Int = LocalConfiguration.current.orientation
) {
    val appTheme = themeStream.collectAsState().value

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-24).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (appTheme.showInverterIcon) {
                InverterIconView(
                    modifier = Modifier
                        .width(43.dp)
                        .height(50.dp)
                        .padding(bottom = 4.dp)
                        .background(MaterialTheme.colors.background)
                )
            }

            inverterPortraitTitles(themeStream, viewModel)

            if (appTheme.showInverterTemperatures) {
                viewModel.temperatures?.let {
                    Row(modifier = Modifier.background(MaterialTheme.colors.background)) {
                        InverterTemperatures(it)
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .offset(y = (-12.dp))
                .fillMaxWidth()
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.background(MaterialTheme.colors.background)
            ) {
                viewModel.temperatures?.let {
                    InverterTemperatures(it)
                }

                inverterLandscapeTitles(themeStream, viewModel)
            }
        }
    }
}

@Composable
private fun inverterPortraitTitles(themeStream: MutableStateFlow<AppTheme>, inverterTemperaturesViewModel: InverterViewModel) {
    val appTheme = themeStream.collectAsState().value

    if (appTheme.showInverterTypeNameOnPowerflow) {
        Text(
            text = inverterTemperaturesViewModel.deviceType
        )
    }

    if (appTheme.showInverterPlantNameOnPowerflow) {
        inverterTemperaturesViewModel.devicePlantName?.let {
            Text(
                text = it
            )
        }
    }
}

@Composable
private fun inverterLandscapeTitles(themeStream: MutableStateFlow<AppTheme>, inverterTemperaturesViewModel: InverterViewModel) {
    val appTheme = themeStream.collectAsState().value

    if (appTheme.showInverterTypeNameOnPowerflow) {
        Text(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .padding(4.dp),
            text = inverterTemperaturesViewModel.deviceType
        )
    }

    if (appTheme.showInverterPlantNameOnPowerflow) {
        inverterTemperaturesViewModel.devicePlantName?.let {
            Text(
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .padding(4.dp),
                text = it
            )
        }
    }
}

@Composable
private fun InverterTemperatures(viewModel: InverterTemperaturesViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            viewModel.temperatures.ambient.asTemperature(),
            fontSize = 12.sp
        )
        Text(
            "INTERNAL",
            fontSize = 8.sp
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            viewModel.temperatures.inverter.asTemperature(),
            fontSize = 12.sp
        )
        Text(
            "EXTERNAL",
            fontSize = 8.sp
        )
    }
}

@Composable
@Preview
fun InverterViewPreview() {
    Column {
        Spacer(modifier = Modifier.height(50.dp))

        InverterView(
            MutableStateFlow(
                AppTheme.preview()
            ),
            InverterViewModel(temperatures = null, configManager = FakeConfigManager()),
            orientation = Configuration.ORIENTATION_PORTRAIT
        )
    }
}