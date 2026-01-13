package com.alpriest.energystats.ui.summary

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.helpers.kWh
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.demo
import com.alpriest.energystats.shared.network.DemoNetworking
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.shared.network.PreviewContextHolder
import com.alpriest.energystats.shared.ui.DimmedTextColor
import com.alpriest.energystats.tabs.TopBarSettings
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.earnings.FinanceAmount
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import io.dontsayboj.rollingnumbers.RollingNumbers
import io.dontsayboj.rollingnumbers.model.DefaultAnimationDuration
import io.dontsayboj.rollingnumbers.ui.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class SummaryScreen {
    Overview,
    EditSummaryDateRanges
}

class SummaryView(
    private val configManager: ConfigManaging,
    private val userManager: UserManaging,
    private val network: Networking,
    private val solarForecastProvider: () -> SolcastCaching
) {
    @Composable
    fun NavigableContent(
        topBarSettings: MutableState<TopBarSettings>,
        viewModel: SummaryTabViewModel = viewModel(factory = SummaryTabViewModelFactory(network, configManager)),
        appSettingsStream: StateFlow<AppSettings>
    ) {
        trackScreenView("Summary", "SummaryView")
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = SummaryScreen.Overview.name
        ) {
            composable(SummaryScreen.Overview.name) {
                topBarSettings.value = TopBarSettings(true, stringResource(R.string.summary_title), {
                    ESButton(onClick = { navController.navigate("EditSummaryDateRanges") }) {
                        Image(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }, null)
                Content(viewModel, appSettingsStream, Modifier)
            }

            composable(SummaryScreen.EditSummaryDateRanges.name) {
                topBarSettings.value = TopBarSettings(true, stringResource(R.string.summary_date_range), {}, { navController.popBackStack() })
                EditSummaryView(configManager, navController, onChange = { viewModel.setDateRange(it) }).Content()
            }
        }
    }

    @Composable
    private fun Content(
        viewModel: SummaryTabViewModel,
        appSettingsStream: StateFlow<AppSettings>,
        modifier: Modifier
    ) {
        val scrollState = rememberScrollState()
        val appTheme = appSettingsStream.collectAsStateWithLifecycle().value
        val approximations = viewModel.approximationsViewModelStream.collectAsStateWithLifecycle().value
        val oldestDataDate = viewModel.oldestDataDate.collectAsStateWithLifecycle().value
        val isLoading = viewModel.loadStateStream.collectAsStateWithLifecycle().value.state
        val latestDataDate = viewModel.latestDataDate.collectAsStateWithLifecycle().value
        val hasPV = viewModel.hasPVStream.collectAsStateWithLifecycle().value

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(null) {
            viewModel.load()
        }

        when (isLoading) {
            is LoadState.Active -> LoadingView(isLoading)
            else -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(12.dp)
                ) {
                    approximations?.let {
                        LoadedView(
                            hasPV = hasPV,
                            approximationsViewModel = it,
                            appSettings = appTheme,
                            oldestDataDate = oldestDataDate,
                            latestDataDate = latestDataDate
                        )
                    }

                    SolarForecastView(
                        solarForecastProvider,
                        appSettingsStream,
                        configManager
                    ).Content(modifier = Modifier.padding(top = 44.dp))
                }
            }
        }
    }

    @Composable
    private fun LoadedView(approximationsViewModel: ApproximationsViewModel, hasPV: Boolean, appSettings: AppSettings, oldestDataDate: String, latestDataDate: String) {
        val style = typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface)

        EnergySummaryRow(
            stringResource(R.string.home_usage),
            approximationsViewModel.homeUsage,
            textStyle = style
        )
        if (hasPV) {
            EnergySummaryRow(
                stringResource(R.string.solar_generated),
                approximationsViewModel.totalsViewModel?.solar,
                textStyle = style
            )
        } else {
            Text(
                stringResource(R.string.your_inverter_doesn_t_store_pv_generation_data_so_we_can_t_show_historic_solar_data),
                color = DimmedTextColor,
                fontSize = appSettings.smallFontSize()
            )
        }

        Spacer(modifier = Modifier.padding(bottom = 22.dp))

        approximationsViewModel.financialModel?.let { energyStatsModel ->
            MoneySummaryRow(
                title = stringResource(R.string.export_income),
                amount = energyStatsModel.exportIncome,
                textStyle = style,
                currencySymbol = appSettings.currencySymbol
            )
            MoneySummaryRow(
                title = stringResource(R.string.grid_import_avoided),
                amount = energyStatsModel.solarSaving,
                textStyle = style,
                currencySymbol = appSettings.currencySymbol
            )
            MoneySummaryRow(
                title = stringResource(R.string.total_benefit),
                amount = energyStatsModel.total,
                textStyle = style,
                currencySymbol = appSettings.currencySymbol
            )
        }

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(
                R.string.includes_data_from_to_figures_are_approximate_and_assume_the_buy_sell_energy_prices_remained_constant_throughout_the_period_of_ownership,
                oldestDataDate,
                latestDataDate
            ),
            color = DimmedTextColor,
            fontSize = appSettings.smallFontSize()
        )
    }

    @Composable
    private fun EnergySummaryRow(title: String, amount: Double?, textStyle: TextStyle, modifier: Modifier = Modifier) {

        amount?.let {
            var displayAmount by remember {
                mutableStateOf(
                    " ".repeat(amount.kWh(0, suffix = "").length)
                )
            }

            LaunchedEffect(title) {
                displayAmount = amount.kWh(decimalPlaces = 0, suffix = "")
            }

            Row {
                Text(
                    title,
                    modifier = modifier.weight(1.0f),
                    style = textStyle
                )

                Row {
                    RollingNumbers(
                        displayAmount,
                        characterLists = listOf(" ,." + Utils.provideNumberString()),
                        animationDuration = DefaultAnimationDuration.Fast.duration,
                        textStyle = textStyle
                    )
                    Text(" kWh", style = textStyle)
                }
            }
        }
    }

    @Composable
    private fun MoneySummaryRow(title: String, amount: FinanceAmount, textStyle: TextStyle, modifier: Modifier = Modifier, currencySymbol: String) {
        var displayAmount by remember {
            mutableStateOf(
                " ".repeat(amount.formattedAmount("").length)
            )
        }

        LaunchedEffect(title) {
            displayAmount = amount.formattedAmount("")
        }

        Row {
            Text(
                title,
                modifier = modifier.weight(1.0f),
                style = textStyle
            )

            Row {
                Text(currencySymbol, style = textStyle)
                RollingNumbers(
                    displayAmount,
                    characterLists = listOf(" ,." + Utils.provideNumberString()),
                    animationDuration = DefaultAnimationDuration.Fast.duration,
                    textStyle = textStyle
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        PreviewContextHolder.context = LocalContext.current
        val topBarSettings = remember { mutableStateOf(TopBarSettings(true, "Summary", {}, null)) }

        SummaryView(
            FakeConfigManager(),
            FakeUserManager(),
            DemoNetworking()
        ) { DemoSolarForecasting() }
            .NavigableContent(
                topBarSettings,
                appSettingsStream = MutableStateFlow(AppSettings.demo().copy(showGridTotals = true))
            )
    }
}
