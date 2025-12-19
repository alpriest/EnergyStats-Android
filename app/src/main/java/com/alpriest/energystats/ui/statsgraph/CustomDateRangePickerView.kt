package com.alpriest.energystats.ui.statsgraph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.helpers.CalendarView
import com.alpriest.energystats.ui.helpers.MonthPicker
import com.alpriest.energystats.ui.helpers.SegmentedControl
import com.alpriest.energystats.ui.helpers.YearPicker
import com.alpriest.energystats.ui.settings.BottomButtonConfiguration
import com.alpriest.energystats.ui.settings.ButtonStrip
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsSegmentedControl
import com.alpriest.energystats.ui.settings.darkenedBackground
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow
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
fun CustomDateRangePickerView(
    initialStart: LocalDate,
    initialEnd: LocalDate,
    initialViewBy: CustomDateRangeDisplayUnit,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit
) {
    var scrollState = rememberScrollState()
    var start by remember(initialStart) { mutableStateOf(initialStart) }
    var end by remember(initialEnd) { mutableStateOf(initialEnd) }
    var viewBy by remember(initialViewBy) { mutableStateOf(initialViewBy) }

    val dirty by remember(initialStart, initialEnd, initialViewBy, start, end, viewBy) {
        derivedStateOf { start != initialStart || end != initialEnd || viewBy != initialViewBy }
    }

    val dirtyStateFlow = remember { MutableStateFlow(false) }
    LaunchedEffect(dirty) {
        dirtyStateFlow.value = dirty
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .darkenedBackground()
            .verticalScroll(scrollState),
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

        if (viewBy == CustomDateRangeDisplayUnit.DAYS) {
            SettingsColumn(header = "Start") {
                CalendarView(start, { start = it })
            }

            SettingsColumn(header = "End") {
                CalendarView(end, { end = it })
            }
        } else {
            SettingsColumn(header = "Start") {
                Row(Modifier.fillMaxWidth()) {
                    MonthPicker(
                        start.monthValue - 1,
                        modifier = Modifier.weight(1.0f),
                        textModifier = Modifier.fillMaxWidth()
                    ) {
                        start = start.withMonth(it + 1)
                    }

                    YearPicker(start.year, modifier = Modifier.weight(1.0f)) {
                        start = start.withYear(it)
                    }
                }
            }

            SettingsColumn(header = "End") {
                Row(Modifier.fillMaxWidth()) {
                    MonthPicker(
                        end.monthValue - 1,
                        modifier = Modifier.weight(1.0f),
                        textModifier = Modifier.fillMaxWidth()
                    ) {
                        end = end.withMonth(it + 1)
                    }

                    YearPicker(end.year, modifier = Modifier.weight(1.0f)) {
                        end = end.withYear(it)
                    }
                }
            }
        }

        ButtonStrip(
            modifier = Modifier
                .background(colorScheme.surface)
                .padding(bottom = 12.dp),
            buttons = listOf(
                BottomButtonConfiguration(title = stringResource(R.string.cancel), onTap = { onDismiss() }),
                BottomButtonConfiguration(title = stringResource(R.string.save), dirtyStateFlow, onTap = { onConfirm(start, end) }),
            )
        )
    }
}

@Preview(showBackground = false)
@Composable
private fun CustomDateRangePickerViewPreview() {
    EnergyStatsTheme {
        CustomDateRangePickerView(
            initialStart = LocalDate.now(),
            initialEnd = LocalDate.now(),
            initialViewBy = CustomDateRangeDisplayUnit.DAYS,
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}
