package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.statsgraph.CalendarView
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

@Composable
fun ParameterGraphHeaderView(viewModel: ParametersGraphTabViewModel, modifier: Modifier = Modifier) {
    var hours by remember { mutableStateOf(0) }
    var showingVariables by remember { mutableStateOf(false) }
    var dateStream = MutableStateFlow<LocalDate>(LocalDate.now())

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        ParameterGraphVariableChooserButton(viewModel)

        Spacer(Modifier.defaultMinSize(minWidth = 8.dp))

        CalendarView(dateStream = dateStream)

        Spacer(Modifier.defaultMinSize(minWidth = 8.dp))

        HourPicker(hours, onHoursChanged = { hours = it })

        Spacer(Modifier.defaultMinSize(minWidth = 8.dp))

        Button(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(36.dp),
            onClick = { viewModel.decrease() },
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Left")
        }

        Spacer(Modifier.defaultMinSize(minWidth = 8.dp))

        Button(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(36.dp),
            onClick = { viewModel.increase() },
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Right")
        }
    }
}

@Composable
private fun HourPicker(hours: Int, onHoursChanged: (Int) -> Unit) {
    var showingHours by remember { mutableStateOf(false) }

    Button(
        onClick = { showingHours = true },
        modifier = Modifier
            .padding(vertical = 6.dp)
            .size(36.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null
        )
    }

    DropdownMenu(expanded = showingHours, onDismissRequest = { showingHours = false }) {
        DropdownMenuItem(onClick = {
            onHoursChanged(6)
            showingHours = false
        }) {
            Text("6 hours")
            if (hours == 6) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
            }
        }
        Divider()

        DropdownMenuItem(onClick = {
            onHoursChanged(12)
            showingHours = false
        }) {
            Text("12 hours")
            if (hours == 12) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
            }
        }
        Divider()

        DropdownMenuItem(onClick = {
            onHoursChanged(24)
            showingHours = false
        }) {
            Text("24 hours")
            if (hours == 24) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
            }
        }
    }
}
