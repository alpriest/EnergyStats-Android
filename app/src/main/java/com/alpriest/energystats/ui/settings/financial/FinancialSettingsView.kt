package com.alpriest.energystats.ui.settings.financial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.SettingsCheckbox
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

@Composable
fun FinancialsSettingsView(config: ConfigManaging) {
    val showFinancialSummaryState = rememberSaveable { mutableStateOf(config.showFinancialSummary) }
    val showFinancialSummaryOnFlowPageState = rememberSaveable { mutableStateOf(config.showFinancialSummaryOnFlowPage) }
    val feedInUnitPrice = rememberSaveable { mutableStateOf(config.feedInUnitPrice.toCurrency()) }
    val gridImportUnitPrice = rememberSaveable { mutableStateOf(config.gridImportUnitPrice.toCurrency()) }

    SettingsColumnWithChild {
        SettingsCheckbox(title = stringResource(R.string.show_financial_summary), state = showFinancialSummaryState, onUpdate = {
            config.showFinancialSummary = it

            if (!it) config.showFinancialSummaryOnFlowPage = false
        })

        if (showFinancialSummaryState.value) {
            SettingsCheckbox(title = stringResource(R.string.show_on_flow_page), state = showFinancialSummaryOnFlowPageState, onUpdate = {
                config.showFinancialSummaryOnFlowPage = it
            })

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .background(colors.surface)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    stringResource(R.string.feed_in_unit_price), Modifier.weight(1.0f), style = MaterialTheme.typography.body2, color = colors.onSecondary
                )

                Text(
                    config.currencySymbol,
                    color = colors.onSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )

                TextField(
                    value = feedInUnitPrice.value,
                    onValueChange = {
                        feedInUnitPrice.value = it
                        config.feedInUnitPrice = it.safeToDouble()
                    },
                    modifier = Modifier
                        .width(90.dp)
                        .defaultMinSize(
                            minWidth = TextFieldDefaults.MinWidth,
                            minHeight = 44.dp
                        ),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.End,
                        color = colors.onSecondary
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .background(colors.surface)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    stringResource(R.string.grid_import_unit_price), Modifier.weight(1.0f), style = MaterialTheme.typography.body2, color = colors.onSecondary
                )

                Text(
                    config.currencySymbol,
                    color = colors.onSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )

                TextField(
                    value = gridImportUnitPrice.value,
                    onValueChange = {
                        gridImportUnitPrice.value = it
                        config.gridImportUnitPrice = it.safeToDouble()
                    },
                    modifier = Modifier
                        .width(90.dp)
                        .defaultMinSize(
                            minWidth = TextFieldDefaults.MinWidth,
                            minHeight = 44.dp
                        ),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.End,
                        color = colors.onSecondary
                    )
                )
            }
        }
    }

    if (showFinancialSummaryState.value) {
        Text(stringResource(R.string.energy_stats_earnings_calculation_description))

        CalculationDescription(
            stringResource(R.string.exported_income_short_title), stringResource(R.string.exported_income_description), stringResource(R.string.exported_income_formula)
        )

        CalculationDescription(
            stringResource(R.string.grid_import_avoided_short_title), stringResource(R.string.grid_import_description), stringResource(R.string.grid_import_formula)
        )

        CalculationDescription(
            stringResource(R.string.total),
            stringResource(R.string.total_formula_description),
            "${stringResource(R.string.exported_income_short_title)} + ${stringResource(R.string.grid_import_avoided_short_title)}"
        )
    }
}

private fun Double.toCurrency(): String {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val currencySymbol = currencyFormat.currency.symbol
    return currencyFormat.format(this).replace(currencySymbol, "").trim()
}

private fun String.safeToDouble(): Double {
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
    try {
        val parsedNumber = numberFormat.parse(this)
        if (parsedNumber != null) {
            return parsedNumber.toDouble()
        }
    } catch (e: ParseException) {
        return 0.0
    }

    return 0.0
}

@Composable
fun CalculationDescription(title: String, description: String, formula: String) {
    Column(modifier = Modifier.padding(top = 18.dp)) {
        Text(
            title, style = TextStyle.Default.copy(fontWeight = FontWeight.Bold)
        )
        Text(description)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                formula, fontStyle = FontStyle.Italic, modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Preview
@Composable
fun FinancialsSettingsViewPreview() {
    EnergyStatsTheme {
        FinancialsSettingsView(
            config = FakeConfigManager()
        )
    }
}