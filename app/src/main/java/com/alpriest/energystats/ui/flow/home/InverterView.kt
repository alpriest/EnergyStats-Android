package com.alpriest.energystats.ui.flow.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.ui.flow.battery.asTemperature
import com.alpriest.energystats.ui.flow.inverter.InverterIconView
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun InverterView(
    themeStream: MutableStateFlow<AppTheme>,
    homePowerFlowViewModel: SummaryPowerFlowViewModel
) {
    val appTheme = themeStream.collectAsState().value

    if (appTheme.showInverterTemperatures) {
        homePowerFlowViewModel.inverterViewModel?.let {
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
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
                    Text(
                        modifier = Modifier
                            .background(MaterialTheme.colors.background)
                            .padding(4.dp),
                        text = it.name
                    )

                    Row(modifier = Modifier.background(MaterialTheme.colors.background)) {
                        InverterTemperatures(it)
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
                        InverterTemperatures(it)

                        Text(
                            it.name,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        }
    } else {
        if (appTheme.showInverterIcon) {
            Column(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .offset(y = (-24).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InverterIconView(
                    modifier = Modifier
                        .width(43.dp)
                        .height(50.dp)
                        .padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun InverterTemperatures(viewModel: InverterViewModel) {
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
