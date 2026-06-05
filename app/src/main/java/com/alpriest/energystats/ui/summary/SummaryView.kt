package com.alpriest.energystats.ui.summary

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
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
import com.alpriest.energystats.ui.settings.InfoButton
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import io.dontsayboj.rollingnumbers.RollingNumbers
import io.dontsayboj.rollingnumbers.model.DefaultAnimationDuration
import io.dontsayboj.rollingnumbers.ui.Utils
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

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
        viewModel: SummaryTabViewModel = viewModel(factory = SummaryTabViewModelFactory(network, configManager, application)),
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
                            appSettings = appSettings,
                            onBestSolarToggle = { viewModel.toggleBestSolarGrouping() }
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
    fun LoadedView(viewData: SummaryViewData, appSettings: AppSettings, onBestSolarToggle: () -> Unit) {
        val style = typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface)
        val oldestDataDate = viewData.oldestDataDate
        val latestDataDate = viewData.latestDataDate
        val hasPV = viewData.hasPV

        Card(modifier = Modifier.padding(bottom = 8.dp)) {
            EnergySummaryRow(
                stringResource(R.string.home_usage),
                viewData.homeUsage,
                textStyle = style,
            )
        }

        if (hasPV) {
            Card(modifier = Modifier.padding(bottom = 8.dp)) {
                EnergySummaryRow(
                    stringResource(R.string.solar_generated),
                    viewData.solar,
                    textStyle = style,
                )
            }

            viewData.bestSolar?.let {
                Card(modifier = Modifier.padding(bottom = 8.dp)) {
                    BestSolarPeriod(it, style, onBestSolarToggle)
                }
            }
        } else {
            Text(
                stringResource(R.string.your_inverter_doesn_t_store_pv_generation_data_so_we_can_t_show_historic_solar_data),
                color = DimmedTextColor,
                fontSize = appSettings.smallFontSize()
            )
        }

        Spacer(modifier = Modifier.padding(bottom = 22.dp))

        viewData.financialData?.let { financialData ->
            FinancialSummaryView(financialData, style, appSettings.currencySymbol)
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
    private fun FinancialSummaryView(financialData: SummaryViewData.FinancialData, style: TextStyle, currencySymbol: String) {
        Card(modifier = Modifier.padding(bottom = 8.dp)) {
            MoneySummaryRow(
                title = stringResource(R.string.export_income),
                amount = financialData.exportIncome,
                textStyle = style,
                currencySymbol = currencySymbol
            )
            MoneySummaryRow(
                title = stringResource(R.string.grid_import_avoided),
                amount = financialData.gridImportAvoided,
                textStyle = style,
                currencySymbol = currencySymbol
            )
            MoneySummaryRow(
                title = stringResource(R.string.total_benefit),
                amount = financialData.totalBenefit,
                textStyle = style,
                currencySymbol = currencySymbol
            )

            financialData.payback?.let { paybackData ->
                SummaryRow(
                    title = stringResource(R.string.time_to_payback),
                    amount = paybackYears(paybackData.paybackMonths),
                    textStyle = style,
                    decimalPlaces = 1,
                    suffix = " " + stringResource(R.string.years),
                    infoButtonText = paybackData.text(stringResource(R.string.assuming_system_was_purchased_around_1s_for_2s))
                )
            }
        }
    }

    private fun paybackYears(months: Int): Double {
        return (months.toDouble() / 12.0)
    }

    @Composable
    private fun BestSolarPeriod(viewData: SummaryViewData.BestSolarData, textStyle: TextStyle, onBestSolarToggle: () -> Unit) {
        val context = LocalContext.current

        Column(Modifier.padding(all = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.best_solar))
                Text(" ")

                Text(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBestSolarToggle
                    ),
                    text = viewData.period.title(context),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            EnergySummaryRow(
                viewData.description,
                viewData.amount,
                textStyle,
                paddingValues = PaddingValues(0.dp)
            )
        }
    }
}

@Composable
private fun SummaryRow(
    title: String,
    amount: Double,
    textStyle: TextStyle,
    paddingValues: PaddingValues = PaddingValues(8.dp),
    decimalPlaces: Int,
    prefix: String = "",
    suffix: String = "",
    infoButtonText: String? = null
) {
    var displayAmount by remember(amount, decimalPlaces) {
        mutableStateOf(
            " ".repeat(amount.roundedToString(decimalPlaces).length)
        )
    }

    LaunchedEffect(title, amount, decimalPlaces) {
        displayAmount = amount.roundedToString(decimalPlaces)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                style = textStyle
            )
            infoButtonText?.let {
                InfoButton(it)
            }
        }

        if (prefix.isNotEmpty()) {
            Text(prefix, style = textStyle)
        }

        RollingNumbers(
            displayAmount,
            characterLists = listOf(" ,." + Utils.provideNumberString()),
            animationDuration = DefaultAnimationDuration.Fast.duration,
            textStyle = textStyle
        )

        if (suffix.isNotEmpty()) {
            Text(suffix, style = textStyle)
        }
    }
}

@Composable
private fun EnergySummaryRow(title: String, amount: Double?, textStyle: TextStyle, paddingValues: PaddingValues = PaddingValues(8.dp)) {
    amount?.let {
        SummaryRow(
            title = title,
            amount = it,
            textStyle = textStyle,
            paddingValues = paddingValues,
            decimalPlaces = 0,
            suffix = " kWh"
        )
    }
}

@Composable
private fun MoneySummaryRow(title: String, amount: Double, textStyle: TextStyle, currencySymbol: String) {
    SummaryRow(
        title = title,
        amount = amount,
        textStyle = textStyle,
        decimalPlaces = 2,
        prefix = currencySymbol
    )
}

@Preview(showBackground = true)
@Composable
fun SummaryViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        PreviewContextHolder.context = LocalContext.current

        Column {
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
                            totalBenefit = 99.81,
                            payback = SummaryViewData.PaybackData(52, "£10,000", LocalDate.now())
                        ),
                        bestSolar = SummaryViewData.BestSolarData(
                            "2025",
                            4519.0,
                            TimeGrouping.YEAR
                        ),
                        hasPV = true,
                        oldestDataDate = "Aug 2022",
                        latestDataDate = "Present",
                        currencySymbol = "£"
                    ),
                    appSettings = AppSettings.demo().copy(showGridTotals = true),
                    {}
                )
        }
    }
}
