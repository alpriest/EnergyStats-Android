package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.flow.home.dateFormat
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DataSettingsView(rawDataStore: RawDataStoring, modifier: Modifier = Modifier) {
    var showing by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        SettingsTitleView("Data")

        Text(
            "Tap to reveal your latest data. If you're seeing something wrong in the app please send the details below with a description of the problem.",
            modifier = Modifier.clickable { showing = !showing }
        )

        if (showing) {
            RawDataDump(rawDataStore)
            BatterySettingsDump(rawDataStore)
            BatteryDump(rawDataStore)
            DeviceListDump(rawDataStore)
        }
    }
}

@Composable
private fun DeviceListDump(rawDataStore: RawDataStoring) {
    rawDataStore.deviceListStream.collectAsState().value?.let { response ->
        SettingsTitleView("Device List")

        response.devices.map {
            Row {
                Text("HasPV ${it.hasPV}")
            }
            Row {
                Text("HasBattery ${it.hasBattery}")
            }
        }
    }
}

@Composable
private fun BatteryDump(rawDataStore: RawDataStoring) {
    rawDataStore.batteryStream.collectAsState().value?.let {
        SettingsTitleView("Battery")

        Row {
            Text("SOC ${it.soc}")
        }
        Row {
            Text("Power ${it.power}")
        }
        Row {
            Text("Residual ${it.residual}")
        }
    }
}

@Composable
private fun BatterySettingsDump(rawDataStore: RawDataStoring) {
    rawDataStore.batterySettingsStream.collectAsState().value?.let {
        SettingsTitleView("Battery Settings")

        Row {
            Text("Min SOC ${it.minSoc}")
        }
    }
}

@Composable
private fun RawDataDump(rawDataStore: RawDataStoring) {
    rawDataStore.rawStream.collectAsState().value?.let { responses ->
        SettingsTitleView("Raw")

        Column {
            responses.map { response ->
                response.data.last().let {
                    Row(Modifier.fillMaxWidth()) {
                        Text("${it.time} ${response.variable} ${it.value}")
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    heightDp = 640
)
@Composable
fun DataSettingsViewPreview() {
    val store = RawDataStore()
    val now = SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date())
    store.store(
        raw = listOf(
            RawResponse("feedInPower", arrayListOf(RawData(now, 2.45))),
            RawResponse("generationPower", arrayListOf(RawData(now, 2.45))),
            RawResponse("batChargePower", arrayListOf(RawData(now, 2.45))),
            RawResponse("batDischargePower", arrayListOf(RawData(now, 2.45))),
            RawResponse("gridConsumptionPower", arrayListOf(RawData(now, 2.45))),
            RawResponse("loadsPower", arrayListOf(RawData(now, 2.45)))
        )
    )
    store.store(batterySettings = BatterySettingsResponse(30))
    store.store(battery = BatteryResponse(power = 2000.0, soc = 20, residual = 1000.0, temperature = 13.6))
    store.store(
        deviceList = PagedDeviceListResponse(
            currentPage = 1, pageSize = 1, total = 1, devices = listOf(
                NetworkDevice(plantName = "plant1", deviceID = "ABC123", deviceSN = "JJJ999", hasBattery = true, hasPV = true, deviceType = "F3000")
            )
        )
    )
    EnergyStatsTheme {
        DataSettingsView(rawDataStore = store, modifier = Modifier.padding(horizontal = 12.dp))
    }
}