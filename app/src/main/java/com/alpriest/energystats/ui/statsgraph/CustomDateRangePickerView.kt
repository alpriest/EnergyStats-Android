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
import androidx.compose.runtime.MutableState
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
import com.alpriest.energystats.shared.helpers.dayMonthFormat
import com.alpriest.energystats.shared.helpers.dayMonthYearFormat
import com.alpriest.energystats.tabs.TopBarSettings
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val CustomDateRangeDisplayUnit.dateFormat: DateTimeFormatter
    get() {
        return when (this) {
            CustomDateRangeDisplayUnit.DAYS -> dayMonthFormat
            CustomDateRangeDisplayUnit.MONTHS -> dayMonthYearFormat
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
    topBarSettings: MutableState<TopBarSettings>,
    initialStart: LocalDate,
    initialEnd: LocalDate,
    initialViewBy: CustomDateRangeDisplayUnit,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate, CustomDateRangeDisplayUnit) -> Unit,
    viewModel: CustomDateRangePickerViewModel = viewModel()
) {
    val context = LocalContext.current
    topBarSettings.value = TopBarSettings(topBarVisible = true, title = stringResource(R.string.choose_custom_range), actions = {}, backButtonAction = null)

    LaunchedEffect(Unit) {
        viewModel.initialise(initialStart, initialEnd, initialViewBy)
    }

    val start by viewModel.start.collectAsState()
    val end by viewModel.end.collectAsState()
    val viewBy by viewModel.viewBy.collectAsState()
    val errorState by viewModel.errorState.collectAsState()

    val startHeader = when (viewBy) {
        CustomDateRangeDisplayUnit.DAYS -> stringResource(R.string.start_day)
        CustomDateRangeDisplayUnit.MONTHS -> stringResource(R.string.start_month)
    }
    val endHeader = when (viewBy) {
        CustomDateRangeDisplayUnit.DAYS -> stringResource(R.string.end_day)
        CustomDateRangeDisplayUnit.MONTHS -> stringResource(R.string.end_month)
    }
    val viewByFooter = when (viewBy) {
        CustomDateRangeDisplayUnit.DAYS -> stringResource(R.string.shows_a_range_of_days_maximum_of_45_days)
        CustomDateRangeDisplayUnit.MONTHS -> stringResource(R.string.shows_a_range_of_months)
    }

    ContentWithBottomButtons(
        footer = {
            Text(
                "${start.format(viewBy.dateFormat)} - ${end.format(viewBy.dateFormat)}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            when (errorState) {
                CustomDateRangePickerError.START_DATE_AFTER_END_DATE -> stringResource(R.string.please_ensure_the_start_date_is_before_the_end_date)
                CustomDateRangePickerError.TIME_PERIOD_NEEDS_MONTHS -> stringResource(R.string.please_choose_months_or_a_shorter_date_range)
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
                    footer = viewByFooter,
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
                    SettingsColumn(header = startHeader) {
                        CalendarView(start) { viewModel.setStart(it) }
                    }

                    SettingsColumn(header = endHeader) {
                        CalendarView(end) { viewModel.setEnd(it) }
                    }
                } else {
                    MonthYearPicker(startHeader, start) {
                        viewModel.setStart(it)
                    }

                    MonthYearPicker(endHeader, end) {
                        viewModel.setEnd(it)
                    }
                }

                Spacer(modifier = Modifier.padding(44.dp))
            }
        },
        buttons = listOf(
            BottomButtonConfiguration(title = stringResource(R.string.cancel), onTap = { onDismiss() }),
            BottomButtonConfiguration(title = stringResource(R.string.save), viewModel.dirty, onTap = { onConfirm(start, end, viewBy) }),
        )
    )
}

@Composable
fun MonthYearPicker(header: String, date: LocalDate, onChange: (LocalDate) -> Unit) {
    SettingsColumn(header = header) {
        Row(Modifier.fillMaxWidth()) {
            MonthPicker(
                date.monthValue,
                modifier = Modifier.weight(1.0f),
                textModifier = Modifier.fillMaxWidth(),
                onPrimary = false
            ) {
                onChange(date.withMonth(it))
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
@Preview(showBackground = false, locale = "de")
@Composable
private fun CustomDateRangePickerViewPreview() {
    val settings = remember { mutableStateOf(TopBarSettings(topBarVisible = true, title = "Choose", actions = {}, backButtonAction = null)) }

    EnergyStatsTheme {
        CustomDateRangePickerView(
            topBarSettings = settings,
            initialStart = LocalDate.now(),
            initialEnd = LocalDate.now(),
            initialViewBy = CustomDateRangeDisplayUnit.MONTHS,
            onDismiss = {},
            onConfirm = { _, _, _ -> },
            viewModel = CustomDateRangePickerViewModel()
        )
    }
}
