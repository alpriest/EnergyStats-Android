package com.alpriest.energystats.ui.flow

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.CalculationBreakdown
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SubLabelledView(value: String, label: String, alignment: Alignment.Horizontal) {
    Column(
        horizontalAlignment = alignment,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            value,
            color = MaterialTheme.colors.onBackground
        )
        Text(
            label.uppercase(),
            fontSize = 8.sp
        )
    }
}

@Composable
fun EarningsView(themeStream: MutableStateFlow<AppTheme>, viewModel: EarningsViewModel) {
    val context = LocalContext.current
    val appTheme = themeStream.collectAsState().value

    Row {
        viewModel.amounts().forEach {
            SubLabelledView(
                value = it.formattedAmount(appTheme.currencySymbol),
                label = it.title(context),
                alignment = Alignment.CenterHorizontally
            )
        }
    }
}

enum class FinanceAmountType {
    TODAY,
    EXPORTED,
    AVOIDED,
    TOTAL
}

class FinanceAmount(val type: FinanceAmountType, val amount: Double) {
    fun formattedAmount(currencySymbol: String): String {
        return amount.roundedToString(2, currencySymbol)
    }

    fun title(context: Context): String {
        return when (type) {
            FinanceAmountType.EXPORTED -> context.getString(R.string.exported_income_short_title)
            FinanceAmountType.AVOIDED -> context.getString(R.string.grid_import_avoided_short_title)
            FinanceAmountType.TOTAL -> context.getString(R.string.total)
            FinanceAmountType.TODAY -> context.getString(R.string.today)
        }
    }
}

class EnergyStatsFinancialModel(totalsViewModel: TotalsViewModel, configManager: ConfigManaging) {
    val exportIncome: FinanceAmount
    val solarSaving: FinanceAmount
    val total: FinanceAmount
    val exportBreakdown: CalculationBreakdown
    val solarSavingBreakdown: CalculationBreakdown

    init {
        exportIncome = FinanceAmount(
            type = FinanceAmountType.EXPORTED,
            amount = totalsViewModel.feedIn * configManager.feedInUnitPrice
        )
        exportBreakdown = CalculationBreakdown(
            formula = "gridExport * feedInUnitPrice",
            calculation = { "${totalsViewModel.feedIn.roundedToString(it)} * ${configManager.feedInUnitPrice.roundedToString(it)}" }
        )

        solarSaving = FinanceAmount(
            type = FinanceAmountType.AVOIDED,
            amount = (totalsViewModel.solar - totalsViewModel.feedIn) * configManager.gridImportUnitPrice
        )
        solarSavingBreakdown = CalculationBreakdown(
            formula = "(solar - gridExport) * gridImportUnitPrice",
            calculation = { "(${totalsViewModel.solar.roundedToString(it)} - ${totalsViewModel.feedIn.roundedToString(it)}) * ${configManager.gridImportUnitPrice}" }
        )

        total = FinanceAmount(
            type = FinanceAmountType.TOTAL,
            amount = exportIncome.amount + solarSaving.amount
        )
    }

    fun amounts(): List<FinanceAmount> {
        return listOf(exportIncome, solarSaving, total)
    }
}

class EarningsViewModel(val energyStatsFinancialModel: EnergyStatsFinancialModel) {
    fun amounts(): List<FinanceAmount> {
        return energyStatsFinancialModel.amounts()
    }

    companion object {
        fun preview(): EarningsViewModel {
            return EarningsViewModel(
                energyStatsFinancialModel = EnergyStatsFinancialModel(
                    totalsViewModel = TotalsViewModel(listOf(OpenReportResponse("raw", unit = "kW", listOf()))),
                    configManager = FakeConfigManager()
                )
            )
        }
    }
}