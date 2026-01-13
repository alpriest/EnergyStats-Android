package com.alpriest.energystats.ui.flow.earnings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.models.CalculationBreakdown
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.EarningsModel
import com.alpriest.energystats.shared.models.TotalsViewModel
import com.alpriest.energystats.shared.ui.roundedToString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EnergyStatsFinancialModel(private val totalsViewModel: TotalsViewModel, private val configManager: ConfigManaging): ViewModel() {
    lateinit var exportIncome: FinanceAmount
    lateinit var solarSaving: FinanceAmount
    lateinit var total: FinanceAmount
    lateinit var exportBreakdown: CalculationBreakdown
    lateinit var solarSavingBreakdown: CalculationBreakdown
    private var _amountsFlow = MutableStateFlow<List<FinanceAmount>>(emptyList())
    val amountsFlow: StateFlow<List<FinanceAmount>> = _amountsFlow

    init {
        update()

        viewModelScope.launch {
            configManager.appSettingsStream.collect {
                update()
                _amountsFlow.value = amounts()
            }
        }
    }

    fun update() {
        exportIncome = FinanceAmount(
            shortTitleResId = R.string.exported_income_short_title,
            amount = amountForIncomeCalculation * configManager.feedInUnitPrice
        )
        exportBreakdown = CalculationBreakdown(
            formula = "$nameForIncomeCalculation * feedInUnitPrice",
            calculation = { "${amountForIncomeCalculation.roundedToString(it)} * ${configManager.feedInUnitPrice.roundedToString(it)}" }
        )

        solarSaving = FinanceAmount(
            shortTitleResId = R.string.grid_import_avoided_short_title,
            amount = java.lang.Double.max(0.0, totalsViewModel.solar - totalsViewModel.feedIn) * configManager.gridImportUnitPrice
        )
        solarSavingBreakdown = CalculationBreakdown(
            formula = "max(0, solar - gridExport) * gridImportUnitPrice",
            calculation = { "max(0, ${totalsViewModel.solar.roundedToString(it)} - ${totalsViewModel.feedIn.roundedToString(it)}) * ${configManager.gridImportUnitPrice}" }
        )

        total = FinanceAmount(
            shortTitleResId = R.string.total,
            amount = exportIncome.amount + solarSaving.amount
        )

        _amountsFlow.value = listOf(exportIncome, solarSaving, total)
    }

    fun amounts(): List<FinanceAmount> {
        return amountsFlow.value
    }

    private val amountForIncomeCalculation: Double
        get() = when (configManager.earningsModel) {
            EarningsModel.Exported -> totalsViewModel.feedIn
            EarningsModel.Generated -> totalsViewModel.solar
            EarningsModel.CT2 -> totalsViewModel.ct2
        }

    private val nameForIncomeCalculation: String
        get() = when (configManager.earningsModel) {
            EarningsModel.Exported -> "exported"
            EarningsModel.Generated -> "generated"
            EarningsModel.CT2 -> "CT2"
        }
}