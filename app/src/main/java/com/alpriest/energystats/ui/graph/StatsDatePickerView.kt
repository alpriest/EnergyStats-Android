package com.alpriest.energystats.ui.graph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun StatsDatePickerView() {
    var showingDisplayMode by remember { mutableStateOf(false) }

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
                DropdownMenuItem(onClick = { /* Handle refresh! */ }) {
                    Text("Day")
                }
                DropdownMenuItem(onClick = { /* Handle settings! */ }) {
                    Text("Month")
                }
                DropdownMenuItem(onClick = { /* Handle settings! */ }) {
                    Text("Year")
                }
            }
        }

        Button(
            onClick = { /*TODO*/ }
        ) {
            Text("May 17, 2023")
        }

        Spacer(modifier = Modifier.weight(1.0f))

        IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Left")
        }

        IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Right")
        }
    }
}

@Preview(widthDp = 300, heightDp = 500)
@Composable
fun StatsDatePickerViewPreview() {
    StatsDatePickerView()
}