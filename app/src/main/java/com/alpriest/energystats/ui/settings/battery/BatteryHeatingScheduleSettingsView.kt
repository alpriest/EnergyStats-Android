package com.alpriest.energystats.ui.settings.battery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.helpers.celsius
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.TimeType
import com.alpriest.energystats.shared.models.network.Time
import com.alpriest.energystats.shared.network.DemoNetworking
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsBottomSpace
import com.alpriest.energystats.ui.settings.SettingsCheckbox
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPaddingValues
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class BatteryHeatingScheduleSettingsViewData(
    val available: Boolean,
    val enabled: Boolean,
    val currentState: String?,
    val timePeriod1: ChargeTimePeriod,
    val timePeriod2: ChargeTimePeriod,
    val timePeriod3: ChargeTimePeriod,
    val startTemperature: Double,
    val endTemperature: Double,
    val minStartTemperature: Double,
    val maxStartTemperature: Double,
    val minEndTemperature: Double,
    val maxEndTemperature: Double,
    val summary: String,
)

class BatteryHeatingScheduleSettingsView(
    private val network: Networking, private val configManager: ConfigManaging, private val navController: NavController, private val userManager: UserManaging
) {
    @Composable
    fun Content(
        viewModel: BatteryHeatingScheduleSettingsViewModel = viewModel(
            factory = BatteryHeatingScheduleSettingsViewModelFactory(
                network = network, configManager = configManager
            )
        ), modifier: Modifier
    ) {
        val loadState = viewModel.uiState.collectAsState().value.state
        val context = LocalContext.current
        val viewData = viewModel.viewDataStream.collectAsStateWithLifecycle().value
        val coroutineScope = rememberCoroutineScope()

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(null) {
            viewModel.load(context)
        }
        trackScreenView("Battery Heating Schedule", "BatteryHeatingScheduleSettingsView")

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState)
            is LoadState.Error -> ErrorView(
                loadState.ex,
                loadState.reason,
                loadState.allowRetry,
                onRetry = { viewModel.load(context) },
                onLogout = { coroutineScope.launch { userManager.logout() } })

            is LoadState.Inactive -> {
                when (viewData.available) {
                    false -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { Text(stringResource(R.string.not_supported)) }

                    true -> ScheduleAvailable(viewModel, modifier)
                }
            }
        }
    }

    @Composable
    fun ScheduleAvailable(viewModel: BatteryHeatingScheduleSettingsViewModel, modifier: Modifier) {
        val context = LocalContext.current
        val viewData = viewModel.viewDataStream.collectAsStateWithLifecycle().value

        ContentWithBottomButtonPair(
            navController, onConfirm = { viewModel.save(context) }, dirtyStateFlow = viewModel.dirtyState, content = { innerModifier ->
                SettingsPage(innerModifier) {
                    SettingsColumn(
                        header = stringResource(R.string.schedule_summary), padding = SettingsPaddingValues.withVertical()
                    ) {
                        Text(
                            viewData.summary, color = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    SettingsColumn(
                        padding = SettingsPaddingValues.withVertical()
                    ) {
                        HeatingScheduleCheckbox(viewData.enabled) { viewModel.didChangeEnabled(it, context) }
                    }

                    TimePeriodView(
                        viewData.timePeriod1, stringResource(R.string.period_1), onChange = { viewModel.didChangeTimePeriod1(it, context) })
                    TimePeriodView(
                        viewData.timePeriod2, stringResource(R.string.period_2), { viewModel.didChangeTimePeriod2(it, context) })
                    TimePeriodView(
                        viewData.timePeriod3, stringResource(R.string.period_3), { viewModel.didChangeTimePeriod3(it, context) })

                    SettingsColumn {
                        TemperatureRangeSlider(
                            viewData.startTemperature,
                            viewData.endTemperature,
                            viewData.minStartTemperature.toFloat()..viewData.maxStartTemperature.toFloat(),
                            viewData.minEndTemperature.toFloat()..viewData.maxEndTemperature.toFloat(),
                            { lower, upper -> viewModel.didChangeTemperatures(lower, upper, context) }
                        )
                    }

                    SettingsColumn(footer = stringResource(R.string.battery_heating_footer)) { }
                    SettingsBottomSpace()
                }
            }, modifier = modifier
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TemperatureRangeSlider(
        lower: Double,
        upper: Double,
        lowerRange: ClosedFloatingPointRange<Float>,
        upperRange: ClosedFloatingPointRange<Float>,
        onUpdate: (Double, Double) -> Unit
    ) {
        var sliderPosition by remember { mutableStateOf(lower.toFloat()..upper.toFloat()) }
        val range = lowerRange.start..upperRange.endInclusive

        SettingsColumn(
            header = stringResource(R.string.temperatures),
            footer = stringResource(R.string.minimum_and_maximum_temperature_ranges_are_controlled_by_your_inverter_firmware)
        ) {
            RangeSlider(
                value = sliderPosition,
                steps = 19,
                valueRange = range,
                onValueChange = { range ->
                    val lower = range.start.coerceIn(lowerRange)
                    val upper = range.endInclusive.coerceIn(upperRange)
                    sliderPosition = lower..upper
                    onUpdate(lower.toDouble(), upper.toDouble())
                },
                startThumb = {
                    SliderBubble(value = sliderPosition.start.roundToInt())
                },
                endThumb = {
                    SliderBubble(value = sliderPosition.endInclusive.roundToInt())
                },
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = range.start.celsius)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = range.endInclusive.celsius)
            }
        }
    }

    @Composable
    private fun SliderBubble(value: Int) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            Text(
                text = value.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }

    @Composable
    private fun HeatingScheduleCheckbox(enabled: Boolean, onUpdate: (Boolean) -> Unit) {
        val state = remember { mutableStateOf(enabled) }

        SettingsCheckbox(
            stringResource(R.string.heating_schedule_enabled),
            state = state,
            onUpdate = { onUpdate(state.value) }
        )
    }

    @Composable
    private fun TimePeriodView(timePeriod: ChargeTimePeriod, periodTitle: String, onChange: (ChargeTimePeriod) -> Unit) {
        val textColor = remember { mutableStateOf(Color.Black) }
        val state = remember { mutableStateOf(timePeriod.enabled) }

        SettingsColumn(
            header = periodTitle
        ) {
            SettingsCheckbox(
                stringResource(R.string.enable_heating),
                state = state,
                onUpdate = { onChange(ChargeTimePeriod(start = timePeriod.start, end = timePeriod.end, enabled = it)) }
            )

            AnimatedVisibility(visible = timePeriod.enabled) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    TimePeriodView(
                        timePeriod.start, TimeType.START, stringResource(R.string.start), labelStyle = TextStyle(color = textColor.value), includeSeconds = false
                    ) { hour, minute ->
                        onChange(ChargeTimePeriod(start = Time(hour, minute), end = timePeriod.end, enabled = timePeriod.enabled))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    TimePeriodView(
                        timePeriod.end,
                        TimeType.END,
                        stringResource(R.string.end),
                        labelStyle = TextStyle(color = textColor.value),
                        includeSeconds = false,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) { hour, minute ->
                        onChange(ChargeTimePeriod(start = timePeriod.start, end = Time(hour, minute), enabled = timePeriod.enabled))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BatteryHeatingScheduleSettingsViewPreview() {
    EnergyStatsTheme {
        BatteryHeatingScheduleSettingsView(
            network = DemoNetworking(), configManager = FakeConfigManager(), navController = NavController(LocalContext.current), FakeUserManager()
        ).ScheduleAvailable(
            BatteryHeatingScheduleSettingsViewModel(
                DemoNetworking(), FakeConfigManager()
            ), Modifier
        )
    }
}