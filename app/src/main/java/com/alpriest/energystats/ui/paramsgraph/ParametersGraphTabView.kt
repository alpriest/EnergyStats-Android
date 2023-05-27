package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

@Composable
fun ParametersGraphTabView(viewModel: ParametersGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>) {
    Column {
        ParameterGraphHeaderView(viewModel = viewModel)
    }
}

@Composable
fun ParameterGraphHeaderView(viewModel: ParametersGraphTabViewModel) {
    var hours by remember { mutableStateOf(0) }
    var showingVariables by remember { mutableStateOf(false) }
    var showingHours by remember { mutableStateOf(false) }

    Row {
        Button(
            onClick = { showingVariables = true },
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(36.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Checklist,
                contentDescription = null
            )
        }

        Spacer(Modifier.defaultMinSize(minWidth = 8.dp))

        Button(onClick = {}) {
            Text("May 16, 2023")
        }

        Spacer(Modifier.defaultMinSize(minWidth = 8.dp))

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
                hours = 6
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
                hours = 12
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
                hours = 24
                showingHours = false
            }) {
                Text("24 hours")
                if (hours == 24) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.Done, contentDescription = "checked")
                }
            }
        }

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

data class ParametersDisplayMode(
    val date: LocalDate,
    val hours: Int
)

data class ParametersGraphValue(val graphPoint: Int, val value: Double, val type: RawVariable)

data class ParametersGraphVariable(
    val type: RawVariable,
    var enabled: Boolean,
    var isSelected: Boolean
) {
    val id: String
        get() = type.title()

    fun setIsSelected(selected: Boolean) {
        isSelected = selected
        enabled = true
    }
}

@Preview
@Composable
fun PreviewParameterGraphHeaderView() {
    ParameterGraphHeaderView(viewModel = ParametersGraphTabViewModel(configManager = FakeConfigManager(), networking = DemoNetworking()))
}