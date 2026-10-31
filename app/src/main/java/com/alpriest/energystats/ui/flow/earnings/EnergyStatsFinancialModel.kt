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
import com.alpriest.energystats.shared.models.weightedPrice
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

data class InstallationPaybackEstimate(
    val monthsRemaining: Int
) {
    companion object {
        fun create(
            installationPurchasePrice: Double,
            totalSavingsToDate: Double,
            installationDate: LocalDate
        ): InstallationPaybackEstimate? {
            val monthsSinceInstallation =
                ChronoUnit.MONTHS.between(installationDate, LocalDate.now())

            if (monthsSinceInstallation <= 0) return null
            if (totalSavingsToDate <= 0.0) return null

            val monthlyBenefit = totalSavingsToDate / monthsSinceInstallation
            if (monthlyBenefit <= 0.0) return null

            val remainingCost =
                installationPurchasePrice - totalSavingsToDate

            return InstallationPaybackEstimate(
                monthsRemaining = ceil(remainingCost / monthlyBenefit).toInt()
            )
        }
    }
}

class EnergyStatsFinancialModel(private val totalsViewModel: TotalsViewModel, private val configManager: ConfigManaging, private val periodStart: LocalDate = LocalDate.now(), private val periodEnd: LocalDate = periodStart.plusDays(1)) : ViewModel() {
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

    fun payback(installDate: LocalDate): InstallationPaybackEstimate? {
        return if (configManager.installationPurchasePrice > 0) {
            InstallationPaybackEstimate.create(
                configManager.installationPurchasePrice,
                total.amount,
                installDate
            )
        } else {
            null
        }
    }

    fun update() {
        val exportPrice = configManager.feedInPriceRates.weightedPrice(periodStart, periodEnd)
        val importPrice = configManager.gridImportPriceRates.weightedPrice(periodStart, periodEnd)
        exportIncome = FinanceAmount(
            shortTitleResId = R.string.exported_income_short_title,
            amount = amountForIncomeCalculation * exportPrice
        )
        exportBreakdown = CalculationBreakdown(
            formula = "$nameForIncomeCalculation * weightedExportPrice",
            calculation = { "${amountForIncomeCalculation.roundedToString(it)} * ${exportPrice.roundedToString(it)}" }
        )

        val inverterConsumptionCalcFormulaDescriptionText: String
        val inverterConsumptionCalcFormulaAmountText: (Int) -> String
        val inverterConsumptionCalcAmount: Double
        if (configManager.deductInverterConsumptionFromGridAvoided) {
            inverterConsumptionCalcFormulaDescriptionText = " - inverterConsumption"
            inverterConsumptionCalcFormulaAmountText = { " - ${totalsViewModel.inverterConsumption.roundedToString(it)}" }
            inverterConsumptionCalcAmount = totalsViewModel.inverterConsumption
        } else {
            inverterConsumptionCalcFormulaDescriptionText = ""
            inverterConsumptionCalcFormulaAmountText = { "" }
            inverterConsumptionCalcAmount = 0.0
        }

        solarSaving = FinanceAmount(
            shortTitleResId = R.string.grid_import_avoided_short_title,
            amount = java.lang.Double.max(0.0, totalsViewModel.solar - totalsViewModel.feedIn - inverterConsumptionCalcAmount) * importPrice
        )

        solarSavingBreakdown = CalculationBreakdown(
            formula = "max(0, solar - gridExport$inverterConsumptionCalcFormulaDescriptionText) * weightedImportPrice",
            calculation = { "max(0, ${totalsViewModel.solar.roundedToString(it)} - ${totalsViewModel.feedIn.roundedToString(it)}${inverterConsumptionCalcFormulaAmountText(it)}) * ${importPrice}" }
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