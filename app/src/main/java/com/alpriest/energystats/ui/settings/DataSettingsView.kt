package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DataSettingsView(rawDataStore: RawDataStoring, modifier: Modifier = Modifier) {
    var showing by rememberSaveable { mutableStateOf(true) }

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
    rawDataStore.deviceList?.let { response ->
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
    rawDataStore.battery?.let {
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
    rawDataStore.batterySettings?.let {
        SettingsTitleView("Battery Settings")

        Row {
            Text("Min SOC ${it.minSoc}")
        }
    }
}

@Composable
private fun RawDataDump(rawDataStore: RawDataStoring) {
    rawDataStore.raw?.let { responses ->
        SettingsTitleView("Raw")

        Column {
            responses.map { response ->
                response.data.last().let {
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            response.variable,
                            Modifier.weight(1f)
                        )
                        Text(
                            it.time,
                            Modifier.weight(1f)
                        )
                        Text(
                            it.value.toString()
                        )
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
    val now = SimpleDateFormat("yyyy-MM-dd hh:mm:ss zZ", Locale.getDefault()).format(Date())
    store.raw = listOf(
        RawResponse("feedInPower", arrayListOf(RawData(now, 2.45))),
        RawResponse("generationPower", arrayListOf(RawData(now, 2.45))),
        RawResponse("batChargePower", arrayListOf(RawData(now, 2.45))),
        RawResponse("batDischargePower", arrayListOf(RawData(now, 2.45))),
        RawResponse("gridConsumptionPower", arrayListOf(RawData(now, 2.45))),
        RawResponse("loadsPower", arrayListOf(RawData(now, 2.45)))
    )
    store.batterySettings = BatterySettingsResponse(30)
    store.battery = BatteryResponse(power = 2000.0, soc = 20, residual = 1000.0)
    store.deviceList = PagedDeviceListResponse(
        currentPage = 1, pageSize = 1, total = 1, devices = listOf(
            Device(deviceID = "ABC123", deviceSN = "JJJ999", hasBattery = true, hasPV = true)
        )
    )
    EnergyStatsTheme {
        DataSettingsView(rawDataStore = store, modifier = Modifier.padding(horizontal = 12.dp))
    }
}