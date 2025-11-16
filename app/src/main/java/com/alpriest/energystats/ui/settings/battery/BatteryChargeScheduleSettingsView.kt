package com.alpriest.energystats.ui.settings.battery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsBottomSpace
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPaddingValues
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

enum class TimeType {
    START,
    END;

    fun appendage(): String {
        return when (this) {
            START -> "00"
            END -> "59"
        }
    }
}

data class BatteryChargeScheduleSettingsViewData(
    val summary: String,
    val chargeTimePeriod1: ChargeTimePeriod,
    val chargeTimePeriod2: ChargeTimePeriod
)

class BatteryChargeScheduleSettingsView(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(
        viewModel: BatteryChargeScheduleSettingsViewModel = viewModel(
            factory = BatteryChargeScheduleSettingsViewModelFactory(
                network = network,
                configManager = configManager
            )
        ),
        modifier: Modifier
    ) {
        val chargeSummary = viewModel.summaryStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state
        val context = LocalContext.current
        val viewData = viewModel.viewDataStream.collectAsStateWithLifecycle().value

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(null) {
            viewModel.load(context)
        }
        trackScreenView("Battery Schedule", "BatteryChargeScheduleSettingsView")

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(
                loadState.ex,
                loadState.reason,
                loadState.allowRetry,
                onRetry = { viewModel.load(context) },
                onLogout = { userManager.logout() }
            )

            is LoadState.Inactive ->
                ContentWithBottomButtonPair(
                    navController,
                    onConfirm = { viewModel.save(context) },
                    dirtyStateFlow = viewModel.dirtyState,
                    content = { innerModifier ->
                        SettingsPage(innerModifier) {
                            BatteryTimePeriodView(
                                viewData.chargeTimePeriod1,
                                stringResource(R.string.period_1),
                                onChange = { viewModel.didChangeTimePeriod1(it, context) }
                            )
                            BatteryTimePeriodView(
                                viewData.chargeTimePeriod2,
                                stringResource(R.string.period_2),
                                { viewModel.didChangeTimePeriod2(it, context) }
                            )

                            SettingsColumn(
                                header = stringResource(R.string.schedule_summary),
                                padding = SettingsPaddingValues.withVertical()
                            ) {
                                Text(
                                    chargeSummary,
                                    color = colorScheme.onSecondary
                                )
                            }

                            SettingsBottomSpace()
                        }
                    },
                    modifier = modifier
                )
        }
    }

    @Composable
    fun BatteryTimePeriodView(timePeriod: ChargeTimePeriod, periodTitle: String, onChange: (ChargeTimePeriod) -> Unit) {
        val textColor = remember { mutableStateOf(Color.Black) }

        SettingsColumn(
            header = periodTitle
        ) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.enable_charge_from_grid),
                    color = colorScheme.onSecondary,
                )
                Switch(checked = timePeriod.enabled, onCheckedChange = {
                    onChange(ChargeTimePeriod(start = timePeriod.start, end = timePeriod.end, enabled = it))
                })
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            TimePeriodView(
                timePeriod.start,
                TimeType.START,
                stringResource(R.string.start),
                labelStyle = TextStyle(color = textColor.value),
                includeSeconds = false
            ) { hour, minute ->
                onChange(ChargeTimePeriod(start = Time(hour, minute), end = timePeriod.end, enabled = timePeriod.enabled))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            TimePeriodView(
                timePeriod.end,
                TimeType.END,
                stringResource(R.string.end),
                labelStyle = TextStyle(color = textColor.value),
                includeSeconds = false
            ) { hour, minute ->
                onChange(ChargeTimePeriod(start = timePeriod.start, end = Time(hour, minute), enabled = timePeriod.enabled))
            }

            OutlinedButton(
                onClick = { onChange(ChargeTimePeriod(start = Time.zero(), end = Time.zero(), enabled = false)) },
            ) {
                Text(
                    stringResource(R.string.reset_times),
                    color = colorScheme.onSecondary
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun BatteryForceChargeTimesViewPreview() {
    EnergyStatsTheme {
        BatteryChargeScheduleSettingsView(
            network = DemoNetworking(),
            configManager = FakeConfigManager(),
            navController = NavHostController(LocalContext.current),
            userManager = FakeUserManager()
        ).Content(modifier = Modifier)
    }
}
