package com.alpriest.energystats.ui.helpers

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.alpriest.energystats.ui.settings.SlimButton
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar

@Composable
fun PopupCalendarView(dateStream: MutableStateFlow<LocalDate>, style: TextStyle) {
    var showingDatePicker by remember { mutableStateOf(false) }

    val dateState = dateStream.collectAsState().value
    val millis = localDateToMillis(dateState)

    Box(
        modifier = Modifier.Companion
            .wrapContentSize(Alignment.Companion.BottomCenter)
            .padding(end = 14.dp)
    ) {
        SlimButton(
            onClick = { showingDatePicker = true }
        ) {
            Text(
                dateState.toString(),
                style = style
            )
        }
        if (showingDatePicker) {
            Dialog(
                onDismissRequest = { showingDatePicker = false },
            ) {
                Column(
                    modifier = Modifier.Companion
                        .background(Color.Companion.White)
                ) {
                    AndroidView(
                        { CalendarView(it) },
                        modifier = Modifier.Companion.wrapContentWidth(),
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

@Composable
fun CalendarView(date: LocalDate, onChange: (LocalDate) -> Unit) {
    val millis = localDateToMillis(date)

    AndroidView(
        { CalendarView(it) },
        modifier = Modifier.Companion.wrapContentWidth(),
        update = { views ->
            views.date = millis
            views.setOnDateChangeListener { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                onChange(millisToLocalDate(cal.timeInMillis))
            }
        }
    )
}

private fun localDateToMillis(localDate: LocalDate): Long {
    val localDateTime = LocalDateTime.of(localDate, LocalTime.MIDNIGHT)
    val zoneId = ZoneId.systemDefault()
    return localDateTime.atZone(zoneId).toInstant().toEpochMilli()
}

private fun millisToLocalDate(millis: Long): LocalDate {
    val instant = Instant.ofEpochMilli(millis)
    val zoneId = ZoneId.systemDefault()
    val localDateTime = LocalDateTime.ofInstant(instant, zoneId)
    return localDateTime.toLocalDate()
}
