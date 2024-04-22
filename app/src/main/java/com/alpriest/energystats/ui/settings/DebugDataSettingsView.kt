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

        composable("debugReport") { ResponseDebugView(networkStore, mapper = { networkStore.reportResponseStream }, fetcher = null) }
        composable("debugQuery") { ResponseDebugView(networkStore, mapper = { networkStore.queryResponseStream }, fetcher = null) }
        composable("debugDeviceList") {
            ResponseDebugView(networkStore, mapper = { networkStore.deviceListResponseStream }, fetcher = {
                network.fetchDeviceList()
            })
        }
        composable("debugVariables") {
            ResponseDebugView(networkStore, mapper = { networkStore.variablesResponseStream }, fetcher = {
                network.fetchVariables()
            })
        }
        composable("debugBatterySOC") {
            ResponseDebugView(networkStore, mapper = { networkStore.batterySOCResponseStream }, fetcher = {
                configManager.currentDevice.value?.deviceSN?.let {
                    network.fetchBatterySettings(it)
                }
            })
        }
        composable("debugBatteryTimes") {
            ResponseDebugView(networkStore, mapper = { networkStore.batteryTimesResponseStream }, fetcher = {
                configManager.currentDevice.value?.deviceSN?.let {
                    network.fetchBatteryTimes(it)
                }
            })
        }
        composable("debugDataLoggers") {
            ResponseDebugView(networkStore, mapper = { networkStore.dataLoggerListResponse }, fetcher = {
                network.fetchDataLoggers()
            })
        }
        composable("debugNetworkTrace") {
            NetworkTraceDebugView(configManager, credentialStore)
        }
    }
}

@Composable
fun DebugDataSettingsView(navController: NavController) {
    SettingsColumnWithChild {
        SettingsTitleView("Debug")

        Button(onClick = { navController.navigate("debugReport") }) {
            Text("device/report/Query")
        }

        Button(onClick = { navController.navigate("debugQuery") }) {
            Text("device/real/Query")
        }

        Button(onClick = { navController.navigate("debugDeviceList") }) {
            Text("device/list")
        }

        Button(onClick = { navController.navigate("debugVariables") }) {
            Text("device/variable/get")
        }

        Button(onClick = { navController.navigate("debugBatterySOC") }) {
            Text("device/battery/soc/get")
        }

        Button(onClick = { navController.navigate("debugBatteryTimes") }) {
            Text("device/battery/forceChargeTime/get")
        }

        Button(onClick = { navController.navigate("debugDataLoggers") }) {
            Text("Dataloggers")
        }

        Button(onClick = { navController.navigate("debugNetworkTrace") }) {
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
            composable("debugQuery") { ResponseDebugView(networkStore, { networkStore.queryResponseStream }, null) }
            composable("debugReport") { ResponseDebugView(networkStore, { networkStore.reportResponseStream }, null) }
            composable("debugBatterySOC") { ResponseDebugView(networkStore, { networkStore.batterySOCResponseStream }, null) }
            composable("debugBatteryTimes") { ResponseDebugView(networkStore, { networkStore.batteryTimesResponseStream }, null) }
            composable("debugDeviceList") { ResponseDebugView(networkStore, { networkStore.deviceListResponseStream }, null) }
        }
    }
}

