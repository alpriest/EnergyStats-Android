package com.alpriest.energystats.ui.helpers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.settings.SlimButton
import com.alpriest.energystats.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MonthPicker(month: Int, enabled: Boolean = true, onClick: (Int) -> Unit) {
    var showing by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

    Box(
        modifier = Modifier.Companion
            .wrapContentSize(Alignment.Companion.TopStart)
            .padding(end = 14.dp)
    ) {
        SlimButton(
            enabled = enabled,
            onClick = { showing = true }
        ) {
            calendar.set(Calendar.MONTH, month)
            Text(monthFormat.format(calendar.time), style = Typography.headlineMedium)
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