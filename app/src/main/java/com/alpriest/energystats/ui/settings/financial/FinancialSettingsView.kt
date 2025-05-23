package com.alpriest.energystats.ui.settings.financial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.SegmentedControl
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.SettingsCheckbox
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsSegmentedControl
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

enum class EarningsModel(val value: Int) {
    Exported(0),
    Generated(1),
    CT2(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: Exported
    }
}

@Composable
fun FinancialsSettingsView(config: ConfigManaging) {
    val showFinancialSummaryState = rememberSaveable { mutableStateOf(config.showFinancialSummary) }
    val showFinancialSummaryOnFlowPageState = rememberSaveable { mutableStateOf(config.showFinancialSummaryOnFlowPage) }
    val unitPrice = rememberSaveable { mutableStateOf(config.feedInUnitPrice.toCurrency()) }
    val gridImportUnitPrice = rememberSaveable { mutableStateOf(config.gridImportUnitPrice.toCurrency()) }
    val currencySymbol = rememberSaveable { mutableStateOf(config.currencySymbol) }
    val earningsModel = rememberSaveable { mutableStateOf(config.earningsModel) }
    trackScreenView("Financial Model", "FinancialsSettingsView")

    SettingsColumn(content = {
        SettingsCheckbox(title = stringResource(R.string.show_financial_summary), state = showFinancialSummaryState, onUpdate = {
            config.showFinancialSummary = it

            if (!it) config.showFinancialSummaryOnFlowPage = false
        })
    }, footer = stringResource(R.string.energy_stats_earnings_calculation_description))

    if (showFinancialSummaryState.value) {
        SettingsColumn {
            SettingsCheckbox(title = stringResource(R.string.show_on_flow_page), state = showFinancialSummaryOnFlowPageState, onUpdate = {
                config.showFinancialSummaryOnFlowPage = it
            })

            MakeCurrencySymbolField(config, currencySymbol)
        }

        SettingsColumn(
            content = {
                MakeTextField(config, unitPrice, stringResource(R.string.unit_price)) {
                    unitPrice.value = it
                    config.feedInUnitPrice = it.safeToDouble()
                }

                SettingsSegmentedControl(
                    title = stringResource(R.string.i_am_paid_for),
                    segmentedControl = {
                        val items = EarningsModel.entries.toTypedArray()
                        val itemTitles = listOf(
                            stringResource(R.string.exporting),
                            stringResource(R.string.generating),
                            "CT2"
                        )

                        SegmentedControl(
                            items = itemTitles,
                            defaultSelectedItemIndex = items.indexOf(earningsModel.value),
                            color = colorScheme.primary
                        ) {
                            earningsModel.value = items[it]
                            config.earningsModel = items[it]
                        }
                    }
                )
            },
            footer = when (earningsModel.value) {
                EarningsModel.Generated -> stringResource(R.string.earnings_generated_description)
                EarningsModel.Exported -> stringResource(R.string.earnings_exported_description)
                EarningsModel.CT2 -> stringResource(R.string.earnings_ct2_description)
            }
        )

        SettingsColumn(content = {
            MakeTextField(config, gridImportUnitPrice, stringResource(R.string.grid_import_unit_price)) {
                gridImportUnitPrice.value = it
                config.gridImportUnitPrice = it.safeToDouble()
            }
        }, footer = stringResource(R.string.earnings_imported_description))
    }

    if (showFinancialSummaryState.value) {
        CalculationDescription(
            stringResource(R.string.exported_income_short_title),
            when (earningsModel.value) {
                EarningsModel.Generated -> "Approximate income received from generating electricity."
                EarningsModel.Exported -> stringResource(R.string.exported_income_description)
                EarningsModel.CT2 -> "Approximate income received from exporting energy to the grid via another inverter tracked by CT2."
            },
            stringResource(R.string.exported_income_formula)
        )

        CalculationDescription(
            stringResource(R.string.grid_import_avoided_short_title),
            stringResource(R.string.grid_import_description),
            stringResource(R.string.grid_import_formula)
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
    val currencySymbol = currencyFormat.currency?.symbol ?: ""
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
fun MakeCurrencySymbolField(config: ConfigManaging, state: MutableState<String>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .background(colorScheme.surface)
            .padding(vertical = 4.dp)
    ) {
        Text(
            stringResource(R.string.currency_symbol),
            Modifier.weight(1.0f),
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSecondary
        )

        TextField(
            value = state.value,
            onValueChange = {
                state.value = it
                config.currencySymbol = it
            },
            modifier = Modifier
                .width(90.dp)
                .defaultMinSize(
                    minWidth = TextFieldDefaults.MinWidth,
                    minHeight = 44.dp
                ),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.End,
                color = colorScheme.onSecondary
            )
        )
    }
}

@Composable
private fun MakeTextField(config: ConfigManaging, state: MutableState<String>, label: String, onValueChange: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .background(colorScheme.surface)
            .padding(vertical = 4.dp)
    ) {
        Text(
            label,
            Modifier.weight(1.0f),
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSecondary
        )

        Text(
            config.currencySymbol,
            color = colorScheme.onSecondary,
            modifier = Modifier.padding(end = 8.dp)
        )

        TextField(
            value = state.value,
            onValueChange = onValueChange,
            modifier = Modifier
                .width(90.dp)
                .defaultMinSize(
                    minWidth = TextFieldDefaults.MinWidth,
                    minHeight = 44.dp
                ),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.End,
                color = colorScheme.onSecondary
            )
        )
    }
}

@Composable
fun CalculationDescription(title: String, description: String, formula: String) {
    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
        Text(
            title,
            style = TextStyle.Default.copy(color = colorScheme.onSecondary, fontWeight = FontWeight.Bold)
        )
        Text(
            description,
            color = colorScheme.onSecondary,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                formula,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = colorScheme.onSecondary,
            )
        }
    }
}

@Preview
@Composable
fun FinancialsSettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SettingsPage(Modifier.padding(12.dp)) {
            FinancialsSettingsView(
                config = FakeConfigManager()
            )
        }
    }
}