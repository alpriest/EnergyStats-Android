package com.alpriest.energystats.ui.settings.financial

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.EnergyPriceRate
import com.alpriest.energystats.shared.models.priceOn
import com.alpriest.energystats.shared.ui.roundedToString
import com.alpriest.energystats.ui.settings.SettingsColumn
import java.text.NumberFormat
import java.text.ParsePosition
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun EnergyPricesView(configManager: ConfigManaging, initialExport: Boolean) {
    var exporting by remember { mutableStateOf(initialExport) }
    var revision by remember { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<EnergyPriceRate?>(null) }
    val rates = remember(exporting, revision) {
        (if (exporting) configManager.feedInPriceRates else configManager.gridImportPriceRates).sortedByDescending { it.effectiveFrom ?: "" }
    }
    fun save(updated: List<EnergyPriceRate>) {
        if (exporting) configManager.feedInPriceRates = updated else configManager.gridImportPriceRates = updated
        revision++
        editing = null
    }
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    SecondaryTabRow(selectedTabIndex = if (exporting) 0 else 1) {
        Tab(
            selected = exporting,
            onClick = { exporting = true },
            text = { Text(stringResource(R.string.export_prices)) }
        )
        Tab(
            selected = !exporting,
            onClick = { exporting = false },
            text = { Text(stringResource(R.string.import_prices)) }
        )
    }
    SettingsColumn(header = stringResource(if (exporting) R.string.export_prices else R.string.import_prices), footer = stringResource(R.string.energy_prices_apportioned)) {
        rates.forEach { rate ->
            Column(Modifier.fillMaxWidth().clickable { editing = rate }.padding(vertical = 12.dp)) {
                Text(configManager.currencySymbol + rate.price.roundedToString(2), style = MaterialTheme.typography.bodyLarge)
                val nextDate = rates.mapNotNull { it.effectiveFrom }.minOrNull()
                Text(when {
                    rate.effectiveFrom != null -> stringResource(R.string.price_applies_from, LocalDate.parse(rate.effectiveFrom).format(formatter))
                    nextDate != null -> stringResource(R.string.price_before, LocalDate.parse(nextDate).format(formatter))
                    else -> stringResource(R.string.default_price)
                }, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider()
        }
        TextButton(onClick = { editing = EnergyPriceRate(rates.priceOn(LocalDate.now()), LocalDate.now().toString()) }) {
            Text(stringResource(R.string.add_price_change))
        }
    }
    editing?.let { rate ->
        val context = LocalContext.current
        var price by remember(rate.id) { mutableStateOf(NumberFormat.getNumberInstance().apply { isGroupingUsed = false; maximumFractionDigits = 20 }.format(rate.price)) }
        var date by remember(rate.id) { mutableStateOf(rate.effectiveFrom?.let(LocalDate::parse)) }
        val position = ParsePosition(0)
        val amount = NumberFormat.getNumberInstance().parse(price, position)?.toDouble()
        val valid = amount != null && amount.isFinite() && position.index == price.length
        AlertDialog(onDismissRequest = { editing = null }, title = { Text(stringResource(R.string.energy_prices)) }, text = {
            Column {
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text(configManager.currencySymbol) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = !valid)
                date?.let { selected ->
                    TextButton(onClick = {
                        DatePickerDialog(context, { _, year, month, day -> date = LocalDate.of(year, month + 1, day) }, selected.year, selected.monthValue - 1, selected.dayOfMonth).show()
                    }) { Text(stringResource(R.string.price_applies_from, selected.format(formatter))) }
                }
                if (rate.effectiveFrom != null && rates.any { it.id == rate.id }) {
                    TextButton(onClick = { save(rates.filterNot { it.id == rate.id }) }) { Text(stringResource(R.string.delete)) }
                }
            }
        }, confirmButton = {
            TextButton(enabled = valid, onClick = {
                val updated = rate.copy(price = amount!!, effectiveFrom = date?.toString())
                save(rates.filterNot { it.id == rate.id || it.effectiveFrom == updated.effectiveFrom } + updated)
            }) { Text(stringResource(R.string.save)) }
        }, dismissButton = { TextButton(onClick = { editing = null }) { Text(stringResource(R.string.cancel)) } })
    }
}

@Preview(showBackground = true)
@Composable
fun EnergyPricesViewPreview() {
    EnergyPricesView(configManager = FakeConfigManager(), initialExport = true)
}