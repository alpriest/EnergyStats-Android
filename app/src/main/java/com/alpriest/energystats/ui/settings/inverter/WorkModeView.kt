package com.alpriest.energystats.ui.settings.inverter

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.SettingsButton
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.launch

class WorkModeView(
    private val network: Networking,
    private val configManager: ConfigManaging,
) {
    @Composable
    fun Content(viewModel: WorkModeViewModel = viewModel(factory = WorkModeViewModelFactory(network, configManager))) {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val coroutineScope = rememberCoroutineScope()

        SettingsPage {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color.Red,
                    modifier = Modifier
                        .height(24.dp)
                        .width(24.dp)
                )
                Text(
                    "Only change these values if you know what you are doing",
                    color = colors.onSecondary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color.Red,
                    modifier = Modifier
                        .height(24.dp)
                        .width(24.dp)
                )
            }

            SettingsColumnWithChild {
                WorkMode.values().forEach { workMode ->
                    Row(modifier = Modifier.padding(bottom = 24.dp)) {
                        Column {
                            Row {
                                Icon(
                                    imageVector = Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "unchecked",
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                                Text(
                                    workMode.title(context),
                                    style = MaterialTheme.typography.h4,
                                    color = colors.onSecondary
                                )
                            }

                            Row(modifier = Modifier.padding(start = 32.dp)) {
                                Text(
                                    workMode.subtitle(context),
                                    color = colors.onSecondary
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Button(
                    onClick = {
                        uriHandler.openUri("https://github.com/TonyM1958/HA-FoxESS-Modbus/wiki/Inverter-Work-Modes")
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colors.primary,
                        backgroundColor = Color.Transparent
                    ),
                    elevation = null,
                ) {
                    androidx.compose.material.Icon(
                        Icons.Default.OpenInBrowser, contentDescription = "Open In Browser", modifier = Modifier.padding(end = 5.dp)
                    )
                    androidx.compose.material.Text(
                        stringResource(R.string.find_out_more_about_work_modes),
                        fontSize = 12.sp,
                    )
                }
            }

            Row {
                SettingsButton(stringResource(R.string.cancel), modifier = Modifier.weight(1.0f)) {
                    // TODO
                }

                Spacer(modifier = Modifier.width(12.dp))

                SettingsButton(stringResource(R.string.save), modifier = Modifier.weight(1.0f)) {
                    coroutineScope.launch {
//                viewModel.save()
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 400)
@Composable
fun WorkModeViewPreview() {
    EnergyStatsTheme(darkTheme = false) {
        WorkModeView(
            DemoNetworking(),
            FakeConfigManager()
        ).Content()
    }
}

enum class WorkMode {
    SELF_USE {
        override fun asInverterWorkMode(): InverterWorkMode = InverterWorkMode.SELF_USE
    },
    FEED_IN_FIRST {
        override fun asInverterWorkMode(): InverterWorkMode = InverterWorkMode.FEED_IN_FIRST
    },
    BACKUP {
        override fun asInverterWorkMode(): InverterWorkMode = InverterWorkMode.BACKUP
    },
    POWER_STATION {
        override fun asInverterWorkMode(): InverterWorkMode = InverterWorkMode.POWER_STATION
    },
    PEAK_SHAVING {
        override fun asInverterWorkMode(): InverterWorkMode = InverterWorkMode.PEAK_SHAVING
    };

    abstract fun asInverterWorkMode(): InverterWorkMode

    fun title(context: Context): String {
        return when (this) {
            SELF_USE -> context.getString(R.string.self_use)
            FEED_IN_FIRST -> context.getString(R.string.feed_in_first)
            BACKUP -> context.getString(R.string.backup)
            POWER_STATION -> context.getString(R.string.power_station)
            PEAK_SHAVING -> context.getString(R.string.peak_shaving)
        }
    }

    fun subtitle(context: Context): String {
        return when (this) {
            SELF_USE -> context.getString(R.string.self_use_mode)
            FEED_IN_FIRST -> context.getString(R.string.feed_in_first_mode)
            BACKUP -> context.getString(R.string.backup_mode)
            POWER_STATION -> context.getString(R.string.powerstation_mode)
            PEAK_SHAVING -> context.getString(R.string.peak_shaving_mode)
        }
    }
}

enum class InverterWorkMode {
    SELF_USE,
    FEED_IN_FIRST,
    BACKUP,
    POWER_STATION,
    PEAK_SHAVING
}
