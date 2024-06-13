package com.alpriest.energystats.ui.flow.home

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.AlertDialog
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.battery.asTemperature
import com.alpriest.energystats.ui.flow.battery.iconBackgroundColor
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.flow.inverter.InverterIconView
import com.alpriest.energystats.ui.helpers.OptionalView
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.dataloggers.Rectangle
import com.alpriest.energystats.ui.settings.inverter.deviceDisplayName
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.PowerFlowNegative
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

class InverterViewModel(
    private val configManager: ConfigManaging,
    val temperatures: InverterTemperatures?,
    val deviceState: DeviceState,
    val faults: List<String>
) : AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    val hasFault: Boolean
        get() = deviceState == DeviceState.Fault || deviceState == DeviceState.Offline || faults.isNotEmpty()

    val deviceStationName: String?
        get() = configManager.currentDevice.value?.stationName

    val deviceTypeName: String?
        get() = configManager.currentDevice.value?.deviceType

    val deviceDisplayName: String
        get() = configManager.currentDevice.value?.deviceDisplayName ?: "Re-login to update"

    val devices: List<Device>
        get() = configManager.devices ?: listOf()

    val hasMultipleDevices: Boolean
        get() = (configManager.devices?.count() ?: 0) > 1

    fun select(device: Device) {
        configManager.select(device)
    }

    fun showFaults(context: Context) {
        alertDialogMessage.value = MonitorAlertDialogData(
            ex = null,
            message = faultsMessage(context)
        )
    }

    private fun faultsMessage(context: Context): String {
        val messages = mutableListOf<String>()
        messages.add(context.getString(R.string.your_inverter_state_is_reported_as_being) + "'${deviceState}'.")

        if (faults.isNotEmpty()) {
            messages.add(context.getString(R.string.reasons_reported))
            messages.addAll(faults)
        } else {
            messages.add("\n")
            messages.add(context.getString(R.string.no_reason_was_given_check_the_front_screen_of_your_inverter))
        }

        return messages.joinToString(separator = "\n")
    }
}

@Composable
fun InverterView(
    themeStream: MutableStateFlow<AppTheme>,
    viewModel: InverterViewModel,
    orientation: Int = LocalConfiguration.current.orientation
) {
    val appTheme = themeStream.collectAsState().value
    val message = viewModel.alertDialogMessage.collectAsState().value
    val context = LocalContext.current

    message?.let {
        AlertDialog(message = it.message ?: "Unknown error", onDismiss = {
            viewModel.resetDialogMessage()
        })
    }

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-24).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (appTheme.showInverterIcon) {
                Box {
                    InverterIconView(
                        modifier = Modifier
                            .width(43.dp)
                            .height(50.dp)
                            .padding(bottom = 4.dp)
                            .background(colors.background)
                            .clickable {
                                if (viewModel.hasFault) {
                                    viewModel.showFaults(context)
                                }
                            },
                        themeStream
                    )

                    panelView(themeStream, viewModel.hasFault)
                }
            }

            inverterPortraitTitles(themeStream, viewModel)

            if (appTheme.showInverterTemperatures) {
                viewModel.temperatures?.let {
                    Row(modifier = Modifier.background(colors.background)) {
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
                modifier = Modifier.background(colors.background)
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
fun panelColor(hasFault: Boolean): Color {
    return when (hasFault) {
        true -> PowerFlowNegative
        false -> Color.Gray
    }
}

@Composable
private fun panelView(themeStream: MutableStateFlow<AppTheme>, hasFault: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "inverter-panel")

    if (hasFault) {
        val color by infiniteTransition.animateColor(
            initialValue = panelColor(hasFault),
            targetValue = iconBackgroundColor(isDarkMode(themeStream)),
            animationSpec = infiniteRepeatable(
                animation = tween(600, 200, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "inverter-panel-color"
        )

        Rectangle(
            color = color,
            modifier = Modifier
                .size(width = 14.5.dp, height = 12.dp)
                .offset(6.5.dp, 9.dp)
        )
    } else {
        Rectangle(
            color = Color.Gray,
            modifier = Modifier
                .size(width = 14.5.dp, height = 12.dp)
                .offset(6.5.dp, 9.dp)
        )
    }
}

@Composable
private fun inverterPortraitTitles(themeStream: MutableStateFlow<AppTheme>, viewModel: InverterViewModel) {
    val appTheme = themeStream.collectAsState().value
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.background(colors.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewModel.hasMultipleDevices) {
            Box(contentAlignment = Alignment.TopEnd) {
                Button(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.outlinedButtonColors(backgroundColor = colors.surface, contentColor = colors.onSecondary),
                    contentPadding = PaddingValues(2.dp),
                    elevation = ButtonDefaults.elevation(1.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                viewModel.deviceDisplayName,
                                fontSize = 12.sp,
                                color = colors.onSurface
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                            )
                        }
                        if (appTheme.showInverterStationNameOnPowerflow) {
                            OptionalView(viewModel.deviceStationName) {
                                Row {
                                    Text(it)
                                }
                            }
                        }
                        if (appTheme.showInverterTypeNameOnPowerflow) {
                            OptionalView(viewModel.deviceTypeName) {
                                Row {
                                    Text(it)
                                }
                            }
                        }
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    viewModel.devices.forEach { device ->
                        DropdownMenuItem(onClick = {
                            expanded = false
                            viewModel.select(device)
                        }) {
                            Text("${device.deviceDisplayName} (${device.deviceType})")
                        }
                    }
                }
            }
        } else {
            if (appTheme.showInverterStationNameOnPowerflow) {
                OptionalView(viewModel.deviceStationName) {
                    Text(it)
                }
            }
            if (appTheme.showInverterTypeNameOnPowerflow) {
                OptionalView(viewModel.deviceTypeName) {
                    Text(it)
                }
            }
        }
    }
}

@Composable
private fun inverterLandscapeTitles(themeStream: MutableStateFlow<AppTheme>, inverterTemperaturesViewModel: InverterViewModel) {
    val appTheme = themeStream.collectAsState().value

    if (appTheme.showInverterStationNameOnPowerflow) {
        OptionalView(inverterTemperaturesViewModel.deviceStationName) {
            Text(
                modifier = Modifier
                    .background(colors.background)
                    .padding(4.dp),
                text = it
            )
        }
    }
}

@Composable
private fun InverterTemperatures(viewModel: InverterTemperatures) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            viewModel.ambient.asTemperature(),
            fontSize = 12.sp
        )
        Text(
            "INTERNAL",
            fontSize = 8.sp
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            viewModel.inverter.asTemperature(),
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
                AppTheme.demo()
            ),
            InverterViewModel(configManager = FakeConfigManager(), temperatures = null, deviceState = DeviceState.Online, faults = listOf("abc", "def")),
            orientation = Configuration.ORIENTATION_PORTRAIT
        )
    }
}