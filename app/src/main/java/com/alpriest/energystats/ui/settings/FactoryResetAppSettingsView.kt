package com.example.energystats

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.ui.settings.BottomButtonConfiguration
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.ui.settings.ContentWithBottomButtons
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.shared.ui.PowerFlowNegative
import com.alpriest.energystats.shared.ui.PowerFlowPositive

@Composable
fun FactoryResetAppSettingsView(
    configManager: ConfigManaging,
    navController: NavController
) {
    var confirmationShowing by remember { mutableStateOf(false) }

    val checkmarkIcon = Icons.Default.CheckCircle
    val xmarkIcon = Icons.Default.Cancel
    val positiveColor = PowerFlowPositive
    val negativeColor = PowerFlowNegative

    ContentWithBottomButtons(
        buttons = listOf(
            BottomButtonConfiguration(title = LocalContext.current.getString(R.string.cancel), onTap = { navController.popBackStack() }),
            BottomButtonConfiguration(title = LocalContext.current.getString(R.string.reset_ellipsis), onTap = {
                confirmationShowing = true
            }),
        ),
        content = {
            SettingsPage(Modifier) {
                SettingsColumn(header = stringResource(R.string.will_be_reset)) {
                    StepView(stringResource(R.string.display_settings), checkmarkIcon, positiveColor)
                    StepView(stringResource(R.string.data_settings), checkmarkIcon, positiveColor)
                    StepView(stringResource(R.string.earnings_settings), checkmarkIcon, positiveColor)
                    StepView(stringResource(R.string.self_sufficiency_settings), checkmarkIcon, positiveColor)
                    StepView(stringResource(R.string.inverter_display_settings_ct2_settings), checkmarkIcon, positiveColor)
                    StepView(stringResource(R.string.battery_display_settings), checkmarkIcon, positiveColor)
                    StepView(stringResource(R.string.custom_parameter_groups), checkmarkIcon, positiveColor)
                }

                SettingsColumn(header = stringResource(R.string.will_not_be_reset)) {
                    StepView(stringResource(R.string.inverter_schedules), xmarkIcon, negativeColor)
                    StepView(stringResource(R.string.battery_schedule), xmarkIcon, negativeColor)
                    StepView(stringResource(R.string.battery_charge_levels), xmarkIcon, negativeColor)
                    StepView(stringResource(R.string.foxess_api_key), xmarkIcon, negativeColor)
                    StepView(stringResource(R.string.solcast_api_key), xmarkIcon, negativeColor)
                }

                if (confirmationShowing) {
                    AlertDialog(
                        onDismissRequest = { confirmationShowing = false },
                        title = { Text(stringResource(R.string.you_cannot_undo_this_action)) },
                        confirmButton = {
                            TextButton(onClick = {
                                configManager.resetDisplaySettings()
                                confirmationShowing = false
                                navController.popBackStack()
                            }) {
                                Text(stringResource(R.string.reset_app_settings), color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { confirmationShowing = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
            }
        })
}

@Composable
fun StepView(text: String, icon: ImageVector, iconColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = null, tint = iconColor)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Preview(showBackground = true, heightDp = 940)
@Composable
fun DataSettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        FactoryResetAppSettingsView(
            configManager = FakeConfigManager(),
            navController = NavHostController(LocalContext.current)
        )
    }
}