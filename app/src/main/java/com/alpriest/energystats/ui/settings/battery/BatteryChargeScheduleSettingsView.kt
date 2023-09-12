package com.alpriest.energystats.ui.settings.battery

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material3.Divider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.alpriest.energystats.ui.flow.ErrorView
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.settings.CancelSaveButtonView
import com.alpriest.energystats.ui.settings.ContentWithBottomButtons
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

class BatteryChargeScheduleSettingsView(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController,
    private val context: Context
) {
    @Composable
    fun Content(
        viewModel: BatteryChargeScheduleSettingsViewModel = viewModel(
            factory = BatteryScheduleTimesViewModelFactory(
                network = network,
                configManager = configManager,
                context = context
            )
        )
    ) {
        val chargeSummary = viewModel.summaryStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state

        LaunchedEffect(null) {
            viewModel.load()
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(loadState.reason) { viewModel.load() }
            is LoadState.Inactive ->
                ContentWithBottomButtons(navController, onSave = { viewModel.save() }) {
                    SettingsPage {
                        BatteryTimePeriodView(viewModel.timePeriod1Stream, stringResource(R.string.period_1))
                        BatteryTimePeriodView(viewModel.timePeriod2Stream, stringResource(R.string.period_2))

                        Column {
                            SettingsTitleView(stringResource(R.string.summary))
                            Text(
                                chargeSummary,
                                color = colors.onSecondary,
                            )
                        }
                    }
                }
        }
    }

    @Composable
    fun BatteryTimePeriodView(timePeriodStream: MutableStateFlow<ChargeTimePeriod>, periodTitle: String) {
        val timePeriod = timePeriodStream.collectAsState().value
        val textColor = remember { mutableStateOf(Color.Black) }

        Column {
            SettingsTitleView(periodTitle)

            SettingsColumnWithChild {
                Row(
                    verticalAlignment = CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.enable_charge_from_grid),
                        color = colors.onSecondary,
                    )
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

            Text(
                "Reset times",
                modifier = Modifier.clickable {
                    timePeriodStream.value = ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false)
                },
                color = colors.primary,
            )
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
            Text(
                title,
                style = textStyle,
                color = colors.onSecondary,
            )

            Text(
                "${"%02d".format(time.hour)}:${"%02d".format(time.minute)}",
                style = textStyle,
                color = colors.onSecondary,
                modifier = Modifier.clickable {
                    dialog.show()
                })
        }
    }
}

@Preview(showBackground = true, widthDp = 300)
@Composable
fun BatteryForceChargeTimesViewPreview() {
    EnergyStatsTheme(darkTheme = true) {
        BatteryChargeScheduleSettingsView(
            network = DemoNetworking(),
            configManager = FakeConfigManager(),
            navController = NavHostController(LocalContext.current),
            context = LocalContext.current
        ).Content()
    }
}
