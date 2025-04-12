package com.alpriest.energystats.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.R
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.InMemoryLoggingNetworkStore
import com.alpriest.energystats.services.NetworkOperation
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.ui.dialog.AlertDialog
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.chuckerteam.chucker.api.Chucker
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
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

@Composable
private fun <T> ResponseDebugView(
    networkStore: InMemoryLoggingNetworkStore,
    mapper: (InMemoryLoggingNetworkStore) -> MutableStateFlow<NetworkOperation<T>?>,
    fetcher: (suspend () -> Unit)?
) {
    val stream = mapper(networkStore).collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        stream.value?.let {
            SettingsTitleView(it.description)
            ESButton(
                onClick = {
                    copyToClipboard(it, context)
                }
            ) {
                Text("Copy")
            }
            Text("${it.time}")
            Text(it.request.url.toString())

            it.raw?.let {
                prettyPrintJson(it).split("\n").map {
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            text = it, fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        fetcher?.let {
            ESButton(onClick = {
                scope.launch {
                    it()
                }
            }) {
                Text("Fetch now")
            }
        }
    }
}

fun <T> copyToClipboard(networkOperation: NetworkOperation<T>, context: Context) {
    val text = networkOperation.raw?.let { prettyPrintJson(it) }
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
}

fun prettyPrintJson(jsonString: String): String {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val jsonElement = gson.fromJson(jsonString, Any::class.java)
    return gson.toJson(jsonElement)
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
