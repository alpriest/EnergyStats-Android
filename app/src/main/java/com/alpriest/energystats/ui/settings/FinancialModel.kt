package com.alpriest.energystats.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.SegmentedControl
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

enum class FinancialModel(val value: Int) {
    EnergyStats(0), FoxESS(1);

    fun title(context: Context): String {
        return when (this) {
            EnergyStats -> context.getString(R.string.energy_stats)
            FoxESS -> context.getString(R.string.foxess)
        }
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

@Composable
fun FinancialsSettingsView(config: ConfigManaging) {
//    val showFinancialSummaryState = rememberSaveable { mutableStateOf(config.showFinancialSummary) }
//    val financialModelState = rememberSaveable { mutableStateOf(config.financialModel) }
    val showFinancialSummaryState = rememberSaveable { mutableStateOf(true) }
    val financialModelState = rememberSaveable { mutableStateOf(FinancialModel.EnergyStats) }
    val context = LocalContext.current

    SettingsColumnWithChild {
        SettingsCheckbox(title = "Show financial summary", state = showFinancialSummaryState, onUpdate = {
            config.showFinancialSummary = it
        })

        if (showFinancialSummaryState.value) {
            SettingsSegmentedControl(segmentedControl = {
                val items = listOf(FinancialModel.EnergyStats, FinancialModel.FoxESS)
                SegmentedControl(
                    items = items.map { it.title(context) }, defaultSelectedItemIndex = items.indexOf(financialModelState.value), color = MaterialTheme.colors.primary
                ) {
                    financialModelState.value = items[it]
                    config.financialModel = items[it]
                }
            })

            if (financialModelState.value == FinancialModel.EnergyStats) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .background(MaterialTheme.colors.surface)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        stringResource(R.string.feed_in_unit_price), Modifier.weight(1.0f), style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSecondary
                    )
                    OutlinedTextField(value = "0.05",
                        onValueChange = { /* TODO */ },
                        modifier = Modifier.width(100.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = MaterialTheme.colors.onSecondary),
                        leadingIcon = { Text(config.currencySymbol, color = MaterialTheme.colors.onSecondary) })
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .background(MaterialTheme.colors.surface)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        stringResource(R.string.grid_import_unit_price), Modifier.weight(1.0f), style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSecondary
                    )
                    OutlinedTextField(value = "0.15",
                        onValueChange = { /* TODO */ },
                        modifier = Modifier.width(100.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = MaterialTheme.colors.onSecondary),
                        leadingIcon = { Text(config.currencySymbol, color = MaterialTheme.colors.onSecondary) })
                }
            }
        }
    }

    if (showFinancialSummaryState.value) {
        when (financialModelState.value) {
            FinancialModel.EnergyStats -> {
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
            FinancialModel.FoxESS -> {
                stringResource(R.string.foxess_earnings_calculation_description)
            }
        }
    }
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