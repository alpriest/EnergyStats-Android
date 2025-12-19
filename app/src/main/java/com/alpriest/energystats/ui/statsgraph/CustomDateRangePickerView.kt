package com.alpriest.energystats.ui.statsgraph

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.helpers.CalendarView
import com.alpriest.energystats.ui.helpers.MonthPicker
import com.alpriest.energystats.ui.helpers.SegmentedControl
import com.alpriest.energystats.ui.helpers.YearPicker
import com.alpriest.energystats.ui.settings.BottomButtonConfiguration
import com.alpriest.energystats.ui.settings.ContentWithBottomButtons
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsSegmentedControl
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val CustomDateRangeDisplayUnit.dateFormat: DateTimeFormatter
    get() {
        return when (this) {
            CustomDateRangeDisplayUnit.DAYS -> DateTimeFormatter.ofPattern("d MMM")
            CustomDateRangeDisplayUnit.MONTHS -> DateTimeFormatter.ofPattern("d MMM yyyy")
        }
    }

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
    var start by remember(initialStart) { mutableStateOf(initialStart) }
    var end by remember(initialEnd) { mutableStateOf(initialEnd) }
    var viewBy by remember(initialViewBy) { mutableStateOf(initialViewBy) }

    val dirty by remember(initialStart, initialEnd, start, end) {
        derivedStateOf { start != initialStart || end != initialEnd }
    }

    val dirtyStateFlow = remember { MutableStateFlow(false) }
    LaunchedEffect(dirty) {
        dirtyStateFlow.value = dirty
    }

    var startHeader: String
    var endHeader: String
    var viewByFooter: String

    when (viewBy) {
        CustomDateRangeDisplayUnit.DAYS -> {
            startHeader = "Start day"
            endHeader = "End day"
            viewByFooter = "Shows a range of days"
        }

        CustomDateRangeDisplayUnit.MONTHS -> {
            startHeader = "Start year"
            endHeader = "End year"
            viewByFooter = "Shows a range of months"
        }
    }

    ContentWithBottomButtons(
        content = {
            SettingsPage {
                SettingsColumn(header = "Quick choice") {
                    Row {
                        ESButton(
                            onClick = {
                                val now = LocalDate.now()
                                end = now.plusMonths(1).withDayOfMonth(1).minusDays(1)
                                start = end.minusMonths(11).withDayOfMonth(1)
                            },
                            content = {
                                Text("Last 12 months")
                            }
                        )
                    }

                    Row {
                        ESButton(
                            onClick = {
                                val now = LocalDate.now()
                                end = now.plusMonths(1).withDayOfMonth(1).minusDays(1)
                                start = end.minusMonths(5).withDayOfMonth(1)
                            },
                            content = {
                                Text("Last 6 months")
                            }
                        )
                    }
                }

                SettingsColumn(
                    header = "View by",
                    footer = viewByFooter
                ) {
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
                    SettingsColumn(header = startHeader) {
                        CalendarView(start, { start = it })
                    }

                    SettingsColumn(header = endHeader) {
                        CalendarView(end, { end = it })
                    }
                } else {
                    SettingsColumn(header = startHeader) {
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

                    SettingsColumn(header = endHeader) {
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

                Text("Range: ${start.format(viewBy.dateFormat)} - ${end.format(viewBy.dateFormat)}")
            }
        },
        buttons = listOf(
            BottomButtonConfiguration(title = stringResource(R.string.cancel), onTap = { onDismiss() }),
            BottomButtonConfiguration(title = stringResource(R.string.save), dirtyStateFlow, onTap = { onConfirm(start, end) }),
        )
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = false)
@Composable
private fun CustomDateRangePickerViewPreview() {
    EnergyStatsTheme {
        CustomDateRangePickerView(
            initialStart = LocalDate.now(),
            initialEnd = LocalDate.now(),
            initialViewBy = CustomDateRangeDisplayUnit.MONTHS,
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}
