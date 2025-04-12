package com.alpriest.energystats.ui.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.R
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.ui.dialog.AlertDialog
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.chuckerteam.chucker.api.Chucker
import kotlinx.coroutines.launch

fun NavGraphBuilder.debugGraph(
    navController: NavHostController,
    network: Networking
) {
    navigation(startDestination = "debug", route = "login") {
        composable("debug") {
            LoadedScaffold(title = stringResource(R.string.view_debug_data), navController = navController) {
                DebugDataSettingsView(network, it)
            }
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

    SettingsColumn(modifier) {
        SettingsColumnWithChild {
            ESButton(onClick = {
                val intent = Chucker.getLaunchIntent(context)
                context.startActivity(intent)
            }) {
                Text(stringResource(R.string.launch_chucker))
            }

            Text(stringResource(R.string.chucker_description))

        }

        SettingsColumnWithChild(modifier) {
            ESButton(onClick = {
                scope.launch {
                    try {
                        val counts = network.fetchRequestCount()

                        alertDialogMessage.value = "${counts.remaining} remaining out of ${counts.total} total"
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

@Preview(showBackground = true, heightDp = 640)
@Composable
fun DebugDataSettingsViewPreview() {
    val navController = rememberNavController()

    EnergyStatsTheme {
        LoadedScaffold(title = stringResource(R.string.view_debug_data), navController = navController) {
            DebugDataSettingsView(DemoNetworking(), it)
        }
    }
}
