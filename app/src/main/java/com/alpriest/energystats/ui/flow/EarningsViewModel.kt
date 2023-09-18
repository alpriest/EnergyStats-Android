package com.alpriest.energystats.ui.flow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.theme.AppTheme

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
fun EarningsView(viewModel: EarningsViewModel, appTheme: AppTheme) {
    Row {
        SubLabelledView(
            value = viewModel.today.roundedToString(2, currencyCode = viewModel.currencyCode, currencySymbol = viewModel.currencySymbol),
            label = stringResource(R.string.today),
            alignment = Alignment.CenterHorizontally
        )

        SubLabelledView(
            value = viewModel.month.roundedToString(2, currencyCode = viewModel.currencyCode, currencySymbol = viewModel.currencySymbol),
            label = stringResource(R.string.month),
            alignment = Alignment.CenterHorizontally
        )

        SubLabelledView(
            value = viewModel.year.roundedToString(2, currencyCode = viewModel.currencyCode, currencySymbol = viewModel.currencySymbol),
            label = stringResource(R.string.year),
            alignment = Alignment.CenterHorizontally
        )

        SubLabelledView(
            value = viewModel.cumulate.roundedToString(2, currencyCode = viewModel.currencyCode, currencySymbol = viewModel.currencySymbol),
            label = stringResource(R.string.total),
            alignment = Alignment.CenterHorizontally
        )
    }
}

data class EarningsViewModel(val today: Double, val month: Double, val year: Double, val cumulate: Double, val currencySymbol: String, val currencyCode: String) {
    companion object
}