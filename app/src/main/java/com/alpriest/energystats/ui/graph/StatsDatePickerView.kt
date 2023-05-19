package com.alpriest.energystats.ui.graph

import android.widget.CalendarView
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

enum class DatePickerRange {
    DAY,
    MONTH,
    YEAR
}

@Composable
fun StatsDatePickerView(viewModel: StatsDatePickerViewModel) {
    var showingDisplayMode by remember { mutableStateOf(false) }
    var showingDatePicker by remember { mutableStateOf(false) }
    val range = viewModel.rangeStream.collectAsState().value

    Row {
        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
            IconButton(
                onClick = { showingDisplayMode = true }
            ) {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
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

        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
            OutlinedButton(
                onClick = { showingDatePicker = true }
            ) {
                Text("May 17, 2023")
            }
            DropdownMenu(expanded = showingDatePicker, onDismissRequest = { showingDatePicker = false }) {
                AndroidView(
                    { CalendarView(it) },
                    modifier = Modifier.wrapContentWidth(),
                    update = { views ->
                        views.setOnDateChangeListener { calendarView, _, _, _ ->
                            viewModel.dateStream.value = calendarView.date
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1.0f))

        IconButton(onClick = { viewModel.decrease() }) {
            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Left")
        }

        IconButton(onClick = { viewModel.increase() }) {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Right")
        }
    }
}

@Preview(widthDp = 300, heightDp = 500)
@Composable
fun StatsDatePickerViewPreview() {
    StatsDatePickerView(viewModel = StatsDatePickerViewModel(MutableStateFlow(StatsDisplayMode.Day(Calendar.getInstance().timeInMillis))))
}