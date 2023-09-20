package com.alpriest.energystats.ui.paramsgraph.editing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ParameterVariableGroupEditorView(viewModel: ParameterVariableGroupEditorViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val selectedGroup = viewModel.selected.collectAsState().value

    SettingsPage {
        SettingsColumnWithChild {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Choose group to edit",
                    color = colors.onSecondary
                )

                Box(contentAlignment = Alignment.TopEnd) {
                    Button(onClick = { expanded = !expanded }) {
                        Text(
                            selectedGroup.title,
                            fontSize = 12.sp,
                            color = colors.onPrimary,
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = colors.onPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        viewModel.groups.forEach { group ->
                            DropdownMenuItem(onClick = {
                                expanded = false
                                viewModel.select(group)
                            }) {
                                Text(group.title)
                            }
                        }
                    }
                }
            }
        }
    }
}

class ParameterVariableGroupEditorViewModel(val groups: List<ParameterGroup>) {
    val selected = MutableStateFlow(groups.first())

    fun select(group: ParameterGroup) {
        selected.value = group
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun ParameterVariableGroupEditorViewPreview() {
    EnergyStatsTheme(darkTheme = false) {
        ParameterVariableGroupEditorView(
            viewModel = ParameterVariableGroupEditorViewModel(
                listOf(
                    ParameterGroup("first", listOf("a", "b", "c")),
                    ParameterGroup("second", listOf("a", "b", "c"))
                )
            )
        )
    }
}