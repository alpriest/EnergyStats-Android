package com.alpriest.energystats.ui.summary

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.FinanceAmount
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.settings.LoadedScaffold
import com.alpriest.energystats.ui.settings.solcast.SolarForecasting
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

class SummaryView(
    private val configManager: ConfigManaging,
    private val userManager: UserManaging,
    private val network: Networking,
    private val solarForecastProvider: () -> SolarForecasting
) {
    @Composable
    fun NavigableContent(
        viewModel: SummaryTabViewModel = viewModel(factory = SummaryTabViewModelFactory(network, configManager)),
        themeStream: MutableStateFlow<AppTheme>
    ) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "Summary"
        ) {
            composable("Summary") {
                LoadedScaffold(title = "Summary",
                    actions = {
                        Button(onClick = { navController.navigate("EditSummaryDateRanges") }) {
                            Image(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                    }) {
                    Content(viewModel, themeStream, it)
                }
            }

            composable("EditSummaryDateRanges") {
                LoadedScaffold(title = "Summary Date Range", navController) {
                    EditSummaryView(it, navController, viewModel)
                }
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
    private fun LoadedView(approximationsViewModel: ApproximationsViewModel, appTheme: AppTheme, oldestDataDate: String) {
        EnergySummaryRow(stringResource(R.string.home_usage), approximationsViewModel.homeUsage, textStyle = typography.titleLarge)
        EnergySummaryRow(stringResource(R.string.solar_generated), approximationsViewModel.totalsViewModel?.solar, textStyle = typography.titleLarge)

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
                R.string.includes_data_from_to_present_figures_are_approximate_and_assume_the_buy_sell_energy_prices_remained_constant_throughout_the_period_of_ownership,
                oldestDataDate
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
        SummaryView(
            FakeConfigManager(),
            FakeUserManager(),
            DemoNetworking()
        ) { DemoSolarForecasting() }.NavigableContent(themeStream = MutableStateFlow(AppTheme.demo().copy(showGridTotals = true)))
    }
}

@SuppressLint("StaticFieldLeak")
object PreviewContextHolder {
    var context: Context? = null
}
