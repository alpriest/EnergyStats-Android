package com.alpriest.energystats.ui.summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoAPI
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.FinanceAmount
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.settings.solcast.SolarForecasting
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.preview
import kotlinx.coroutines.flow.MutableStateFlow

class SummaryView(
    private val configManager: ConfigManaging,
    private val userManager: UserManaging,
    private val network: Networking,
    private val solarForecastProvider: () -> SolarForecasting
) {
    @Composable
    fun Content(
        viewModel: SummaryTabViewModel = viewModel(factory = SummaryTabViewModelFactory(network, configManager)),
        themeStream: MutableStateFlow<AppTheme>
    ) {
        val scrollState = rememberScrollState()
        val appTheme = themeStream.collectAsState().value
        val approximations = viewModel.approximationsViewModelStream.collectAsState().value
        val oldestDataDate = viewModel.oldestDataDate.collectAsState().value
        val isLoading = viewModel.loadStateStream.collectAsState().value.state
        val context = LocalContext.current

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                stringResource(R.string.lifetime_summary),
                style = MaterialTheme.typography.h1,
                fontWeight = FontWeight.Bold
            )

            when (isLoading) {
                is LoadState.Active ->
                    Text(stringResource(R.string.loading))

                else -> {
                    approximations?.let {
                        LoadedView(
                            approximationsViewModel = it,
                            appTheme = appTheme,
                            oldestDataDate = oldestDataDate
                        )
                    }

                    SolarForecastView(
                        solarForecastProvider,
                        themeStream,
                    ).Content(modifier = Modifier.padding(top = 44.dp))
                }
            }
        }
    }

    @Composable
    fun LoadedView(approximationsViewModel: ApproximationsViewModel, appTheme: AppTheme, oldestDataDate: String) {
        energySummaryRow(stringResource(R.string.home_usage), approximationsViewModel.homeUsage, textStyle = MaterialTheme.typography.h2)
        energySummaryRow(stringResource(R.string.solar_generated), approximationsViewModel.totalsViewModel?.solar, textStyle = MaterialTheme.typography.h2)

        Spacer(modifier = Modifier.padding(bottom = 22.dp))

        approximationsViewModel.financialModel?.let { energyStatsModel ->
            moneySummaryRow(
                title = stringResource(R.string.export_income),
                amount = energyStatsModel.exportIncome,
                textStyle = MaterialTheme.typography.h2,
                currencySymbol = appTheme.currencySymbol
            )
            moneySummaryRow(
                title = stringResource(R.string.grid_import_avoided),
                amount = energyStatsModel.solarSaving,
                textStyle = MaterialTheme.typography.h2,
                currencySymbol = appTheme.currencySymbol
            )
            moneySummaryRow(
                title = stringResource(R.string.total_benefit),
                amount = energyStatsModel.total,
                textStyle = MaterialTheme.typography.h2,
                currencySymbol = appTheme.currencySymbol
            )
        }

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(
                R.string.includes_data_from_to_present_figures_are_approximate_and_assume_the_buy_sell_energy_prices_remained_constant_throughout_the_period_of_ownership,
                oldestDataDate
            ),
            color = DimmedTextColor,
            fontSize = appTheme.smallFontSize()
        )
    }

    @Composable
    private fun energySummaryRow(title: String, amount: Double?, textStyle: TextStyle, modifier: Modifier = Modifier) {
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
    private fun moneySummaryRow(title: String, amount: FinanceAmount, textStyle: TextStyle, modifier: Modifier = Modifier, currencySymbol: String) {
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
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        SummaryView(
            FakeConfigManager(),
            FakeUserManager(),
            DemoNetworking()
        ) { DemoSolarForecasting() }.Content(themeStream = MutableStateFlow(AppTheme.preview().copy(showGridTotals = true)))
    }
}