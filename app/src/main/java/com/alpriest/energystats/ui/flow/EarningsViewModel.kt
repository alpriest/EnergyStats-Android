package com.alpriest.energystats.ui.flow

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Earning
import com.alpriest.energystats.models.EarningsResponse
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.FinancialModel
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SubLabelledView(value: String, label: String, alignment: Alignment.Horizontal) {
    Column(
        horizontalAlignment = alignment,
        modifier = Modifier.padding(end = 8.dp)
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
    val theme by themeStream.collectAsState()

    Row {
        viewModel.amounts(theme.financialModel, context).forEach {
            SubLabelledView(
                value = it.formattedAmount(),
                label = it.title,
                alignment = Alignment.CenterHorizontally
            )
        }
    }
}

class FinanceAmount(val title: String, val amount: Double, val currencyCode: String, val currencySymbol: String) {
    fun formattedAmount(): String {
        return amount.roundedToString(2, currencyCode, currencySymbol)
    }
}

class EarningsViewModel(val response: EarningsResponse) {
    fun amounts(model: FinancialModel, context: Context): List<FinanceAmount> {
        return when (model) {
            FinancialModel.FoxESS ->
                listOf(
                    FinanceAmount(context.getString(R.string.today), response.today.earnings, response.currencyCode(), response.currencySymbol()),
                    FinanceAmount(context.getString(R.string.month), response.month.earnings, response.currencyCode(), response.currencySymbol()),
                    FinanceAmount(context.getString(R.string.year), response.year.earnings, response.currencyCode(), response.currencySymbol()),
                    FinanceAmount(context.getString(R.string.total), response.cumulate.earnings, response.currencyCode(), response.currencySymbol())
                )

            FinancialModel.EnergyStats ->
                listOf<FinanceAmount>()
        }
    }

    companion object {
        fun preview(): EarningsViewModel {
            return EarningsViewModel(
                response = EarningsResponse(
                    today = Earning(1.0, 1.0),
                    month = Earning(5.0, 5.0),
                    year = Earning(50.0, 50.0),
                    cumulate = Earning(500.0, 500.0),
                    currency = "GBP(£)"
                )
            )
        }
    }
}