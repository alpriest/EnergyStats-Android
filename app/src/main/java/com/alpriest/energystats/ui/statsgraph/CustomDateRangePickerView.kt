package com.alpriest.energystats.ui.statsgraph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.helpers.MonthPicker
import com.alpriest.energystats.ui.helpers.SegmentedControl
import com.alpriest.energystats.ui.helpers.YearPicker
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsSegmentedControl
import com.alpriest.energystats.ui.settings.darkenedBackground
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import java.time.LocalDate

enum class CustomDateRangeDisplayUnit {
    DAYS,
    MONTHS;

    val title: String
        get() = when (this) {
            DAYS -> "Days"
            MONTHS -> "Months"
        }
}

@Composable
fun CustomDateRangePickerView() {
    var start by remember { mutableStateOf(LocalDate.now()) }
    var end by remember { mutableStateOf(LocalDate.now()) }
    var viewBy by remember { mutableStateOf(CustomDateRangeDisplayUnit.MONTHS) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .darkenedBackground(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SettingsColumn(header = "Quick choice") {
            Row {
                ESButton(
                    onClick = {
                        start = LocalDate.now().minusMonths(12)
                        end = LocalDate.now()
                    },
                    content = {
                        Text("Last 12 months")
                    }
                )
            }

            Row {
                ESButton(
                    onClick = {
                        start = LocalDate.now().minusMonths(6)
                        end = LocalDate.now()
                    },
                    content = {
                        Text("Last 6 months")
                    }
                )
            }
        }

        SettingsColumn(header = "View by") {
            SettingsSegmentedControl(
                segmentedControl = {
                    val items = CustomDateRangeDisplayUnit.entries
                    SegmentedControl(
                        items = items.map { it.title },
                        defaultSelectedItemIndex = items.indexOf(viewBy),
                        color = colorScheme.primary
                    ) {
                        viewBy = items[it]
                    }
                }
            )
        }

        SettingsColumn(header = "Start") {
            Row(Modifier.fillMaxWidth()) {
                MonthPicker(
                    start.monthValue,
                    modifier = Modifier.weight(1.0f),
                    textModifier = Modifier.fillMaxWidth()
                ) {
                    start = start.withMonth(it)
                }

                YearPicker(start.year, modifier = Modifier.weight(1.0f)) {
                    start = start.withYear(it)
                }
            }
        }

        SettingsColumn(header = "End") {
            Row(Modifier.fillMaxWidth()) {
                MonthPicker(
                    end.monthValue,
                    modifier = Modifier.weight(1.0f),
                    textModifier = Modifier.fillMaxWidth()
                ) {
                    end = end.withMonth(it)
                }

                YearPicker(end.year, modifier = Modifier.weight(1.0f)) {
                    end = end.withYear(it)
                }
            }
        }
//    viewModel.rangeStream.value = DatePickerRange.CUSTOM(LocalDate.now().minusDays(30), LocalDate.now())
    }
}

@Preview(showBackground = false)
@Composable
private fun CustomDateRangePickerViewPreview() {
    EnergyStatsTheme {
        CustomDateRangePickerView()
    }
}
