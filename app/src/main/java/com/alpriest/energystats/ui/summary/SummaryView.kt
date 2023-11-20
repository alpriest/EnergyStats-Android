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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoFoxESSNetworking
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.FinanceAmount
import com.alpriest.energystats.ui.flow.FinanceAmountType
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.settings.FinancialModel
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

class SummaryView(
    private val configManager: ConfigManaging,
    private val network: FoxESSNetworking
) {
    @Composable
    fun Content(
        viewModel: SummaryTabViewModel = viewModel(
            factory = SummaryTabViewModelFactory(network, configManager)
        ),
        themeStream: MutableStateFlow<AppTheme>
    ) {
        val scrollState = rememberScrollState()
        val appTheme = themeStream.collectAsState().value
        val approximations = viewModel.approximationsViewModelStream.collectAsState().value
        val oldestDataDate = viewModel.oldestDataDate.collectAsState().value
        var isLoading by remember { mutableStateOf(false) }

        LaunchedEffect(null) {
            isLoading = true
            viewModel.load()
            isLoading = false
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                stringResource(R.string.summary),
                style = MaterialTheme.typography.h1,
                fontWeight = FontWeight.Bold
            )

            if (isLoading) {
                Text(stringResource(R.string.loading))
            } else {
                approximations?.let {
                    LoadedView(
                        approximationsViewModel = it,
                        appTheme = appTheme,
                        oldestDataDate = oldestDataDate
                    )
                }
            }
            
            SolarForecastView(viewModel = SolarForecastViewModel(DemoSolarForecasting()))
        }
    }

    @Composable
    fun LoadedView(approximationsViewModel: ApproximationsViewModel, appTheme: AppTheme, oldestDataDate: String) {
        energyRow(stringResource(R.string.home_usage), approximationsViewModel.homeUsage, textStyle = MaterialTheme.typography.h2)
        energyRow(stringResource(R.string.solar_generated), approximationsViewModel.totalsViewModel?.solar, textStyle = MaterialTheme.typography.h2)

        Spacer(modifier = Modifier.padding(bottom = 22.dp))

        when (appTheme.financialModel) {
            FinancialModel.EnergyStats -> {
                approximationsViewModel.financialModel?.let { energyStatsModel ->
                    moneyRow(title = stringResource(R.string.export_income), amount = energyStatsModel.exportIncome.amount, textStyle = MaterialTheme.typography.h2)
                    moneyRow(title = stringResource(R.string.grid_import_avoided), amount = energyStatsModel.solarSaving.amount, textStyle = MaterialTheme.typography.h2)
                    moneyRow(title = stringResource(R.string.total_benefit), amount = energyStatsModel.total.amount, textStyle = MaterialTheme.typography.h2)
                }
            }

            FinancialModel.FoxESS -> {
                approximationsViewModel.earnings?.let { earningsResponse ->
                    moneyRow(title = stringResource(R.string.total_benefit), amount = earningsResponse.cumulate.earnings, textStyle = MaterialTheme.typography.h2)
                }
            }
        }

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Includes data from $oldestDataDate to Present. Figures are approximate and assume the buy/sell energy prices remained constant throughout the period of ownership.",
            color = DimmedTextColor,
            fontSize = appTheme.smallFontSize()
        )
    }

    @Composable
    private fun energyRow(title: String, amount: Double?, textStyle: TextStyle, modifier: Modifier = Modifier) {
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
    private fun moneyRow(title: String, amount: Double, textStyle: TextStyle, modifier: Modifier = Modifier) {
        Row {
            Text(
                title,
                modifier = modifier.weight(1.0f),
                style = textStyle
            )
            Text(
                FinanceAmount(type = FinanceAmountType.TOTAL, amount = amount, currencyCode = "GBP", currencySymbol = "£").formattedAmount(),
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
            DemoFoxESSNetworking()
        ).Content(themeStream = MutableStateFlow(AppTheme.preview().copy(showGridTotals = true)))
    }
}