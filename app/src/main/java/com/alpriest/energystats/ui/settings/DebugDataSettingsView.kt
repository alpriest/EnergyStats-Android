package com.alpriest.energystats.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.services.InMemoryLoggingNetworkStore
import com.alpriest.energystats.services.NetworkOperation
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.settings.debug.networkTrace.NetworkTraceDebugView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun NavGraphBuilder.debugGraph(
    navController: NavController,
    networkStore: InMemoryLoggingNetworkStore,
    configManager: ConfigManaging,
    network: Networking,
    credentialStore: CredentialStore
) {
    navigation(startDestination = "debug", route = "login") {
        composable("debug") { DebugDataSettingsView(navController) }
        composable("raw") { ResponseDebugView(networkStore, mapper = { networkStore.rawResponseStream }, fetcher = null) }
        composable("report") { ResponseDebugView(networkStore, mapper = { networkStore.reportResponseStream }, fetcher = null) }
//        composable("battery") {
//            ResponseDebugView(networkStore, mapper = { networkStore.batteryResponseStream }, fetcher = {
//                configManager.currentDevice.value?.deviceSN?.let {
//                    network.fetchBattery(it)
//                }
//            })
//        }
//        composable("batteryTimes") {
//            ResponseDebugView(networkStore, mapper = { networkStore.batteryTimesResponseStream }, fetcher = {
//                configManager.currentDevice.value?.deviceSN?.let {
//                    network.fetchBatteryTimes(it)
//                }
//            })
//        }
        composable("deviceList") {
            ResponseDebugView(networkStore, mapper = { networkStore.deviceListResponseStream }, fetcher = {
                network.fetchDeviceList()
            })
        }
//        composable("deviceSettings") {
//            ResponseDebugView(networkStore, mapper = { networkStore.deviceSettingsGetResponse }, fetcher = null)
//        }
//        composable("dataLoggers") {
//            ResponseDebugView(networkStore, mapper = { networkStore.dataLoggerListResponse }, fetcher = {
//                network.fetchDataLoggers()
//            })
//        }
        composable("networkTrace") {
            NetworkTraceDebugView(configManager, credentialStore)
        }
    }
}

@Composable
fun DebugDataSettingsView(navController: NavController) {
    SettingsColumnWithChild {
        SettingsTitleView("Debug")

        Button(onClick = { navController.navigate("raw") }) {
            Text("Raw")
        }

        Button(onClick = { navController.navigate("report") }) {
            Text("Report")
        }

        Button(onClick = { navController.navigate("battery") }) {
            Text("Battery")
        }


        Button(onClick = { navController.navigate("batteryTimes") }) {
            Text("Battery Charge Schedule")
        }

        Button(onClick = { navController.navigate("deviceList") }) {
            Text("Device List")
        }

        Button(onClick = { navController.navigate("addressBook") }) {
            Text("Address Book")
        }

        Button(onClick = { navController.navigate("earnings") }) {
            Text("Earnings")
        }

        Button(onClick = { navController.navigate("deviceSettings") }) {
            Text("Device Settings")
        }

        Button(onClick = { navController.navigate("dataLoggers") }) {
            Text("Dataloggers")
        }

        Button(onClick = { navController.navigate("networkTrace") }) {
            Text("Network trace")
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
            Button(
                onClick = {
                    copyToClipboard(it, context)
                }
            ) {
                Text("Copy")
            }
            Text("${it.time}")
            Text(it.request.url.toString())

            val raw = it.raw?.let {
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
            Button(onClick = {
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
    val networkStore = InMemoryLoggingNetworkStore.shared
    val navController = rememberNavController()

    EnergyStatsTheme {
        NavHost(
            navController = navController,
            startDestination = "debug"
        ) {
            composable("debug") { DebugDataSettingsView(navController) }
            composable("raw") { ResponseDebugView(networkStore, { networkStore.rawResponseStream }, null) }
            composable("report") { ResponseDebugView(networkStore, { networkStore.reportResponseStream }, null) }
            composable("battery") { ResponseDebugView(networkStore, { networkStore.batteryResponseStream }, null) }
            composable("batterySettings") { ResponseDebugView(networkStore, { networkStore.batterySOCResponseStream }, null) }
            composable("deviceList") { ResponseDebugView(networkStore, { networkStore.deviceListResponseStream }, null) }
            composable("addressBook") { ResponseDebugView(networkStore, { networkStore.addressBookResponseStream }, null) }
        }
    }
}

