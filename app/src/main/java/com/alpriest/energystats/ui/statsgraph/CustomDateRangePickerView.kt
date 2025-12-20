package com.alpriest.energystats.ui.statsgraph

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    fun title(context: Context): String {
        return when (this) {
            DAYS -> context.getString(R.string.days)
            MONTHS -> context.getString(R.string.months)
        }
    }
}

@Composable
fun CustomDateRangePickerView(
    initialStart: LocalDate,
    initialEnd: LocalDate,
    initialViewBy: CustomDateRangeDisplayUnit,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate, CustomDateRangeDisplayUnit) -> Unit,
    viewModel: CustomDateRangePickerViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initialise(initialStart, initialEnd, initialViewBy)
    }

    val start by viewModel.start.collectAsState()
    val end by viewModel.end.collectAsState()
    val viewBy by viewModel.viewBy.collectAsState()
    val dirty by viewModel.dirty.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val startHeader = remember { mutableStateOf("") }
    val endHeader = remember { mutableStateOf("") }
    val viewByFooter = remember { mutableStateOf("") }

    val dirtyStateFlow = remember { MutableStateFlow(false) }
    LaunchedEffect(dirty) { dirtyStateFlow.value = dirty }

    ContentWithBottomButtons(
        footer = {
            Text(
                "${start.format(viewBy.dateFormat)} - ${end.format(viewBy.dateFormat)}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            when (errorState) {
                CustomDateRangePickerError.START_DATE_AFTER_END_DATE -> "Please ensure the start date is before the end date."
                CustomDateRangePickerError.TIME_PERIOD_NEEDS_MONTHS -> "Please choose months or a shorter date range."
                else -> null
            }?.let {
                Text(
                    it,
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        },
        content = {
            SettingsPage {
                SettingsColumn(header = stringResource(R.string.quick_choice)) {
                    Row {
                        ESButton(
                            onClick = {
                                viewModel.setLastMonths(12, context)
                            },
                            content = {
                                Text(stringResource(R.string.last_12_months))
                            }
                        )
                    }

                    Row {
                        ESButton(
                            onClick = {
                                viewModel.setLastMonths(6, context)
                            },
                            content = {
                                Text(stringResource(R.string.last_6_months))
                            }
                        )
                    }
                }

                SettingsColumn(
                    header = stringResource(R.string.view_by),
                    footer = viewByFooter.value
                ) {
                    SettingsSegmentedControl(
                        segmentedControl = {
                            val items = CustomDateRangeDisplayUnit.entries
                            SegmentedControl(
                                items = items.map { it.title(context) },
                                defaultSelectedItemIndex = items.indexOf(viewBy),
                                color = colorScheme.primary
                            ) {
                                viewModel.setViewBy(items[it])
                            }
                        }
                    )
                }

                if (viewBy == CustomDateRangeDisplayUnit.DAYS) {
                    SettingsColumn(header = startHeader.value) {
                        CalendarView(start) { viewModel.setStart(it) }
                    }

                    SettingsColumn(header = endHeader.value) {
                        CalendarView(end) { viewModel.setEnd(it) }
                    }
                } else {
                    MonthYearPicker(startHeader.value, start) {
                        viewModel.setStart(it)
                    }

                    MonthYearPicker(endHeader.value, end) {
                        viewModel.setEnd(it)
                    }
                }

                Spacer(modifier = Modifier.padding(44.dp))
            }
        },
        buttons = listOf(
            BottomButtonConfiguration(title = stringResource(R.string.cancel), onTap = { onDismiss() }),
            BottomButtonConfiguration(title = stringResource(R.string.save), dirtyStateFlow, onTap = { onConfirm(start, end, viewBy) }),
        )
    )

    LaunchedEffect(viewBy) {
        when (viewBy) {
            CustomDateRangeDisplayUnit.DAYS -> {
                startHeader.value = "Start day"
                endHeader.value = "End day"
                viewByFooter.value = "Shows a range of days. Maximum of 45 days"
            }

            CustomDateRangeDisplayUnit.MONTHS -> {
                startHeader.value = "Start month"
                endHeader.value = "End month"
                viewByFooter.value = "Shows a range of months"
            }
        }
    }
}

@Composable
fun MonthYearPicker(header: String, date: LocalDate, onChange: (LocalDate) -> Unit) {
    SettingsColumn(header = header) {
        Row(Modifier.fillMaxWidth()) {
            MonthPicker(
                date.monthValue - 1,
                modifier = Modifier.weight(1.0f),
                textModifier = Modifier.fillMaxWidth(),
                onPrimary = false
            ) {
                onChange(date.withMonth(it + 1))
            }

            YearPicker(
                date.year,
                modifier = Modifier.weight(1.0f),
                onPrimary = false
            ) {
                onChange(date.withYear(it))
            }
        }
    }
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
            onConfirm = { _, _, _ -> },
            viewModel = CustomDateRangePickerViewModel()
        )
    }
}
