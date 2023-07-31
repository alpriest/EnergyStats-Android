package com.alpriest.energystats.ui.settings.battery

import android.app.TimePickerDialog
import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.settings.SettingsButton
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BatteryForceChargeTimes(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController,
    private val context: Context
) {
    @Composable
    fun Content(
        viewModel: BatteryForceChargeTimesViewModel = viewModel(
            factory = BatteryForceChargeTimesViewModelFactory(
                network = network,
                configManager = configManager,
                navController = navController,
                context = context
            )
        )
    ) {
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        val chargeSummary = viewModel.summaryStream.collectAsState().value
        val isActive = viewModel.activityStream.collectAsState().value

        LaunchedEffect(null) {
            viewModel.load()
        }

        isActive?.let {
            LoadingView(it)
        } ?: run {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
                    .padding(12.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                BatteryTimePeriodView(viewModel.timePeriod1Stream, stringResource(R.string.period_1))
                BatteryTimePeriodView(viewModel.timePeriod2Stream, stringResource(R.string.period_2))

                Column {
                    SettingsTitleView("Summary")
                    Text(chargeSummary)
                }

                SettingsButton("Save") {
                    coroutineScope.launch {
                        viewModel.save()
                    }
                }
            }
        }
    }

    @Composable
    fun BatteryTimePeriodView(timePeriodStream: MutableStateFlow<ChargeTimePeriod>, periodTitle: String) {
        val timePeriod = timePeriodStream.collectAsState().value
        val errorMessage = remember { mutableStateOf<String?>(null) }
        val textColor = remember { mutableStateOf(Color.Black) }

        LaunchedEffect(null) {
            timePeriodStream.collect {
                if (it.start.after(it.end)) {
                    textColor.value = Color.Red
                } else {
                    textColor.value = Color.Black
                }
                errorMessage.value = it.validate
            }
        }

        Column {
            SettingsTitleView(periodTitle)

            SettingsColumnWithChild {
                Row(
                    verticalAlignment = CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.enable_charge_from_grid))
                    Switch(checked = timePeriod.enabled, onCheckedChange = {
                        timePeriodStream.value = ChargeTimePeriod(start = timePeriod.start, end = timePeriod.end, enabled = it)
                    })
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                TimePeriodView(
                    timePeriod.start,
                    stringResource(R.string.start),
                    textStyle = TextStyle(color = textColor.value)
                ) { hour, minute ->
                    timePeriodStream.value = ChargeTimePeriod(start = Time(hour, minute), end = timePeriod.end, enabled = timePeriod.enabled)
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                TimePeriodView(
                    timePeriod.end,
                    stringResource(R.string.end),
                    textStyle = TextStyle(color = textColor.value)
                ) { hour, minute ->
                    timePeriodStream.value = ChargeTimePeriod(start = timePeriod.start, end = Time(hour, minute), enabled = timePeriod.enabled)
                }
            }

            errorMessage.value?.let {
                Text(
                    text = it,
                    style = TextStyle(color = textColor.value)
                )
            }
        }
    }

    @Composable
    fun TimePeriodView(time: Time, title: String, textStyle: TextStyle, onChange: (Int, Int) -> Unit) {
        val dialog = TimePickerDialog(
            LocalContext.current, { _, mHour: Int, mMinute: Int ->
                onChange(mHour, mMinute)
            },
            time.hour,
            time.minute,
            true
        )

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(title, style = textStyle)

            Text(
                "${"%02d".format(time.hour)}:${"%02d".format(time.minute)}",
                style = textStyle,
                modifier = Modifier.clickable {
                    dialog.show()
                })
        }
    }
}

@Preview(showBackground = true, widthDp = 300, locale = "de")
@Composable
fun BatteryForceChargeTimesViewPreview() {
    EnergyStatsTheme {
        BatteryForceChargeTimes(
            network = DemoNetworking(),
            configManager = FakeConfigManager(),
            navController = NavHostController(LocalContext.current),
            context = LocalContext.current
        ).Content()
    }
}
