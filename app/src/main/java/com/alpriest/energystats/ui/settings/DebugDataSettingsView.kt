package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.alpriest.energystats.R
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.shared.network.DemoNetworking
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.tabs.TopBarSettings
import com.alpriest.energystats.ui.dialog.AlertDialog
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.chuckerteam.chucker.api.Chucker
import kotlinx.coroutines.launch

fun NavGraphBuilder.debugGraph(
    topBarSettings: MutableState<TopBarSettings>,
    network: Networking,
    navController: NavHostController
) {
    navigation(startDestination = "debug", route = "login") {
        composable("debug") {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.view_debug_data), {}, { navController.popBackStack() })
            DebugDataSettingsView(network, Modifier)
        }
    }
}

@Composable
fun DebugDataSettingsView(network: Networking, modifier: Modifier) {
    val scope = rememberCoroutineScope()
    val alertDialogMessage = remember { mutableStateOf(null as String?) }
    trackScreenView("Debug", "DebugDataSettingsView")
    val context = LocalContext.current

    alertDialogMessage.value?.let {
        AlertDialog(message = it, onDismiss = {
            alertDialogMessage.value = null
        })
    }

    SettingsPage(modifier) {
        SettingsColumnWithChild(padding = SettingsPaddingValues.withVertical()) {
            Text(stringResource(R.string.chucker_description))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                ESButton(onClick = {
                    val intent = Chucker.getLaunchIntent(context)
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.launch_chucker))
                }
            }
        }

        SettingsColumnWithChild(padding = SettingsPaddingValues.withVertical()) {
            Text(stringResource(R.string.fox_restricts_the_number_of_network_requests_you_can_make_within_a_24_hr_period_find_out_how_many_you_have_left))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                ESButton(onClick = {
                    scope.launch {
                        try {
                            val counts = network.fetchRequestCount()

                            alertDialogMessage.value = context.getString(R.string.requests_remaining_out_of_total, counts.remaining, counts.total)
                        } catch (ex: Exception) {
                            alertDialogMessage.value = "API Timeout"
                        }
                    }
                }) {
                    Text(stringResource(R.string.view_request_count))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
fun DebugDataSettingsViewPreview() {
    EnergyStatsTheme {
        DebugDataSettingsView(DemoNetworking(), Modifier)
    }
}
