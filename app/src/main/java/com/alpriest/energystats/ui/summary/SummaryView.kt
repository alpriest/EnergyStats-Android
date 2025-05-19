package com.alpriest.energystats.ui.summary

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alpriest.energystats.R
import com.alpriest.energystats.TopBarSettings
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.FinanceAmount
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

class SummaryView(
    private val configManager: ConfigManaging,
    private val userManager: UserManaging,
    private val network: Networking,
    private val solarForecastProvider: () -> SolcastCaching
) {
    @Composable
    fun NavigableContent(
        topBarSettings: MutableState<TopBarSettings>,
        navController: NavHostController,
        viewModel: SummaryTabViewModel = viewModel(factory = SummaryTabViewModelFactory(network, configManager)),
        themeStream: MutableStateFlow<AppTheme>
    ) {
        trackScreenView("Summary", "SummaryView")

        NavHost(
            navController = navController,
            startDestination = "Summary"
        ) {
            composable("Summary") {
                topBarSettings.value = TopBarSettings(true, false, "Summary", {
                    ESButton(onClick = { navController.navigate("EditSummaryDateRanges") }) {
                        Image(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                })
                Content(viewModel, themeStream, Modifier)
            }

            composable("EditSummaryDateRanges") {
                topBarSettings.value = TopBarSettings(true, true, "Summary Date Range", {})
                EditSummaryView(Modifier, navController, viewModel)
            }
        }
    }

    @Composable
    private fun Content(
        viewModel: SummaryTabViewModel,
        themeStream: MutableStateFlow<AppTheme>,
        modifier: Modifier
    ) {
        val scrollState = rememberScrollState()
        val appTheme = themeStream.collectAsState().value
        val approximations = viewModel.approximationsViewModelStream.collectAsState().value
        val oldestDataDate = viewModel.oldestDataDate.collectAsState().value
        val isLoading = viewModel.loadStateStream.collectAsState().value.state
        val context = LocalContext.current
        val latestDataDate = viewModel.latestDataDate.collectAsState().value
        val hasPV = viewModel.hasPVStream.collectAsState().value

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp)
        ) {
            when (isLoading) {
                is LoadState.Active -> LoadingView(title = "Loading...")
                else -> {
                    approximations?.let {
                        LoadedView(
                            hasPV = hasPV,
                            approximationsViewModel = it,
                            appTheme = appTheme,
                            oldestDataDate = oldestDataDate,
                            latestDataDate = latestDataDate
                        )
                    }

                    SolarForecastView(
                        solarForecastProvider,
                        themeStream,
                        configManager
                    ).Content(modifier = Modifier.padding(top = 44.dp))
                }
            }
        }
    }

    @Composable
    private fun LoadedView(approximationsViewModel: ApproximationsViewModel, hasPV: Boolean, appTheme: AppTheme, oldestDataDate: String, latestDataDate: String) {
        EnergySummaryRow(stringResource(R.string.home_usage), approximationsViewModel.homeUsage, textStyle = typography.titleLarge)
        if (hasPV) {
            EnergySummaryRow(stringResource(R.string.solar_generated), approximationsViewModel.totalsViewModel?.solar, textStyle = typography.titleLarge)
        } else {
            Text(
                stringResource(R.string.your_inverter_doesn_t_store_pv_generation_data_so_we_can_t_show_historic_solar_data),
                color = DimmedTextColor,
                fontSize = appTheme.smallFontSize()
            )
        }

        Spacer(modifier = Modifier.padding(bottom = 22.dp))

        approximationsViewModel.financialModel?.let { energyStatsModel ->
            MoneySummaryRow(
                title = stringResource(R.string.export_income),
                amount = energyStatsModel.exportIncome,
                textStyle = typography.titleLarge,
                currencySymbol = appTheme.currencySymbol
            )
            MoneySummaryRow(
                title = stringResource(R.string.grid_import_avoided),
                amount = energyStatsModel.solarSaving,
                textStyle = typography.titleLarge,
                currencySymbol = appTheme.currencySymbol
            )
            MoneySummaryRow(
                title = stringResource(R.string.total_benefit),
                amount = energyStatsModel.total,
                textStyle = typography.titleLarge,
                currencySymbol = appTheme.currencySymbol
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
            fontSize = appTheme.smallFontSize()
        )
    }

    @Composable
    private fun EnergySummaryRow(title: String, amount: Double?, textStyle: TextStyle, modifier: Modifier = Modifier) {
        amount?.let {
            Row {
                Text(
                    title,
                    modifier = modifier.weight(1.0f),
                    style = textStyle
                )
                Text(
                    it.energy(displayUnit = DisplayUnit.Kilowatts, decimalPlaces = 0),
                    modifier = modifier,
                    style = textStyle
                )
            }
        }
    }

    @Composable
    private fun MoneySummaryRow(title: String, amount: FinanceAmount, textStyle: TextStyle, modifier: Modifier = Modifier, currencySymbol: String) {
        Row {
            Text(
                title,
                modifier = modifier.weight(1.0f),
                style = textStyle
            )
            Text(
                amount.formattedAmount(currencySymbol),
                modifier = modifier,
                style = textStyle
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        PreviewContextHolder.context = LocalContext.current
        val topBarSettings = remember { mutableStateOf(TopBarSettings(true, false, "Summary", { })) }

        SummaryView(
            FakeConfigManager(),
            FakeUserManager(),
            DemoNetworking()
        ) { DemoSolarForecasting() }
            .NavigableContent(
                topBarSettings,
                navController = NavHostController(LocalContext.current),
                themeStream = MutableStateFlow(AppTheme.demo().copy(showGridTotals = true))
            )
    }
}

@SuppressLint("StaticFieldLeak")
object PreviewContextHolder {
    var context: Context? = null
}
