package com.alpriest.energystats.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.SegmentedControl

@Composable
fun ApproximationsView(config: ConfigManaging) {
    SettingsPage {
        SelfSufficiencySettingsView(config, modifier = Modifier.fillMaxWidth())
    }
}

enum class FinancialModel(val value: Int) {
    EnergyStats(0),
    FoxESS(1);

    fun title(context: Context): String {
        return when (this) {
            EnergyStats -> "Energy Stats"
            FoxESS -> "FoxESS"
        }
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

@Composable
fun FinancialsSettingsView(config: ConfigManaging) {
    val showFinancialSummaryState = rememberSaveable { mutableStateOf(config.showFinancialSummary) }
    val financialModelState = rememberSaveable { mutableStateOf(config.financialModel) }
    val context = LocalContext.current

    SettingsColumnWithChild {
        SettingsCheckbox(title = "Show financial summary", state = showFinancialSummaryState, onConfigUpdate = {
            config.showFinancialSummary = it
        })

        if (showFinancialSummaryState.value) {
            SettingsSegmentedControl(segmentedControl = {
                val items = listOf(FinancialModel.EnergyStats, FinancialModel.FoxESS)
                SegmentedControl(
                    items = items.map { it.title(context) },
                    defaultSelectedItemIndex = items.indexOf(financialModelState.value),
                    color = MaterialTheme.colors.primary
                ) {
                    config.financialModel = items[it]
                }
            }, footer = buildAnnotatedString {
                if (financialModelState.value == FinancialModel.FoxESS) {
                    append("This unit price is managed on the FoxESS Cloud.\n\n")
                    append("Shows earnings today, this month, this year, and all -time based on a calculation of feed - in kWh * unit price.")
                }
            })
        }
    }
}

@Preview
@Composable
fun FinancialsSettingsViewPreview() {
    FinancialsSettingsView(
        config = FakeConfigManager()
    )
}