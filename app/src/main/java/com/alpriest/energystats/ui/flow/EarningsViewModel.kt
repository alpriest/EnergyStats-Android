    package com.alpriest.energystats.ui.flow

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.alpriest.energystats.ui.settings.financial.EarningsModel
import com.alpriest.energystats.ui.theme.AppTheme
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.Double.max

    @Composable
fun SubLabelledView(value: String, label: String, alignment: Alignment.Horizontal) {
    Column(
        horizontalAlignment = alignment,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            value,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            label.uppercase(),
            fontSize = 8.sp
        )
    }
}

@Composable
fun EarningsView(themeStream: MutableStateFlow<AppTheme>, viewModel: EarningsViewModel?) {
    val context = LocalContext.current
    val appTheme = themeStream.collectAsState().value

    Row(
        modifier = Modifier.let {
            if (viewModel == null) {
                it.shimmer()
            } else {
                it
            }
        }
    ) {
        viewModel?.let {
            it.amounts().forEach {
                SubLabelledView(
                    value = it.formattedAmount(appTheme.currencySymbol),
                    label = it.title(context),
                    alignment = Alignment.CenterHorizontally
                )
            }
        }
    }
}

class FinanceAmount(val shortTitleResId: Int, val amount: Double) {
    fun formattedAmount(currencySymbol: String): String {
        return amount.roundedToString(2, currencySymbol)
    }

    fun title(context: Context): String {
        return context.getString(shortTitleResId)
    }
}

class EnergyStatsFinancialModel(totalsViewModel: TotalsViewModel, configManager: ConfigManaging) {
    val exportIncome: FinanceAmount
    val solarSaving: FinanceAmount
    val total: FinanceAmount
    val exportBreakdown: CalculationBreakdown
    val solarSavingBreakdown: CalculationBreakdown

    init {
        val amountForIncomeCalculation = if (configManager.earningsModel == EarningsModel.Exported) totalsViewModel.feedIn else totalsViewModel.solar
        exportIncome = FinanceAmount(
            shortTitleResId = R.string.exported_income_short_title,
            amount = amountForIncomeCalculation * configManager.feedInUnitPrice
        )
        exportBreakdown = CalculationBreakdown(
            formula = "gridExport * feedInUnitPrice",
            calculation = { "${totalsViewModel.feedIn.roundedToString(it)} * ${configManager.feedInUnitPrice.roundedToString(it)}" }
        )

        solarSaving = FinanceAmount(
            shortTitleResId = R.string.grid_import_avoided_short_title,
            amount = max(0.0, totalsViewModel.solar - totalsViewModel.feedIn) * configManager.gridImportUnitPrice
        )
        solarSavingBreakdown = CalculationBreakdown(
            formula = "max(0, solar - gridExport) * gridImportUnitPrice",
            calculation = { "max(0, ${totalsViewModel.solar.roundedToString(it)} - ${totalsViewModel.feedIn.roundedToString(it)}) * ${configManager.gridImportUnitPrice}" }
        )

        total = FinanceAmount(
            shortTitleResId =  R.string.total,
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
                    totalsViewModel = TotalsViewModel(listOf(OpenReportResponse("raw", unit = "kW", listOf())), true),
                    configManager = FakeConfigManager()
                )
            )
        }
    }
}