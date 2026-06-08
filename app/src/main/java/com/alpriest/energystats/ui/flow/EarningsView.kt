package com.alpriest.energystats.ui.flow

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
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.ui.flow.earnings.EnergyStatsFinancialModel
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.flow.StateFlow

@Composable
private fun SubLabelledView(value: String, label: String, alignment: Alignment.Horizontal) {
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
fun EarningsView(appSettingsStream: StateFlow<AppSettings>, viewModel: EnergyStatsFinancialModel?) {
    val context = LocalContext.current
    val appSettings = appSettingsStream.collectAsState().value
    val amounts = viewModel?.amountsFlow?.collectAsState()

    Row(
        modifier = Modifier.let {
            if (viewModel == null) {
                it.shimmer()
            } else {
                it
            }
        }
    ) {
        amounts?.let { amounts ->
            amounts.value.forEach {
                SubLabelledView(
                    value = it.formattedAmount(appSettings.currencySymbol),
                    label = it.title(context),
                    alignment = Alignment.CenterHorizontally
                )
            }
        }
    }
}
