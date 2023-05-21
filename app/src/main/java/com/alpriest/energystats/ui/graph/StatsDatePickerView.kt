package com.alpriest.energystats.ui.graph

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.*
import java.util.*

enum class DatePickerRange {
    DAY,
    MONTH,
    YEAR
}

@Composable
fun StatsDatePickerView(viewModel: StatsDatePickerViewModel) {
    var showingDisplayMode by remember { mutableStateOf(false) }
    val range = viewModel.rangeStream.collectAsState().value
    val currentMonth = remember { YearMonth.now() }

    Row {
        Box(
            modifier = Modifier
                .wrapContentSize(Alignment.TopStart)
                .padding(end = 14.dp)
        ) {
            Button(
                onClick = { showingDisplayMode = true }
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showingDisplayMode,
                onDismissRequest = { showingDisplayMode = false }
            )
            {
                DropdownMenuItem(onClick = { viewModel.rangeStream.value = DatePickerRange.DAY }) {
                    Row {
                        Text("Day")
                        if (range == DatePickerRange.DAY) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                        }
                    }
                }
                Divider()
                DropdownMenuItem(onClick = { viewModel.rangeStream.value = DatePickerRange.MONTH }) {
                    Text("Month")
                    if (range == DatePickerRange.MONTH) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                    }
                }
                Divider()
                DropdownMenuItem(onClick = { viewModel.rangeStream.value = DatePickerRange.YEAR }) {
                    Text("Year")
                    if (range == DatePickerRange.YEAR) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                    }
                }
            }
        }

        calendarView(viewModel = viewModel)

        Spacer(modifier = Modifier.weight(1.0f))

        Button(
            modifier = Modifier.padding(end = 14.dp),
            onClick = { viewModel.decrease() }
        ) {
            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Left", modifier = Modifier.size(20.dp))
        }

        Button(
            modifier = Modifier.padding(end = 14.dp),
            onClick = { viewModel.increase() }
        ) {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Right", modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun calendarView(viewModel: StatsDatePickerViewModel) {
    var showingDatePicker by remember { mutableStateOf(false) }

    val dateState = viewModel.dateStream.collectAsState().value
    val millis = localDateToMillis(dateState)

    Box(modifier = Modifier.wrapContentSize(Alignment.BottomCenter)) {
        Button(
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
                            views.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
                                val cal = Calendar.getInstance()
                                cal.set(year, month, dayOfMonth)
                                viewModel.dateStream.value = millisToLocalDate(cal.timeInMillis)
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
    StatsDatePickerView(viewModel = StatsDatePickerViewModel(MutableStateFlow(StatsDisplayMode.Day(LocalDate.now()))))
}