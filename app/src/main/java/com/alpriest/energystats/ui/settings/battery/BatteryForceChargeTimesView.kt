package com.alpriest.energystats.ui.settings.battery

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Divider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.SettingsButton
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BatteryForceChargeTimesViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Networking::class.java, ConfigManaging::class.java, NavController::class.java)
            .newInstance(network, configManager, navController)
    }
}

class BatteryForceChargeTimesViewModel(
    private val network: Networking,
    private val config: ConfigManaging,
    private val navController: NavController
) : ViewModel() {
    val timePeriod1Stream = MutableStateFlow(ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false))
    val timePeriod2Stream = MutableStateFlow(ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false))
    var activityStream = MutableStateFlow<String?>(null)

    suspend fun load() {
        activityStream.value = "Loading"

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                val result = network.fetchBatteryTimes(deviceSN)
                result.times.getOrNull(0)?.let {
                    timePeriod1Stream.value = ChargeTimePeriod(
                        start = it.startTime,
                        end = it.endTime,
                        enabled = it.enableGrid
                    )
                }

                result.times.getOrNull(1)?.let {
                    timePeriod2Stream.value = ChargeTimePeriod(
                        start = it.startTime,
                        end = it.endTime,
                        enabled = it.enableGrid
                    )
                }
            }
        }.also {
            activityStream.value = null
        }
    }

    suspend fun save() {
//        activityStream.value = "Saving"
//
//        runCatching {
//            config.currentDevice.value?.let { device ->
//                val deviceSN = device.deviceSN
//
//                network.setSoc(
//                    minSOC = minSOCStream.value.toInt(),
//                    minGridSOC = minSOConGridStream.value.toInt(),
//                    deviceSN = deviceSN
//                )
//
//                navController.popBackStack()
//            } ?: run {
//                activityStream.value = null
//            }
//        }
    }
}

private fun Time.Companion.zero(): Time {
    return Time(0, 0)
}

class BatteryForceChargeTimes(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController
) {
    @Composable
    fun Content(
        viewModel: BatteryForceChargeTimesViewModel = viewModel(
            factory = BatteryForceChargeTimesViewModelFactory(
                network = network,
                configManager = configManager,
                navController = navController
            )
        )
    ) {
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background)
                .padding(12.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            BatteryTimePeriodView(viewModel.timePeriod1Stream, "Period 1")
            BatteryTimePeriodView(viewModel.timePeriod2Stream, "Period 2")

            SettingsButton("Save") {
                coroutineScope.launch {
                    viewModel.save()
                }
            }
        }
    }

    @Composable
    fun BatteryTimePeriodView(timePeriodStream: MutableStateFlow<ChargeTimePeriod>, periodTitle: String) {
        val timePeriod = timePeriodStream.collectAsState().value

        SettingsColumnWithChild {
            Text(periodTitle)

            Divider()

            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enable charge from grid")
                Switch(
                    checked = timePeriod.enabled,
                    onCheckedChange = {
                        timePeriodStream.value = ChargeTimePeriod(start = timePeriod.start, end = timePeriod.end, enabled = it)
                    }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            TimePeriodView(timePeriod.start, "Start") { hour, minute ->
                timePeriodStream.value = ChargeTimePeriod(start = Time(hour, minute), end = timePeriod.end, enabled = timePeriod.enabled)
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            TimePeriodView(timePeriod.end, "End") { hour, minute ->
                timePeriodStream.value = ChargeTimePeriod(start = timePeriod.start, end = Time(hour, minute), enabled = timePeriod.enabled)
            }
        }
    }

    @Composable
    fun TimePeriodView(time: Time, title: String, onChange: (Int, Int) -> Unit) {
        val dialog = TimePickerDialog(
            LocalContext.current,
            { _, mHour: Int, mMinute: Int ->
                onChange(mHour, mMinute)
            },
            time.hour,
            time.minute,
            false
        )

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(title)

            Text(
                "${"%02d".format(time.hour)}:${"%02d".format(time.minute)}",
                modifier = Modifier.clickable {
                    dialog.show()
                }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 300)
@Composable
fun BatteryForceChargeTimesViewPreview() {
    EnergyStatsTheme {
        BatteryForceChargeTimes(
            configManager = FakeConfigManager(),
            network = DemoNetworking(),
            navController = NavHostController(LocalContext.current)
        ).Content()
    }
}
