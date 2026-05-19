package com.alpriest.energystats.ui.summary

import android.app.Application
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
import com.alpriest.energystats.shared.ui.roundedToString
import com.alpriest.energystats.tabs.TopBarSettings
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import io.dontsayboj.rollingnumbers.RollingNumbers
import io.dontsayboj.rollingnumbers.model.DefaultAnimationDuration
import io.dontsayboj.rollingnumbers.ui.Utils
import kotlinx.coroutines.flow.StateFlow

enum class SummaryScreen {
    Overview,
    EditSummaryDateRanges
}

class SummaryView(
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val application: Application,
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
        val appSettings = appSettingsStream.collectAsStateWithLifecycle().value
        val viewData = viewModel.viewDataStream.collectAsStateWithLifecycle().value
        val isLoading = viewModel.loadStateStream.collectAsStateWithLifecycle().value.state

        MonitorAlertDialog(viewModel)

        LaunchedEffect(viewModel) {
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
                    viewData?.let {
                        LoadedView(
                            viewData,
                            appSettings = appSettings
                        )
                    }

                    SolarForecastView(
                        solarForecastProvider,
                        appSettingsStream,
                        configManager,
                        network,
                        application
                    ).Content(modifier = Modifier.padding(top = 28.dp))
                }
            }
        }
    }

    @Composable
    fun LoadedView(viewData: SummaryViewData, appSettings: AppSettings) {
        val style = typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface)
        val oldestDataDate = viewData.oldestDataDate
        val latestDataDate = viewData.latestDataDate
        val hasPV = viewData.hasPV

        EnergySummaryRow(
            stringResource(R.string.home_usage),
            viewData.homeUsage,
            textStyle = style
        )
        if (hasPV) {
            EnergySummaryRow(
                stringResource(R.string.solar_generated),
                viewData.solar,
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

        viewData.financialData?.let { financialData ->
            MoneySummaryRow(
                title = stringResource(R.string.export_income),
                amount = financialData.exportIncome,
                textStyle = style,
                currencySymbol = appSettings.currencySymbol
            )
            MoneySummaryRow(
                title = stringResource(R.string.grid_import_avoided),
                amount = financialData.gridImportAvoided,
                textStyle = style,
                currencySymbol = appSettings.currencySymbol
            )
            MoneySummaryRow(
                title = stringResource(R.string.total_benefit),
                amount = financialData.totalBenefit,
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
    private fun MoneySummaryRow(title: String, amount: Double, textStyle: TextStyle, modifier: Modifier = Modifier, currencySymbol: String) {
        var displayAmount by remember {
            mutableStateOf(
                " ".repeat(amount.roundedToString(2).length)
            )
        }

        LaunchedEffect(title) {
            displayAmount = amount.roundedToString(2)
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
            DemoNetworking(),
            application = Application()
        ) { DemoSolarForecasting() }
            .LoadedView(
                SummaryViewData(
                    solar = 54.90,
                    homeUsage = 117.5,
                    financialData = SummaryViewData.FinancialData(
                        exportIncome = 231.10,
                        gridImportAvoided = 10.50,
                        totalBenefit = 99.81
                    ),
                    bestSolar = null,
                    hasPV = true,
                    oldestDataDate = "Aug 2022",
                    latestDataDate = "Present",
                    currencySymbol = "£"
                ),
                appSettings = AppSettings.demo().copy(showGridTotals = true)
            )
    }
}
