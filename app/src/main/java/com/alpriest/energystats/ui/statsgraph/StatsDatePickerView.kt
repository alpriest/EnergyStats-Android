package com.alpriest.energystats.ui.statsgraph

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.settings.SlimButton
import com.alpriest.energystats.ui.theme.ESButton
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale

sealed class DatePickerRange {
    object DAY : DatePickerRange()
    object MONTH : DatePickerRange()
    object YEAR : DatePickerRange()
    data class CUSTOM(val start: LocalDate, val end: LocalDate) : DatePickerRange()

    fun isCustom(): Boolean {
        return !(this == DAY || this == MONTH || this == YEAR)
    }
}

@Composable
fun StatsDatePickerView(viewModel: StatsDatePickerViewModel, graphShowingState: MutableStateFlow<Boolean>, modifier: Modifier = Modifier) {
    val range = viewModel.rangeStream.collectAsState().value
    val month = viewModel.monthStream.collectAsState().value
    val year = viewModel.yearStream.collectAsState().value
    val canIncrease = viewModel.canIncreaseStream.collectAsState().value
    val canDecrease = viewModel.canDecreaseStream.collectAsState().value

    Row(modifier = modifier) {
        DateRangePicker(viewModel, range, graphShowingState)

        when (range) {
            is DatePickerRange.DAY -> CalendarView(viewModel.dateStream)
            is DatePickerRange.MONTH -> {
                MonthPicker(month) { viewModel.monthStream.value = it }
                YearPicker(year) { viewModel.yearStream.value = it }
            }

            is DatePickerRange.YEAR -> YearPicker(year) { viewModel.yearStream.value = it }
            is DatePickerRange.CUSTOM -> CustomRangePicker(viewModel)
        }

        Spacer(modifier = Modifier.weight(1.0f))

        if (!range.isCustom()) {
            ESButton(
                modifier = Modifier
                    .padding(end = 14.dp)
                    .padding(vertical = 6.dp)
                    .size(36.dp),
                onClick = { viewModel.decrease() },
                contentPadding = PaddingValues(0.dp),
                enabled = canDecrease
            ) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Left")
            }

            ESButton(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .size(36.dp),
                onClick = { viewModel.increase() },
                contentPadding = PaddingValues(0.dp),
                enabled = canIncrease
            ) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Right")
            }
        }
    }
}

@Composable
fun MonthPicker(month: Int, enabled: Boolean = true, onClick: (Int) -> Unit) {
    var showing by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
            .padding(end = 14.dp)
    ) {
        SlimButton(
            enabled = enabled,
            onClick = { showing = true }
        ) {
            calendar.set(Calendar.MONTH, month)
            Text(monthFormat.format(calendar.time))
        }

        DropdownMenu(expanded = showing, onDismissRequest = { showing = false }) {
            for (monthIndex in 0 until 12) {
                calendar.set(Calendar.MONTH, monthIndex)
                val monthName = monthFormat.format(calendar.time)
                DropdownMenuItem(onClick = {
                    onClick(monthIndex)
                    showing = false
                }, text = {
                    Text(monthName)
                }, trailingIcon = {
                    if (monthIndex == month) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                    }
                })
                if (monthIndex < 11) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun YearPicker(year: Int, enabled: Boolean = true, onClick: (Int) -> Unit) {
    var showing by remember { mutableStateOf(false) }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
            .padding(end = 14.dp)
    ) {
        SlimButton(
            enabled = enabled,
            onClick = { showing = true }
        ) {
            Text(year.toString())
        }

        DropdownMenu(expanded = showing, onDismissRequest = { showing = false }) {
            for (yearIndex in 2021..currentYear) {
                DropdownMenuItem(onClick = {
                    onClick(yearIndex)
                    showing = false
                }, text = {
                    Text(yearIndex.toString())
                }, trailingIcon = {
                    if (yearIndex == year) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                    }
                })
                if (yearIndex < currentYear) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun DateRangePicker(
    viewModel: StatsDatePickerViewModel,
    range: DatePickerRange,
    graphShowingState: MutableStateFlow<Boolean>
) {
    var showing by remember { mutableStateOf(false) }
    val graphShowing = graphShowingState.collectAsState()

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
            .padding(end = 14.dp)
    ) {
        ESButton(
            onClick = { showing = true },
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(36.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = showing,
            onDismissRequest = { showing = false }
        )
        {
            DropdownMenuItem(onClick = {
                viewModel.rangeStream.value = DatePickerRange.DAY
                showing = false
            }, text = {
                Text(stringResource(R.string.day))
            }, trailingIcon = {
                if (range == DatePickerRange.DAY) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                }
            })
            HorizontalDivider()
            DropdownMenuItem(onClick = {
                viewModel.rangeStream.value = DatePickerRange.MONTH
                showing = false
            }, text = {
                Text(stringResource(R.string.month))
            }, trailingIcon = {
                if (range == DatePickerRange.MONTH) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                }
            })
            HorizontalDivider()
            DropdownMenuItem(onClick = {
                viewModel.rangeStream.value = DatePickerRange.YEAR
                showing = false
            }, text = {
                Text(stringResource(R.string.year))
            }, trailingIcon = {
                if (range == DatePickerRange.YEAR) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                }
            })
            HorizontalDivider()
            DropdownMenuItem(onClick = {
                viewModel.rangeStream.value = DatePickerRange.CUSTOM(LocalDate.now().minusDays(30), LocalDate.now())
                showing = false
            }, text = {
                Text(stringResource(R.string.custom_range))
            }, trailingIcon = {
                if (range.isCustom()) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                }
            })

            HorizontalDivider(thickness = 4.dp)
            DropdownMenuItem(onClick = {
                graphShowingState.value = !graphShowing.value
                showing = false
            }, text = {
                Text(if (graphShowing.value) stringResource(R.string.hide_graph) else stringResource(R.string.show_graph))
            }, trailingIcon = {
                Icon(imageVector = Icons.Default.BarChart, contentDescription = "graph")
            })
        }
    }
}

@Composable
fun CustomRangePicker(viewModel: StatsDatePickerViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalendarView(viewModel.customStartDate)
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.padding(end = 14.dp)
        )
        CalendarView(viewModel.customEndDate)
    }
}

@Composable
fun CalendarView(dateStream: MutableStateFlow<LocalDate>) {
    var showingDatePicker by remember { mutableStateOf(false) }

    val dateState = dateStream.collectAsState().value
    val millis = localDateToMillis(dateState)

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.BottomCenter)
            .padding(end = 14.dp)
    ) {
        SlimButton(
            onClick = { showingDatePicker = true }
        ) {
            Text(dateState.toString())
        }
        if (showingDatePicker) {
            Dialog(
                onDismissRequest = { showingDatePicker = false },
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                ) {
                    AndroidView(
                        { CalendarView(it) },
                        modifier = Modifier.wrapContentWidth(),
                        update = { views ->
                            views.date = millis
                            views.setOnDateChangeListener { _, year, month, dayOfMonth ->
                                val cal = Calendar.getInstance()
                                cal.set(year, month, dayOfMonth)
                                dateStream.value = millisToLocalDate(cal.timeInMillis)
                                showingDatePicker = false
                            }
                        }
                    )
                }
            }
        }
    }
}

fun localDateToMillis(localDate: LocalDate): Long {
    val localDateTime = LocalDateTime.of(localDate, LocalTime.MIDNIGHT)
    val zoneId = ZoneId.systemDefault()
    return localDateTime.atZone(zoneId).toInstant().toEpochMilli()
}

fun millisToLocalDate(millis: Long): LocalDate {
    val instant = Instant.ofEpochMilli(millis)
    val zoneId = ZoneId.systemDefault()
    val localDateTime = LocalDateTime.ofInstant(instant, zoneId)
    return localDateTime.toLocalDate()
}

@Preview(widthDp = 500, heightDp = 500)
@Composable
fun StatsDatePickerViewPreview() {
    StatsDatePickerView(
        viewModel = StatsDatePickerViewModel(MutableStateFlow(StatsDisplayMode.Day(LocalDate.now()))),
        graphShowingState = MutableStateFlow(false)
    )
}