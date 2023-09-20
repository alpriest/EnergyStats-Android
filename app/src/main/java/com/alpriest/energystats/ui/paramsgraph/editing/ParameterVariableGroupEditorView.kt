package com.alpriest.energystats.ui.paramsgraph.editing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.alpriest.energystats.ui.settings.ContentWithBottomButtons
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun ParameterVariableGroupEditorView(viewModel: ParameterVariableGroupEditorViewModel, navController: NavHostController) {
    var expanded by remember { mutableStateOf(false) }
    val selectedGroup = viewModel.selected.collectAsState().value
    val variables = viewModel.variables.collectAsState().value
    val groups = viewModel.groups.collectAsState().value

    ContentWithBottomButtons(navController = navController, onSave = {}) {
        SettingsPage {
            Column {
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
                                groups.forEach { group ->
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

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            "Rename...",
                            color = colors.onPrimary
                        )
                    }

                    Button(onClick = { /*TODO*/ }) {
                        Text(
                            "Create new...",
                            color = colors.onPrimary
                        )
                    }
                }
            }

            SettingsColumnWithChild {
                SettingsTitleView("Choose parameters")
                ParameterVariableListView(variables = variables, onTap = { })
            }
        }
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
            ),
            NavHostController(LocalContext.current)
        )
    }
}