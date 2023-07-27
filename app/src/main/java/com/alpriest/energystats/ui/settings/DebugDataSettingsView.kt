package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun NavGraphBuilder.debugGraph(navController: NavController, networkStore: InMemoryLoggingNetworkStore) {
    navigation(startDestination = "debug", route = "login") {
        composable("debug") { DebugDataSettingsView(navController) }
        composable("raw") { ResponseDebugView(networkStore) { networkStore.rawResponseStream } }
        composable("report") { ResponseDebugView(networkStore) { networkStore.reportResponseStream } }
        composable("battery") { ResponseDebugView(networkStore) { networkStore.batteryResponseStream } }
        composable("batterySettings") { ResponseDebugView(networkStore) { networkStore.batterySettingsResponseStream } }
        composable("deviceList") { ResponseDebugView(networkStore) { networkStore.deviceListResponseStream } }
        composable("addressBook") { ResponseDebugView(networkStore) { networkStore.addressBookResponseStream } }
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

        Button(onClick = { navController.navigate("batterySettings") }) {
            Text("Battery Settings")
        }

        Button(onClick = { navController.navigate("deviceList") }) {
            Text("Device List")
        }

        Button(onClick = { navController.navigate("addressBook") }) {
            Text("Address Book")
        }
    }
}

@Composable
private fun <T> ResponseDebugView(
    networkStore: InMemoryLoggingNetworkStore, mapper: (InMemoryLoggingNetworkStore) -> MutableStateFlow<NetworkOperation<T>?>
) {
    val stream = mapper(networkStore).collectAsState()
    val scrollState = rememberScrollState()

    stream.value?.let {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            SettingsTitleView(it.description)
            Text("${it.time}")

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
    }
}

fun prettyPrintJson(jsonString: String): String {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val jsonElement = gson.fromJson(jsonString, Any::class.java)
    return gson.toJson(jsonElement)
}

@Preview(
    showBackground = true, heightDp = 640
)
@Composable
fun DataSettingsViewPreview() {
    val networkStore = InMemoryLoggingNetworkStore()
    val formatter = DateTimeFormatter.ofPattern(dateFormat)
    val now = LocalDateTime.now().format(formatter)
    val navController = rememberNavController()

//    store.addressBookResponse = NetworkOperation(
//        "Address book",
//        value = NetworkResponse(
//            errno = 0,
//            result = AddressBookResponse(SoftwareVersion("1.21", "1.9", "0.9"))
//        ),
//        raw = null
//    )

//    store.store(
//        raw = listOf(
//            RawResponse("feedInPower", arrayListOf(RawData(now, 2.45))),
//            RawResponse("generationPower", arrayListOf(RawData(now, 2.45))),
//            RawResponse("batChargePower", arrayListOf(RawData(now, 2.45))),
//            RawResponse("batDischargePower", arrayListOf(RawData(now, 2.45))),
//            RawResponse("gridConsumptionPower", arrayListOf(RawData(now, 2.45))),
//            RawResponse("loadsPower", arrayListOf(RawData(now, 2.45)))
//        )
//    )
//    store.store(batterySettings = BatterySettingsResponse(30))
//    store.store(battery = BatteryResponse(power = 2000.0, soc = 20, residual = 1000, temperature = 13.6))
//    store.store(
//        deviceList = PagedDeviceListResponse(
//            currentPage = 1, pageSize = 1, total = 1, devices = listOf(
//                NetworkDevice(plantName = "plant1", deviceID = "ABC123", deviceSN = "JJJ999", hasBattery = true, hasPV = true, deviceType = "F3000")
//            )
//        )
//    )
    EnergyStatsTheme {
        NavHost(
            navController = navController,
            startDestination = "debug"
        ) {
            composable("debug") { DebugDataSettingsView(navController) }
            composable("raw") { ResponseDebugView(networkStore) { networkStore.rawResponseStream } }
            composable("report") { ResponseDebugView(networkStore) { networkStore.reportResponseStream } }
            composable("battery") { ResponseDebugView(networkStore) { networkStore.batteryResponseStream } }
            composable("batterySettings") { ResponseDebugView(networkStore) { networkStore.batterySettingsResponseStream } }
            composable("deviceList") { ResponseDebugView(networkStore) { networkStore.deviceListResponseStream } }
            composable("addressBook") { ResponseDebugView(networkStore) { networkStore.addressBookResponseStream } }
        }
    }
}