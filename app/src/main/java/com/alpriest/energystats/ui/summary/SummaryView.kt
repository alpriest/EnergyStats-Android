package com.alpriest.energystats.ui.summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SummaryView() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Summary",
            style = MaterialTheme.typography.h1,
            fontWeight = FontWeight.Bold
        )

        energyRow("Home usage", 442.0, textStyle = MaterialTheme.typography.h2)
        energyRow("Solar generated", 365.0, textStyle = MaterialTheme.typography.h2)
    }
}

@Composable
private fun energyRow(title: String, amount: Double, textStyle: TextStyle, modifier: Modifier = Modifier) {
    Row {
        Text(
            title,
            modifier = modifier.weight(1.0f),
            style = textStyle
        )
        Text(
            amount.energy(displayUnit = DisplayUnit.Kilowatts, decimalPlaces = 0),
            modifier = modifier,
            style = textStyle
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        SummaryView()
    }
}